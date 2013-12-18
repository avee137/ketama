using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

    /// <summary>
    /// A special class to hold the information behind each server connection
    /// Currently holds:
    /// serverIP   (string) - the IP:port of a redis server
    /// serverName (string) - the given redis name for the box. Can be switched over time.
    /// weight     (int)    - a proportional value of how many keys on the continuum this server should be assigned to, relative to other server weights. Default to 1.
    /// factor     (double) - how many actual key replicas there are for a certain server IP:port (number calculated based on weight)
    /// hashes     (uint)   - a List of the hashes for a certain IP address
    /// </summary>
    public class KetamaServer
    {

        public string serverName;
        public string serverIP;
        public double factor;
        public int weight;
        public List<uint> hashes;

        /// <summary>
        /// Takes in a string array of server names.
        /// The server names are pased to create a serverInfo object holding the server's properites,
        /// and then the method adds the serverInfo object to the grand list of server.
        /// Example: Pass in "10.24.16.40:6240/8", where the first part ("10.24.16.40:6240") is the IP:port and the second part ("8") is the weight
        /// The method parses the passed in strong for / where it knows to separate the IP:port and weight
        /// Actually passing in a weight is optional (will default the server's weight to 1)
        /// </summary>
        /// <param name="serverIP">The given server's IP:port information.</param>
        /// <param name="newServers">An array of strings containing a list of all the redis servers currently in use.</param>
        public KetamaServer(string serverIP = "", string serverName = "")
        {
            this.serverIP = serverIP;
            this.serverName = serverName;

            string[] split = serverName.Split(new char[] { '/' });

            if (split.Length > 1 && int.Parse(split[1]) >= 0)
            {
                try
                {
                    weight = int.Parse(split[1]);
                }
                catch (Exception)
                {
                    weight = 1;
                }
            }
            else
            {
                weight = 1;
            }

            int totalWeight = 1; //Change at some point to a less hard coded value (or decide whether or not we need totalWeight at all)
            serverName = split[0];
            factor = GetFactor(totalWeight);
            hashes = new List<uint>();
        }

        /// <summary>
        /// Calculates the 'factor', i.e., number of replicas, of keys a server should have
        /// A server will have multiple keys placed along the continuum so that the keys are more uniformly distributed
        /// </summary>
        /// <param name="totalWeight">A value indicating relatively how many points a server shoudl have over another</param>
        /// <returns>The factor, a value representing how many keys on the continuum should be created for a single server</returns>
        private double GetFactor(int totalWeight)
        {
            return 160; //Can adjust at some point to return a non-hard coded value, but for now simply returns 160 points on the continuum for each server
        }
    }

