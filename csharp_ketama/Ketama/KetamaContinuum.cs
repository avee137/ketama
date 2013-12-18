using System.Collections.Generic;
using System.Linq;

namespace Ketama
{
    /// <summary>
    /// The Ketama Continuum class which implements a consistent hashing algorithm, AKA an approach on how to distribute and find keys across servers
    /// even when the server number is changing. Modeled after the Ketama library (https://github.com/RJ/ketam) 
    /// Uses the Fowler-Noll-Vo alternative (fnv1a) algorithm (http://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function) for hashing keys and servers,
    /// currently supports only the fnv1a 32-bit version, but could be extended to use the 64-bit version, or another hashing algorithm entirely.
    /// </summary>
    public class KetamaContinuum
    {
        #region Constructor

        public SortedDictionary<uint, string> _continuum;                             //The continuum of hash values with each server they should connect to
        public KetamaServers ketamaServers;                                           //A class to contain the collection of KetamaServer objects we're connected to on the continuum. Acts as a way of grouping the KetamaServer objects together.
        private KetamaHashingAlgorithm_FNV1a32Bit hashingAlgorithm;                   //The hashing algorithm object to actually do our hashing

        /// <summary>
        /// Initializing the KetamaContinuum object.
        /// </summary>
        /// <param name="server">A string containing server 'name weight' values (e.g. '10.10.10.10:1234 5')
        /// Can separate multiple 'name weight' inputs via commas (e.g. '10.10.10.10:1234 5, 20.20.20.20:2345 6') in order
        /// to pass in multiple servers to connect to at once.</param>
        public KetamaContinuum(string serverNames = null)
        {
            _continuum = new SortedDictionary<uint, string>();
            hashingAlgorithm = new KetamaHashingAlgorithm_FNV1a32Bit();
            ketamaServers = new KetamaServers();
            if (serverNames != null) { SyncServerConections(serverNames); }
        }

        #endregion Constructor
                            
        #region Public methods

        /// <summary>
        /// Allows you to pass in a string of server 'name weight' values and make sure that those are the only servers
        /// along the continuum. Meaning that any servers that are in the passed-in string not already connected to will get added to 
        /// the continuum, and any servers that aren't in the passed-in string but are connected to will get removed from the 
        /// continuum.
        /// </summary>
        /// <param name="serverNames">A string of server 'name weight' values that we want to connect to.</param>
        public void SyncServerConections(string serversNames)
        {
            string[] newServers = ParseServerString(serversNames);
            List<string> existingServers = ketamaServers.GetServerList();

            //looping through a List of the servers that we need to be connected to, seeing if there's any new servers we want to be connected to that we aren't (i.e., need to add a server)
            foreach (string server in newServers)
            {
                //checks if there's an item in the new list of servers that's not present in the existing list of servers. If this is the case, we add it.
                if (!existingServers.Contains(server))
                {
                    AddServerConnection(server);
                }
            }

            //looping through a List of the servers we currently have, seeing if there are any in the passed-in list of servers we have but aren't in that list (i.e., need to be removed)
            foreach (string server in existingServers)
            {
                if (!newServers.Contains(server))
                {
                    RemoveServerConnection(server);
                }
            }
        }

        /// <summary>
        /// Where we create a new Ketama Server based on the ketama value, add it to the KetamaServers object (which keeps track of the servers),
        /// and we generate the hash values for the server being added.
        /// </summary>
        /// <param name="server">A string array containing the server 'name weight'
        /// Allows for multiple server values to be passed in a time</param>
        public void AddServerConnection(string server)
        {
            string[] serverSplit = server.Split(new char[] { ' ' });
            string serverName = serverSplit[0];

            ketamaServers.AddServerToServers(serverName);
            AddHashValuesForServerToContinuum(serverName);
        }

        /// <summary>
        /// Removes the keys associated with a server from the continuum,
        /// and then removes the serverInfo object associated with the server from the list of servers
        /// </summary>
        /// <param name="serverName">A server name string (i.e., "10.24.16.40:6245") which should already
        /// be stored in the List of serverInfo objects called servers so that we can remove it now</param>
        public void RemoveServerConnection(string serverName)
        {
            RemoveHashValuesForServerFromContinuum(serverName);
            ketamaServers.RemoveServerFromServers(serverName);
        }

        /// <summary>
        /// Finds what server the key we want is in.
        /// The key and the server are (well, at least they should be) hashed using the same hashing function; 
        /// the value for the key is located at the closest server hashed number equal to (not likely) or greater than the key value hashed pair.
        /// </summary>
        /// <param name ="keyForHost">The key we want to look up</param>
        /// <returns>The server where the key is housed in. Specifically, the server that has a hashed key value equal to or closeset-greater than the hashed value of the desired key.
        /// If we get to the top of the server continuum without running into a server, then it goes around and starts from zero and tries again 
        /// (hence finding the server with the hashed value that's hashed to the smallest numerical value)</returns>       
        public string FindServerForKey(string keyForHost)
        {
            uint hashKey = hashingAlgorithm.GetHashFromString(keyForHost); //get the hashed value for the key we're trying to look up
            uint upperBoundKey = _continuum.Keys.Max(); //find the highest numerical value that an actual server has hashed to
            string serverName;

            //if the highest hashed value for a server is still less than the hashed value of the key,
            //it means we have to go around the circle, and thus select the server with the smallest hashed value
            if (upperBoundKey < hashKey)
            {
                serverName = _continuum[_continuum.Keys.Min()];
                return serverName;
            }

            foreach (uint serverKey in _continuum.Keys)
            {
                if (serverKey >= hashKey && serverKey <= upperBoundKey)
                {
                    upperBoundKey = serverKey;
                }
            }
            serverName = _continuum[upperBoundKey];
            return serverName;
        }

        /// <summary>
        /// Returns a string List of server IP values currently connected to on the ketama continuum
        /// </summary>
        /// <returns>String List of server UPs</returns>
        public List<string> GetServerList()
        {
            return ketamaServers.GetServerList();
        }

        #region Unit testing methods

        /// <summary>
        /// For unit testing
        /// </summary>
        /// <returns>A comma-delimited list of server names</returns>
        public string ServerToString()
        {
            List<string> serverNames = ketamaServers.GetServerList();
            return string.Join(",", serverNames);
        }


        /// <summary>
        /// For unit testing
        /// </summary>
        /// <param name="serverName">The server name (e.g. 10.24.16.40:624) we want to look up the hashes for</param>
        /// <returns>A list of hashes associated with the server passed in</returns>
        public List<uint> GetHashesOfIndividualServer(string serverName)
        {
            List<uint> hashList = new List<uint>();

            //finding the specific server to get its hash list
            foreach (KeyValuePair<uint, string> serverHashes in _continuum)
            {
                if (serverName == serverHashes.Value)
                {
                    hashList.Add(serverHashes.Key);
                }
            }
            return hashList;
        }

        /// <summary>
        /// For unit testing
        /// </summary>
        /// <returns>List of hashes</returns>
        public List<uint> GetAllHashes()
        {
            return _continuum.Keys.ToList();
        }

        #endregion Unit testing methods

        #endregion Public methods

        #region Private methods
        /// <summary>
        /// Used to separate out an input string of 'name weight' values into a string array of values.
        /// Put in a separate function in case we want to do more processing on the string before setting them to KetamaServer objects.
        /// </summary>
        /// <param name="serverString">A string of 'name weight' values</param>
        /// <returns>An array of those 'name weight' values</returns>
        private string[] ParseServerString(string serverString)
        {
            string[] servers = serverString.Split(new char[] { ',' });
            return servers;
        }

        /// <summary>
        /// Adding hash values to a continuum, used when adding a server to the continuum.
        /// </summary>
        /// <param name="serverName">The server name we want to calculate hash values based upon</param>
        private void AddHashValuesForServerToContinuum(string serverName)
        {
            List<uint> hashes = CalculateHashValuesForServer(serverName);      //Calculate the hashes for the new server 

            foreach (uint hash in hashes)                                      //Adding all the hashes to the _continuum
            {
                _continuum.Add(hash, serverName);
            }
        }

        /// <summary>
        /// A method to separate hash values from a continuum, used when removing a server from the continuum.
        /// </summary>
        /// <param name="serverName">The server name for the server we're removing from the continuum</param>
        private void RemoveHashValuesForServerFromContinuum(string serverName)
        {
            List<uint> keysToRemove = new List<uint>();
            //Going through the continuum and getting a separate list of all the keys that will need to be removed
            //Have to use this approach of making a temporary list of the keys to remove because we aren't allowed to delete KeyValuePairs in 
            //a dictionary while iterating over that same dictionary
            foreach (KeyValuePair<uint, string> serverHashes in _continuum)
            {
                if (serverHashes.Value == serverName)
                {
                    keysToRemove.Add(serverHashes.Key);
                }
            }
            //Actually going through the _continuum and getting rid of the keys that we listed we need to remove
            foreach (uint hash in keysToRemove)
            {
                _continuum.Remove(hash);
            }
        }

        /// <summary>
        /// Where the servers get their names hashed to be placed along the continuum.
        /// How the hashing works: For the first hash, we calculate the hash with the server name (e.g. 'redis1') and the default offset value,
        /// then for subsequent hashes we take the previous hash, use that hash value as the initial offset, and redo the hash.
        /// In other words, for all but the first hash, when calculating a new hash value, the one that came before it is used as the salt. 
        /// </summary>
        /// <param name="serverName">A server name to base hashes on</param>
        /// <returns>A List(uint) of hashes for a given KetamaServer object</returns>
        private List<uint> CalculateHashValuesForServer(string serverName)
        {
            KetamaServer server = ketamaServers.GetServer(serverName);
            List<uint> listOfHashes = new List<uint>();
            uint serverHostKeyHash;

            for (int individualReplica = 0; individualReplica < server.factor; individualReplica++)
            {
                if (individualReplica == 0)
                {
                    serverHostKeyHash = hashingAlgorithm.GetHashFromString(server.serverName);
                }
                else
                {
                    serverHostKeyHash = hashingAlgorithm.GetHashFromString(server.serverName, listOfHashes.Last()); //using the last entry in the hash list as the offset for the next hash to come

                }
                listOfHashes.Add(serverHostKeyHash);
            }
            return listOfHashes;
        }
    }
        #endregion Private methods

}
