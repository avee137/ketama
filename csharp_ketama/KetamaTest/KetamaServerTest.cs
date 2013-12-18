using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Ketama;
using System.Collections.Generic;
using System.Diagnostics;

namespace Ketama.Tests
{
    [TestClass]
    public class KetamaServerTest
    {
        string servers;
        string serverList;
        KetamaContinuum ketamaContinuum;

        [TestInitialize()]
        public void Initialize()
        {
            servers = "10.10.10.10:6379,20.20.20.20:6379,30.30.30.30:6379,40.40.40.40:6379";
            ketamaContinuum = new KetamaContinuum(servers);
            serverList = ketamaContinuum.ServerToString();
            Console.WriteLine(serverList);
        }

        [TestMethod]
        //Confirms that you can add a list of servers to the continuum and that they get saved/set properly to it
        public void GetListOfKetamaServers()
        {
            Assert.AreEqual("10.10.10.10:6379,20.20.20.20:6379,30.30.30.30:6379,40.40.40.40:6379", serverList);
        }

        [TestMethod]
        //Confirms that the key finds the correct host
        public void FindCorrectServerForKey()
        {
            string key = "stevieweevie";
            string serverForKey = ketamaContinuum.FindServerForKey(key);
            Assert.AreEqual(serverForKey, "10.10.10.10:6379");
        }

        [TestMethod]
        //Confirms that a value is hashed to a certain server, then if we remove that server the value 
        //then hashes to a new server (i.e. the server that's the next highest numerically)
        public void RemovingServerForAKeyChangesTheServerTheKeyHashesTo()
        {
            string key = "stevieweevie";
            string serverForKey = ketamaContinuum.FindServerForKey(key);
            Assert.AreEqual(serverForKey, "10.10.10.10:6379"); //same up to this point as the as 'FindCorrectServerForKey' test

            ketamaContinuum.RemoveServerConnection("10.10.10.10:6379");

            serverForKey = ketamaContinuum.FindServerForKey(key);
            Assert.AreEqual(serverForKey, "20.20.20.20:6379");
        }

        [TestMethod]
        //Confirms that the sync server function is working
        public void ConfirmSyncKetamaServerWorks()
        {
            string servers = "10.10.10.10:6379";
            KetamaContinuum ketamaContinuum = new KetamaContinuum(servers);
            serverList = ketamaContinuum.ServerToString();
            Assert.AreEqual("10.10.10.10:6379", serverList);

            ketamaContinuum.SyncServerConections("10.10.10.10:6379,20.20.20.20:6379");
            serverList = ketamaContinuum.ServerToString();
            Assert.AreEqual("10.10.10.10:6379,20.20.20.20:6379", serverList);

            ketamaContinuum.SyncServerConections("10.10.10.10:6379,20.20.20.20:6379,30.30.30.30:6379,40.40.40.40:6379");
            serverList = ketamaContinuum.ServerToString();
            Assert.AreEqual("10.10.10.10:6379,20.20.20.20:6379,30.30.30.30:6379,40.40.40.40:6379", serverList);

            ketamaContinuum.SyncServerConections("10.10.10.10:6379");
            serverList = ketamaContinuum.ServerToString();
            Assert.AreEqual("10.10.10.10:6379", serverList);
        }

    }
}