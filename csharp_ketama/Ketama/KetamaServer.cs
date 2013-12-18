using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ketama
{
    /// <summary>
    /// A special class to hold the information behind each server connection
    /// Currently holds:
    /// serverName (string) - the name of the server we're connecting to.
    /// weight     (int)    - a proportional value of how many keys on the continuum this server should be assigned to, relative to other server weights. Default to 1.
    /// factor     (double) - how many actual key replicas there are for a certain server (number calculated based on weight)
    /// hashes     (uint)   - a List of the hashes for a certain IP address
    /// </summary>
    public class KetamaServer
    {

        public string serverName;
        public double factor;
        public int weight;
        public List<uint> hashes;

        /// <summary>
        /// The KetamaServer object contains information about a server's properties.
        /// Actually passing in a weight is optional (will default the server's weight to 1)
        /// </summary>
        /// <param name="serverIP">The given server's IP:port information.</param>
        /// <param name="newServers">An array of strings containing a list of all the redis servers currently in use.</param>
        public KetamaServer(string serverName, int serverWeight = 0)
        {
            int totalWeight = 1; //Change at some point to a less hard coded value (or decide whether or not we need totalWeight at all)
            this.serverName = serverName;
            factor = GetFactor(totalWeight);
            hashes = new List<uint>();
        }

        /// <summary>
        /// **Currently not really implemented, but could be used to calculate a factor for a server to adjust the number of hashes calculated for a given server** 
        /// 
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

}