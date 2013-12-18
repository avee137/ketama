using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Ketama;
using System.Collections.Generic;
using System.Diagnostics;

namespace Ketama.Tests
{
    [TestClass]
    public class KetamaHashTest
    {

        [TestMethod]
        //Confirms that the numeric hash value does not depend on the number of times the value has been hashed
        public void ConfirmHashingToCorrectValues()
        {
            string servers = "10.10.10.10:6379,20.20.20.20:6379,30.30.30.30:6379,40.40.40.40:6379";
            KetamaContinuum ketamaContinuum = new KetamaContinuum(servers);
            KetamaHashingAlgorithm_FNV1a32Bit ketamaHashingAlgorithm = new KetamaHashingAlgorithm_FNV1a32Bit();

            string key = "ThisIsTheBestHash!!!!";
            string server = ketamaContinuum.FindServerForKey(key);
            uint hash = ketamaHashingAlgorithm.GetHashFromString(key);
            //Console.WriteLine("Hash value for {0} is: {1}, which is found on server: {2}", key, hash, server);
            Assert.AreEqual((uint)hash, (uint)1916906286);
            hash = ketamaHashingAlgorithm.GetHashFromString(key);
            Assert.AreEqual((uint)hash, (uint)1916906286); //Doing it twice to ensure hashing the same string multiple times isn't coming up with different results

            key = "thisistheWORSTHASH:( :( :(";
            server = ketamaContinuum.FindServerForKey(key);
            hash = ketamaHashingAlgorithm.GetHashFromString(key);
            //Console.WriteLine("Hash value for {0} is: {1}, which is found on server: {2}", key, hash, server);
            Assert.AreEqual((uint)hash, (uint)3993883897);
            hash = ketamaHashingAlgorithm.GetHashFromString(key);
            Assert.AreEqual((uint)hash, (uint)3993883897); //Doing it twice to ensure hashing the same string multiple times isn't coming up with different results

            key = "Stevieweevie";
            server = ketamaContinuum.FindServerForKey(key);
            hash = ketamaHashingAlgorithm.GetHashFromString(key);
            //Console.WriteLine("Hash value for {0} is: {1}, which is found on server: {2}", key, hash, server);
            Assert.AreEqual((uint)hash, (uint)3692210458);
            hash = ketamaHashingAlgorithm.GetHashFromString(key);
            Assert.AreEqual((uint)hash, (uint)3692210458); //Doing it twice to ensure hashing the same string multiple times isn't coming up with different results
        }

        //[TestMethod] //Uncomment to run a timing test (i.e., see how long it takes for all this to run)
        public void HashTimingTest()
        {
            KetamaHashingAlgorithm_FNV1a32Bit ketamaHashingAlgorithm = new KetamaHashingAlgorithm_FNV1a32Bit();
            Stopwatch totalTime = new Stopwatch();

            string stringKey = "test_string_value";

            for (int total = 10; total <= 10000; total *= 10)
            {
                totalTime.Reset();
                totalTime.Start();
                for (int i = 0; i < total; i++)
                {
                    ketamaHashingAlgorithm.GetHashFromString(stringKey + "_" + i);
                }
                totalTime.Stop();
                Debug.WriteLine("Time for " + total + " hashes: " + totalTime.ElapsedMilliseconds.ToString());

            }
        }
    }
}