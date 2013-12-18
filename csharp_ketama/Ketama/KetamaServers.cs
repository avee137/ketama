using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;


namespace Ketama
{
    public class KetamaServers
    {
        public List<KetamaServer> servers; //An array containing each KetamaServer object we're connected to 

        /// <summary>
        /// Constructor to create the KetamaServers object, which will contain all the servers that are connected to the continuum 
        /// </summary>
        public KetamaServers() 
        {
            servers = new List<KetamaServer>(); 
        }

        /// <summary>
        /// Creating a new KetamaServer object based on the server IP:port/weight information passed in.
        /// The new KetamaServer object will get saved to the list of KetamaServer objects called server
        /// </summary>
        /// <param name="serverName">The name of the server we're adding</param>
        /// <param name="serverWeight">The weight of the server we're adding</param>
        public void AddServerToServers(string serverName, int serverWeight = 0)
        {
            KetamaServer newServer = new KetamaServer(serverName, serverWeight); //Since we're making a new server, we create a new server object.

            servers.Add(newServer); //Add that new server to our list of servers    
         
         }

        /// <summary>
        /// Used to remove a KetamaServer object from the collection of KetamaServer objects.
        /// </summary>
        /// <param name="server">A server name we want to remove</param>
        public void RemoveServerFromServers(string server)
        {        
            KetamaServer serverToRemove = GetServer(server);

            servers.Remove(serverToRemove);
        }

        /// <summary>
        /// Returns a KetamaServer object depending on the name passed in
        /// </summary>
        /// <param name="serverName">A server name</param>
        /// <returns>A KetamaServer object from the servers list</returns>
        public KetamaServer GetServer(string serverName)
        {
            foreach (KetamaServer server in servers)
            {
                if (server.serverName == serverName)
                {
                    return server;
                }
            }

            //If we get here, it means that the server wasn't found and we pass back nothing
            return null;
        }

        /// <summary>
        /// Gets a list of the server names we currently have on the continuum
        /// </summary>
        /// <returns>A List<string> of server names we're currently connected to</returns>
        public List<string> GetServerList()
        {
            List<string> serverList = new List<string>();
            foreach (KetamaServer server in servers)
            {
                serverList.Add(server.serverName);
            }

            return serverList;
        }

    }
}