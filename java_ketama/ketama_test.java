package java_ketama;                                 
import java.util.*;
import java.io.*;

public class ketama_test
{
  //An example of initializing using defaults and providing weights for servers:
  public static void main(String[] args)
  {
        ArrayList<String> machinelist = new ArrayList<String>();                      
        try {
	   String line;
	   Scanner input = new Scanner(new File(args[0]));                                                                    
           while(input.hasNextLine()){
	     line = input.nextLine();
	     line = line.trim();
	     if ((line.length()>1)&&(((line.charAt(0)>='0')&&(line.charAt(0)<='9'))||((line.charAt(0)>='a')&&(line.charAt(0)<='z'))||((line.charAt(0)>='A')&&(line.charAt(0)<='Z'))))
	     {
  		machinelist.add(line);
	     }
           }          
	 
                                                                                                                                                                   
        } catch (Exception e) {
	   System.out.println(e);
	}
        //initialize with input file path
        System.out.println("====input servers:===="); 
        int len = machinelist.size();
	String[] names = new String[len];
	String[] servers = new String[len];
	Integer[] weights = new Integer[len];
	int index = 0;
	for (String str:machinelist)
	{
	    String[] split = str.split(" ");
	    names[index]=split[0];
	    servers[index]=split[1];
	    weights[index]=Integer.parseInt(split[2]);
	    System.out.println(str);
	    index++;
 	}
	System.out.println();
     	Ketama pool = Ketama.getInstance();
     	pool.initialize(servers, names, weights);

	//example with input file
	System.out.println("====Continuum with input file:====");
	pool.ketamaInfo();
	System.out.println();

	//display hash value
	System.out.println("====ketama hash:  "+ (int)(long)pool.calculateHash("test")+"====\n");
	
	// change serverlist and sync
	System.out.println("====Here is default test case:====");
	String[] newmachinelist = { "redis1", "redis2", "redis3","redis4" };
	Integer[] newweightlist   = { new Integer(1000), new Integer(1000),new Integer(1000),new Integer(1000) };
	
	pool.syncServers( newmachinelist, newweightlist);
	pool.ketamaInfo();

        System.out.println("====Please check output file(java_test.out) to see mapping table between key and host names====");
	
	//output key-host map to file
	BufferedWriter writer = null;
	String text;
	try
	{
    		writer = new BufferedWriter( new FileWriter( "java_test.out"));
		
		for (int i = 1; i<100; i++)
		{
			Integer partialkey = new Integer(i);
			String key = "test_key_"+ partialkey.toString();
			text = pool.getServerForKey(key);
			writer.write(key+" " +pool.getFnv1Hash(key.toCharArray())+" "+text+"\n");
		}

	}		

	catch ( IOException e)
	{ }
	finally{
	  try
   	  {
       		if ( writer != null)
       		writer.close( );
   	  }
   	  catch ( IOException e)
   	  { }
	}
     }
}
