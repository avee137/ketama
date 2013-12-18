package java_ketama;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.math.BigInteger;

/** 
 * This class is a ketama pool just the same logic with C.
 *
 * add syncServers:   Syncronizes ketama ring based on the list of node names given 
 * add ketamaInfo:    Populates global info information about the ketama pool
 *
 */
public class Ketama {


	// store instances of pools
	private static Map pools = new HashMap();

	// Constants
	public static final int NATIVE_HASH 	= 0;			// native String.hashCode();
	public static final int OLD_COMPAT_HASH = 1;			// original compatibility hashing algorithm (works with other clients)
	public static final int NEW_COMPAT_HASH = 2;			// new CRC32 based compatibility hashing algorithm (works with other clients)
	public static final int KETAMA_HASH 	= 3;			// MD5 Based
	public static final int FNV1A_32 	= 4;			//fnv1A-32 based
	private static final int POOL_SIZE      = 160;			//every server has 160 points in pool

	// Pool data
	private boolean initialized       	= false;

	// initial, mod time and hash algorithm 
	private int hashingAlg 		  	= FNV1A_32;		// default to using the FNV1A_32 hash as it is the fastest
	private int version		  	= 1;			// initialize ketama version
	private int modTime		  	= 0;			// update time recorder

	// list of all servers
	private String[] servers;
	private Integer[] weights;
	private String[] names;
	private ArrayList<Long> serverpool;				//sorted server hash value
	private Integer totalWeight = 0;
	private static TreeMap<Long, String> buckets = new TreeMap<Long, String>();  

	// empty constructor
	protected Ketama() { }

	/** 
	 * Factory to create/retrieve new pools given a unique poolName. 
	 * 
	 * @param poolName unique name of the pool
	 * @return instance of Ketama
	 */
	@SuppressWarnings("unchecked")
	public static synchronized Ketama getInstance( String poolName ) {
		if ( pools.containsKey( poolName ) )
			return (Ketama)pools.get( poolName );

		Ketama pool = new Ketama();
		pools.put( poolName, pool );

		return pool;
	}

	/** 
	 * Single argument version of factory used for back compat.
	 * Simply creates a pool named "default". 
	 * 
	 * @return instance of Ketama
	 */

	public static synchronized Ketama getInstance() {
		return getInstance( "default" );
	}
	
	/**
	 * brief Populates global info information about the ketama pool
	 *
	 * @param poolName unique name of the pool
	 * @return 
	 */
	public void ketamaInfo(){
		
		
		System.out.print(	"{\"ketama_info\":{\"version\":" + getVersion()+
					"\"mod_time\":" + getModTime()+
					"\"num_servers\":" + servers.length+
					"\"mem_total\":" + totalWeight+
					"\"server_list\":[" 
				);
				
		
		String [] currentServers = getServers();
		int size = currentServers.length;
		Integer [] currentWeights = getWeights();
		for (int i = 0; i<size; i++)
		{
			System.out.print(	"{\"server\":\""+ this.servers[i]+
						"\",\"mem\":"+	currentWeights[i]+
						"}");
			if (i!=size-1)
			System.out.print(",");
		}
                
		System.out.println("]}}");
	}	 	

	/**
 	 * brief Get current version of pool
 	 * @param 
 	 * @return version number
 	 */
	public int getVersion(){ return version;}


	/**
	 * brief Get current modTime of pool
 	 * @param
         * @return modTime
         */
        public int getModTime(){ return modTime;}
	
	/**
         * brief add newServer and newWeight to serverpool and buckets
         * @param newServer, newWeight
         * @return 
         */
	public void addServer(String newServer, Integer newWeight) {
		//update totalWeight
               	this.totalWeight+=newWeight;
		
		//find if same server name exists	
		if (servers!=null && Arrays.asList(servers).contains(newServer))
			return;		
	
		//add corresponding hash value from buckets and serverpool
		Long hv = calculateHash(newServer);
                for(int j = 0; j < POOL_SIZE; j++) {
			serverpool.add(hv);
                        buckets.put(hv, newServer);
			//update hv using getFnv1Hash with hv from last step
			if (hashingAlg == FNV1A_32){
	                        hv = getFnv1Hash((newServer).toCharArray(), hv);
			} else{
				hv = calculateHash(newServer);
			}
               }         

	}
	
	/**
         * brief delete delServer and delWeight from serverpool and buckets
         * @param newServer, newWeight
         * @return
         */
	public void deleteServer(String delServer, Integer delWeight) {
		//update totalWeight
		this.totalWeight-=delWeight;	
		
		//delete corresponding hash value from buckets and serverpool
		Long hv = calculateHash(delServer);
                for(int j = 0; j < POOL_SIZE; j++) {
                        serverpool.remove(serverpool.indexOf(hv));
                        buckets.remove(hv);
			//update hv using getFnv1Hash with hv from last stepi
			if (hashingAlg == FNV1A_32){
	                        hv = getFnv1Hash((delServer).toCharArray(), hv);
			} else{
				hv = calculateHash(delServer);
			}
                }
	}		
	/**
         * brief find Server index from serverArray
         * @param name, arr
         * @return index
         */
	public int findFromServers(String name, String[] arr){
		for (int i = 0; i<arr.length; i++)
		{
			if (arr[i].equals(name)){
				return i;
			}
		}
		//not found
		return -1;
	}

	 /**
          * brief find Weight index from weightArray
          * @param name, arr
          * @return index
          */
        public int findFromWeights(Integer name, Integer[] arr){
                for (int i = 0; i<arr.length; i++)
                {
                        if (arr[i].equals(name)){
                                return i;
			}
                }
		//not found
                return -1;
        }

	/**
 	 * brief Syncronizes ketama ring based on the list of node names given
 	 * @param node_names comma separate list of node names that ketama should have
 	 * @param cont is the continuum which will be syncronized
 	 * return
 	 */ 	
	public void syncServers( String[] newServers, Integer[] newWeights){
	
		//find servers and their corresponding weights which does not exist in newServers list
		//and delete them from serverpool and bucket	
		for (String checkServer:servers)
		{
			//first set every element from servers does not have identical from newServer
			boolean hasIdentical = false;
			for (String newCheckServer: newServers){
				if (checkServer.equals(newCheckServer)){
					hasIdentical = true;
					break;
				}
			}
			//if we need to remove from serverpool and bucket
			if (hasIdentical == false){
				Integer checkWeight = weights[findFromServers(checkServer, servers)];
				deleteServer(checkServer, checkWeight);
			}
		}

		//find servers and their corresponding weights which need to be added to serverpool and bucket
		//and add them into serverpool and bucket
		for (String newCheckServer:newServers)
                {
			//first set every element from newServers does not have identical from server
                        boolean hasIdentical = false;
                        for (String checkServer: servers){
                        	if (checkServer.equals(newCheckServer)){
                                	hasIdentical = true;
					break;
				}
			}
			//if we need to add into serverpool and bucket
                        if (hasIdentical == false){
                                Integer newCheckWeight = newWeights[findFromServers(newCheckServer, newServers)];
				addServer(newCheckServer, newCheckWeight);
                        }
                }

		this.servers = newServers;
                this.weights = newWeights;
		
		//sort serverpool for quicker searching hashvalue
		Collections.sort(serverpool);
	}
	
	
	/**
         * brief for a given key, print the server name it is assigned to
         * @param key is input key
         * @return server name
        */
	public String getServerForKey(String key){

		Long hv = this.calculateHash(key);
		int bucketSize = buckets.size();
		
		//divide and conquer array search to find server with next biggest
		//point after what this key hashed to
		int lowp = 0, midp, highp;
		highp = bucketSize;
		Long midval, midval1;
		while (true){
			midp = (lowp+highp)/2;
			if (midp == bucketSize)
				return buckets.get(serverpool.get(0)); //if at the end, roll back to zeroth
			midval = serverpool.get(midp);
			midval1 = midp == 0 ? 0 : serverpool.get(midp-1);
			if ((hv <= midval)&&(hv > midval1))
				return buckets.get(serverpool.get(midp));
			if (midval < hv)
				lowp = ++midp;
			else 
				highp = --midp;
			if (lowp > highp)
				return buckets.get(serverpool.get(0));
		}
	}

	/**
	 * NATIVE_HASH = 0
	 * OLD_COMPAT_HASH = 1
	 * NEW_COMPAT_HASH = 2
	 * KETAMA_HASH = 3
	 * FNV1A_32 = 4
	 * @param key
	 * @return HashValue
	 */
	public Long calculateHash(String key) {
		
		switch ( hashingAlg ) {
			case NATIVE_HASH:
				return (long)key.hashCode();
			case OLD_COMPAT_HASH:
				return (long)origCompatHashingAlg(key);
			case NEW_COMPAT_HASH:
				return (long)newCompatHashingAlg(key);
			case FNV1A_32:
				return getFnv1Hash(key.toCharArray());
			default:
				// use the native hash as a default
				hashingAlg = NATIVE_HASH;
				return (long)key.hashCode();
		}		
	}	
	
	/** 
	 * Sets the list of all cache servers and weights for each string in the array
	 * that should be in the form ip:port tweight
	 * @param servers 
	 */
	public void setServersAndWeights(String[] newServersAndWeights) {

		this.servers = new String[newServersAndWeights.length];
		this.weights = new Integer[newServersAndWeights.length];
		this.totalWeight = 0;
		version++;
		modTime = (int)System.currentTimeMillis();

		for(int i = 0; i<newServersAndWeights.length; i++) {
			String[] split = newServersAndWeights[i].split(" ");
			this.servers[i] = split[0];
			try {
				this.weights[i] = Integer.parseInt(split[1]);				
			} catch (Exception e) {
				this.weights[i] = 1;
			}
			this.totalWeight+=this.weights[i];
		}
	}


	/**
         * Returns the current list of all cache servers.
         *
         * @return String names of servers
         */
        public String[] getNames() { return this.names; }


        /**
 	 * Sets the list of all cache names for each string in the array
         * 
         * @param names
         *                            
         */
        public void setNames(String[] names){
		this.names = names; 
        }

	/** 
	 * Returns the current list of all cache servers. 
	 * 
	 * @return String array of servers [host:port]
	 */
	public String[] getServers() { return this.servers; }

	/** 
	 * Sets the list of weights to apply to the server list.
	 *
	 * This is an int array with each element corresponding to an element<br/>
	 * in the same position in the server String array. 
	 * 
	 * @param weights Integer array of weights
	 */
	public void setWeights(Integer[] weights) { 
		version++;  
		this.weights = weights; 
		modTime = (int)System.currentTimeMillis();
	}
	
	/** 
	 * Returns the current list of weights. 
	 * 
	 * @return int array of weights
	 */
	public Integer[] getWeights() { return this.weights; }


	/** 
	 * Sets the hashing algorithm we will use.
	 *
	 * The types are as follows.
	 *
	 * Ketama.NATIVE_HASH (0)     - native String.hashCode() - fast (cached) but not compatible with other clients
	 * Ketama.OLD_COMPAT_HASH (1) - original compatibility hashing alg (works with other clients)
	 * Ketama.NEW_COMPAT_HASH (2) - new CRC32 based compatibility hashing algorithm (fast and works with other clients)
	 * 
	 * @param alg int value representing hashing algorithm
	 */
	public void setHashingAlg( int alg ) { this.hashingAlg = alg; }

	/** 
	 * Returns current status of customHash flag
	 * 
	 * @return true/false
	 */
	public int getHashingAlg() { return this.hashingAlg; }

	
	/** 
	 * Internal private hashing method.
	 *
	 * This is the original hashing algorithm from other clients.
	 * Found to be slow and have poor distribution.
	 * 
	 * @param key String to hash
	 * @return hashCode for this string using our own hashing algorithm
	 */
	private static int origCompatHashingAlg( String key ) {
		int hash    = 0;
		char[] cArr = key.toCharArray();

		for ( int i = 0; i < cArr.length; ++i ) {
			hash = (hash * 33) + cArr[i];
		}

		return hash;
	}

	/** 
	 * Internal private hashing method.
	 *
	 * This is the new hashing algorithm from other clients.
	 * Found to be fast and have very good distribution. 
	 *
	 * UPDATE: This is dog slow under java
	 * 
	 * @param key 
	 * @return 
	 */
	private static int newCompatHashingAlg( String key ) {
		CRC32 checksum = new CRC32();
		checksum.update( key.getBytes() );
		int crc = (int) checksum.getValue();

		return (crc >> 16) & 0x7fff;
	}

	

	/**
         * Calculates the ketama hash value for character array with fnv1a32, return value is cast into long
	 * INIT 32 is set to be default value: 811c9dc5
         * @param key
         * @return hashvalue
        */
        public static Long getFnv1Hash(char[] str) {
        	byte [] data = String.valueOf(str).getBytes();
                
                BigInteger INIT32  = new BigInteger("811c9dc5",         16);
                BigInteger PRIME32 = new BigInteger("01000193",         16);
                BigInteger MOD32   = new BigInteger("2").pow(32);
                
                
                
                BigInteger hash = INIT32;
                
                for (byte b : data) {
                    hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
                    hash = hash.multiply(PRIME32).mod(MOD32);
                }


        	return hash.longValue();
        }
	
	/**
 	 * Calculates the ketama hash value for character array with fnv1a32, return value is cast into long
	 * hash value is calculated based on INIT32 which is set to be second input parameter
         * @param key, previous
         * @return hashvalue
        */
        public static Long getFnv1Hash(char[] str, Long previous) {
                byte [] data = String.valueOf(str).getBytes();

                BigInteger INIT32  = BigInteger.valueOf(previous);
                BigInteger PRIME32 = new BigInteger("01000193",         16);
                BigInteger MOD32   = new BigInteger("2").pow(32);



                BigInteger hash = INIT32;

                for (byte b : data) {
                    hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
                    hash = hash.multiply(PRIME32).mod(MOD32);
                }


                return hash.longValue();
        }


	/**
         * Calculates the ketama hash value for character array with fnv1a32,this function returns BigInteger as return type
         * @param key
         * @return hashvalue
         */
        public static BigInteger getFnv1HashBigInteger(char[] str) {
                byte [] data = String.valueOf(str).getBytes();

                BigInteger INIT32  = new BigInteger("811c9dc5", 16);
                BigInteger PRIME32 = new BigInteger("01000193", 16);
                BigInteger MOD32   = new BigInteger("2").pow(32);

                BigInteger hash = INIT32;
                for (byte b : data) {
                    hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
                    hash = hash.multiply(PRIME32).mod(MOD32);
                }
                return hash;
      }

	/** 
 	 * Initializes the pool. 
	 */
	@SuppressWarnings("unchecked")
	public synchronized void initialize(String[] newServers, String[] newNames, Integer[] newWeights) {
		// initialize empty maps
		buckets = new TreeMap();
		
		// throw a runtime exception
		if ( newServers == null || newServers.length <= 0 ) {
			throw new IllegalStateException( "++++ trying to initialize with no servers" );
		}

		if (initialized == true){
			version++;
		} else{
			version = 1;
		}

		if (newWeights == null) {
			this.totalWeight = this.servers.length;
		}
		int index = 0;
		this.serverpool = new ArrayList<Long>();
		for (int i = 0;i<newServers.length;i++) {
			
			int thisWeight = 1;
			if(newWeights != null && newWeights[i] != null) {
				thisWeight = newWeights[i];
			}
			this.addServer(newServers[i], thisWeight);
		}

		this.servers = newServers;
                this.names = newNames;
                this.weights = newWeights;

		Collections.sort(serverpool);
		// mark pool as initialized
		this.initialized = true;
	}

	/** 
	 * Returns state of pool. 
	 * 
	 * @return <CODE>true</CODE> if initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
 	 * Set initialized
 	 *
 	 * @param status	
 	 * @return 
 	 */
	public void SetInitialize(Boolean status) {
               initialized = status;
        }

}

