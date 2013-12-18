using System;
using System.Configuration;
using System.Linq;
using System.Text;

namespace Ketama
{
    public class KetamaHashingAlgorithm_FNV1a32Bit : IKetamaHashingAlgorithm
    {
        #region Constructor

        #region Constants

        //For the source of these numbers, see: http://www.isthe.com/chongo/tech/comp/fnv/index.html#FNV-param
        const uint fnv1a32HashPrime = 16777619;                             //A prime number for 32-bit Fnv1a hash. AKA 0x811C9DC5 in hex.
        const uint fnv1a32HashOffset = 2166136261;                           //The offset basis for a 32-bit Fnv1a hash. AKA 0x01000193 in hex.

        #endregion Constants

        public string hashingAlgorithmName = "FNV1a 32bit";

        /// <summary>
        /// Initializes the hashing algorithm. 
        /// Currently doesn't need anything passed in.
        /// </summary>
        public KetamaHashingAlgorithm_FNV1a32Bit() { }

        #endregion Constructor

        #region Public methods

        /// <summary>
        /// Implementation of a FNV1a algorithm.
        /// For implementation details, see http://home.comcast.net/~bretm/hash/6.html or https://gist.github.com/rasmuskl/3786618
        /// </summary>
        /// <param name="key">A string key to be hashed</param>
        /// <param name="offset">The offset value used to calculate the hash</param>
        /// <returns>The uint hashed value of the passed-in key</returns>
        public uint GetHashFromString(string key, uint offset = fnv1a32HashOffset)
        {
            byte[] inputBytes = System.Text.Encoding.ASCII.GetBytes(key);
            uint keyHash = offset;

            foreach (byte individualByte in inputBytes)
            {
                keyHash ^= (individualByte);
                keyHash *= fnv1a32HashPrime;
            }
            return keyHash;
        }
    }
        #endregion Public methods
}