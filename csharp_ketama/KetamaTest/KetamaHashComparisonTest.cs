using System;
using System.IO;
using Ketama;
using System.Collections.Generic;
using System.Diagnostics;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace KetamaTest
{
    //Run this test when you want to make sure the ketama library is working correctly.
    //Reads server information from file ../../../../ketama.servers
    //Writes the resulting nodes in the form of "{server} - {key}" to file ../../../../csharp_test.out
    [TestClass]
    public class KetamaHashComparisonTest
    {

        [TestMethod]
        public void HashComparisonTest()
        {
            string servers = "";

            using (StreamReader reader = new StreamReader("../../../../ketama.servers"))
            {
                string line;

                while ((line = reader.ReadLine()) != null)
                {
                    servers = string.Concat(servers, ",", line);
                }

                servers = servers.Substring(1); //Remove the comma at the head of the string, a side effect of the concatination 
            }

            if (servers == "")
            {
                Assert.Fail("No servers provided.");
            }

            KetamaContinuum ketamaContinuum = new KetamaContinuum(servers);
            List<uint> hashes = ketamaContinuum.GetAllHashes();

            string key;
            uint hash;
            KetamaHashingAlgorithm_FNV1a32Bit ketamaHashingAlgorithm = new KetamaHashingAlgorithm_FNV1a32Bit();

            using (StreamWriter writer = new StreamWriter("../../../../csharp_test.out"))
            {
                for (int i = 0; i < 100; i++)
                {
                    key = "aab" + i.ToString();

                    hash = (uint)ketamaHashingAlgorithm.GetHashFromString(key);

                    Debug.WriteLine(ketamaContinuum.FindServerForKey(key) + " - " + key);
                    writer.WriteLine(ketamaContinuum.FindServerForKey(key) + " - " + key);
                }
            }
        }
    }
}


