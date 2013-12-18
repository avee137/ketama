using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ketama
{
    public class KetamaServers
    {
        public List<KetamaServer> servers;                                            //An array containing each KetamaServer object we're connected to 

        private Dictionary<string, string> serverNames;                               //A dictionary to hold the association between server name (e.g. redis1) and IP address (e.g. 10.10.10.10:6374). 
                                                                                      //Example, something like < {redis1, 10.10.10.10:1234} {redis2, 20.20.20.20:2345} >                                                          
                                                                                      //Used if we want to dynamically change the server name and IPs among each other.
        /// <summary>
        /// Constructor to create the KetamaServers object, which will contain all the servers that are connected to the continuum 
        /// </summary>
        public KetamaServers() 
        {
            servers = new List<KetamaServer>(); 
            serverNames = new Dictionary<string, string>();
        }

        /// <summary>
        /// Creating a new KetamaServer object based on the server IP:port/weight information passed in.
        /// The new KetamaServer object will get saved to the list of KetamaServer objects called server
        /// </summary>
        /// <param name="serverIP">A server IP:port/weight value</param>
        public void AddServerToServers(string serverIP)
        {
            string serverRedisName = "redis" + (serverNames.Count() + 1);             //Should resolve to some name like 'redis1' or 'redis2', etc.
            KetamaServer newServer = new KetamaServer(serverIP, serverRedisName);     //Since we're making a new server, we create a new server object. Pass in the serverIP (e.g. '10.10.10.10:1234') and the given redis name (e.g. 'redis1')
            
            servers.Add(newServer);                                                   //Add that new server to our list of servers

            serverNames.Add(serverRedisName, newServer.serverIP);                   //Setting the server name to redisX (where X is an integer) and having it be associated with the IP address
         }

        /// <summary>
        /// Used to remove a KetamaServer object from the collection of KetamaServer objects.
        /// </summary>
        /// <param name="serverIP">A server IP:port/weight value</param>
        public void RemoveServerFromServers(string serverIP)
        {
            KetamaServer serverToRemove = GetKetamaServerByIP(serverIP);

            servers.Remove(serverToRemove);

            string serverName = GetServerNameFromIP(serverIP);
            serverNames.Remove(serverName);
        }
        
        /// <summary>
        /// //Looks up in the Dictionary<string, string> that has entries like {'redis1', '10.10.10.10:1234'} 
        /// and returns the IP address to get connected
        /// </summary>
        /// <param name="serverName">The serverName (e.g. 'redis1')</param>
        /// <returns>The IP address currently associated with the redis name</returns>
        public string GetServerIPFromServerName(string serverName)
        {
            return serverNames[serverName]; 
        }

        /// <summary>
        /// //Looks up in the Dictionary<string, string> that has entries like {'redis1', '10.10.10.10:1234'} 
        /// and returns the server name from the IP string passed in
        /// </summary>
        /// <param name="serverIP">The serverIP (e.g. 'redis1')</param>
        /// <returns>The server name associated with that server IP</returns>
        public string GetServerNameFromIP(string serverIP)
        {
            foreach (KeyValuePair<string, string> keyValuePair in serverNames)
            {
                if (keyValuePair.Value == serverIP)
                {
                    return keyValuePair.Key;            
                }
            } 
           //if it wasn't found, we just return a default name 'redis'
           return "redis";
        }

        /// <summary>
        /// Returns a KetamaServer object depending on the server IP:port/weight string passed ins
        /// <param name="serverName">A server IP:port string (e.g. '10.10.10.10:6245') which should already
        /// be stored in the List of serverInfo objects called servers</param>
        /// <returns>A KetamaServer object from the servers list
        /// If there's no serverInfo object found for that servername, passes back an empty KetamaServer object</returns>
        public KetamaServer GetKetamaServerByIP(string serverIP)
        {
            foreach (KetamaServer server in servers)
            {
                if (server.serverIP == serverIP)
                {
                    return server;
                }
            }
            //If we get here, it means that the server wasn't found.
            //We will return a new and empty KetamaServer object instead
            KetamaServer newServer = new KetamaServer();
            return newServer;
        }

        /// <summary>
        /// Returns a KetamaServer object depending on the name (e.g. 'redis1') passed in
        /// </summary>
        /// <param name="serverName">A server name</param>
        /// <returns>A KetamaServer object from the servers list</returns>
        public KetamaServer GetKetamaServerByName(string serverName)
        {
            foreach (KetamaServer server in servers)
            {
                if (server.serverName == serverName)
                {
                    return server;
                }
            }
            //If we get here, it means that the server wasn't found.
            //We will return a new and empty KetamaServer object instead
            KetamaServer newServer = new KetamaServer();
            return newServer;
        }

        /// <summary>
        /// Gets a list of the server names we currently have on the continuum
        /// </summary>
        /// <returns>A List<string> of server names we're currently connected to</returns>
        public List<string> GetServerNameList()
        {
            List<string> serverList = new List<string>();
            foreach (KetamaServer server in servers)
            {
                serverList.Add(server.serverName);
            }

            return serverList;
        }

        /// <summary>
        /// Gets a list of the server names we currently have on the continuum
        /// </summary>
        /// <returns>A List<string> of server names we're currently connected to</returns>
        public List<string> GetServerIPList()
        {
            List<string> serverList = new List<string>();
            foreach (KetamaServer server in servers)
            {
                serverList.Add(server.serverIP);
            }

            return serverList;
        }
    }
}