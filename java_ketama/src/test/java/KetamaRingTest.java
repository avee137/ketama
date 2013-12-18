import com.google.common.collect.Sets;
import com.wayfair.common.hash.ketama.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class KetamaRingTest {

    private ServerHashStrategy strategy;
    private Set<KetamaServer> servers;
    private HashFunction hashFunction;

    @Before
    public void setup() {
        strategy = new FNV1AServerHashStrategy();
        servers = new HashSet<KetamaServer>();
        servers.add(randomServer());
        hashFunction = new FNV1A32HashFunction();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNPE() {
        new KetamaRing(null, hashFunction, servers);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNPE2() {
        new KetamaRing(strategy, null, servers);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNPE3() {
        new KetamaRing(strategy, hashFunction, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNPE4() {
        new KetamaRing(null, hashFunction);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNPE5() {
        new KetamaRing(strategy, null);
    }

    @Test
    public void testConstructor() {
        Set<KetamaServer> servers = new HashSet<KetamaServer>();
        servers.add(randomServer());
        servers.add(randomServer());
        KetamaRing ring = new KetamaRing(strategy, hashFunction, servers);

        for (KetamaServer server : servers) {
            assertTrue(ring.contains(server));
        }

        assertEquals(servers.size(), ring.getServers().size());
        assertEquals(0, Sets.difference(servers, ring.getServers()).size());
    }

    @Test
    public void testConstructorEmpty() {
        KetamaRing ring = new KetamaRing(strategy, hashFunction);
        assertEquals(0, ring.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNPE() {
        ring(1).add((KetamaServer) null);
    }

    @Test
    public void testAdd() throws InterruptedException {
        KetamaRing ring = ring(1);
        KetamaServer server = randomServer();

        DateTime modifiedDate = ring.getModifiedDate();
        assertFalse(ringHasServer(ring, server));

        Thread.sleep(1);
        ring.add(server);
        assertTrue(ring.getModifiedDate().getMillis() > modifiedDate.getMillis());
        assertEquals(2, ring.getServers().size());
        assertTrue(ringHasServer(ring, server));

        // Verify nothing changes if you try to add it again
        modifiedDate = ring.getModifiedDate();
        Thread.sleep(1);
        ring.add(server);
        assertTrue(ring.getModifiedDate().getMillis() == modifiedDate.getMillis());
        assertEquals(2, ring.getServers().size());
        assertTrue(ringHasServer(ring, server));
    }

    @Test(expected = NullPointerException.class)
    public void testAddSetNPE() {
        ring(1).add((Set<KetamaServer>) null);
    }

    @Test
    public void testAddSet() throws InterruptedException {
        KetamaRing ring = ring(1);
        Set<KetamaServer> servers = new HashSet<KetamaServer>();
        KetamaServer toAdd0 = randomServer();
        KetamaServer toAdd1 = randomServer();
        servers.add(toAdd0);
        servers.add(toAdd1);

        DateTime modifiedDate = ring.getModifiedDate();
        for (KetamaServer server : servers) {
            assertFalse(ringHasServer(ring, server));
        }

        Thread.sleep(1);
        ring.add(servers);
        assertTrue(ring.getModifiedDate().getMillis() > modifiedDate.getMillis());
        assertEquals(3, ring.getServers().size());
        assertTrue(ringHasServer(ring, toAdd0));
        assertTrue(ringHasServer(ring, toAdd1));

        // Verify nothing changes if you try to add them again
        modifiedDate = ring.getModifiedDate();
        Thread.sleep(1);
        ring.add(servers);
        assertTrue(ring.getModifiedDate().getMillis() == modifiedDate.getMillis());
        assertEquals(3, ring.getServers().size());
        assertTrue(ringHasServer(ring, toAdd0));
        assertTrue(ringHasServer(ring, toAdd1));
    }

    @Test(expected = NullPointerException.class)
    public void RemoveNPE() {
        ring(1).remove((KetamaServer) null);
    }

    @Test
    public void testRemove() throws InterruptedException {
        KetamaRing ring = ring(1);
        KetamaServer toRemove = randomServer();
        KetamaServer toKeep = ring.get("a");
        ring.add(toRemove);
        ring.add(toKeep);

        DateTime modifiedDate = ring.getModifiedDate();
        assertTrue(ring.contains(toKeep));
        assertTrue(ring.contains(toRemove));

        Thread.sleep(1);
        ring.remove(toRemove);
        assertTrue(ring.getModifiedDate().getMillis() > modifiedDate.getMillis());
        assertEquals(1, ring.getServers().size());
        assertFalse(ringHasServer(ring, toRemove));
        assertTrue(ringHasServer(ring, toKeep));

        // Verify nothing changes if you try to remove it again
        modifiedDate = ring.getModifiedDate();
        Thread.sleep(1);
        ring.remove(toRemove);
        assertTrue(ring.getModifiedDate().getMillis() == modifiedDate.getMillis());
        assertEquals(1, ring.getServers().size());
        assertFalse(ringHasServer(ring, toRemove));
        assertTrue(ringHasServer(ring, toKeep));
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveSetNPE() {
        ring(1).remove((Set<KetamaServer>) null);
    }

    @Test
    public void testRemoveSet() throws InterruptedException {
        KetamaRing ring = ring(1);
        KetamaServer toRemove0 = randomServer();
        KetamaServer toRemove1 = randomServer();
        KetamaServer toKeep = ring.get("a");
        ring.add(toRemove0);
        ring.add(toRemove1);
        ring.add(toKeep);

        DateTime modifiedDate = ring.getModifiedDate();
        assertTrue(ring.contains(toKeep));
        assertTrue(ring.contains(toRemove0));
        assertTrue(ring.contains(toRemove1));

        Thread.sleep(1);
        ring.remove(new HashSet<KetamaServer>(Arrays.asList(toRemove0, toRemove1)));
        assertTrue(ring.getModifiedDate().getMillis() > modifiedDate.getMillis());
        assertEquals(1, ring.getServers().size());
        assertFalse(ringHasServer(ring, toRemove0));
        assertFalse(ringHasServer(ring, toRemove1));
        assertTrue(ringHasServer(ring, toKeep));

        // Verify nothing changes if you try to remove it again
        modifiedDate = ring.getModifiedDate();
        Thread.sleep(1);
        ring.remove(new HashSet<KetamaServer>(Arrays.asList(toRemove0, toRemove1)));
        assertTrue(ring.getModifiedDate().getMillis() == modifiedDate.getMillis());
        assertEquals(1, ring.getServers().size());
        assertFalse(ringHasServer(ring, toRemove0));
        assertFalse(ringHasServer(ring, toRemove1));
        assertTrue(ringHasServer(ring, toKeep));
    }

    @Test
    public void testContains() {
        KetamaRing ring = new KetamaRing(strategy, hashFunction);
        KetamaServer server0 = randomServer();
        KetamaServer server1 = randomServer();

        assertFalse(ring.contains(server0));
        assertFalse(ring.contains(server1));

        ring.add(server0);

        assertTrue(ring.contains(server0));
        assertFalse(ring.contains(server1));

        ring.add(server1);

        assertTrue(ring.contains(server0));
        assertTrue(ring.contains(server1));
    }

    @Test(expected = NullPointerException.class)
    public void testContainsNPE() {
        ring(1).contains(null);
    }

    @Test(expected =  NullPointerException.class)
    public void testSynchronizeNPE() {
        ring(1).synchronizeRing(null);
    }

    @Test
    public void testSynchronize() {
        KetamaRing ring = ring(2);
        assertEquals(2, ring.size());

        Set<KetamaServer> servers = new HashSet<KetamaServer>();
        KetamaServer server0 = randomServer();
        KetamaServer server1 = randomServer();
        servers.add(server0);
        servers.add(server1);

        assertFalse(ringHasServer(ring, server0));
        assertFalse(ringHasServer(ring, server1));

        ring.synchronizeRing(servers);
        assertEquals(2, ring.size());
        assertTrue(ringHasServer(ring, server0));
        assertTrue(ringHasServer(ring, server1));

        ring.synchronizeRing(servers);
        assertEquals(2, ring.size());
        assertTrue(ringHasServer(ring, server0));
        assertTrue(ringHasServer(ring, server1));
    }

    @Test
    public void testSynchronizeEmpty() {
        KetamaRing ring = ring(2);
        assertEquals(2, ring.size());

        Set<KetamaServer> servers = Collections.emptySet();
        ring.synchronizeRing(servers);
        assertEquals(0, ring.size());
    }

    @Test(expected = NullPointerException.class)
    public void testGetNPE() {
        ring(1).get(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNoSuchElement() {
        ring(0).get("test");
    }

    @Test
    public void testGet() {
        KetamaRing ring = ring(0);
        KetamaServer serverA = new KetamaServer("serverA", "serverA", 1234);
        KetamaServer serverB = new KetamaServer("serverB", "serverA", 5678);
        KetamaServer serverC = new KetamaServer("serverC", "serverC", 5678);
        ring.add(serverA);
        ring.add(serverB);
        ring.add(serverC);

        assertEquals(serverB, ring.get("test0"));
        assertEquals(serverA, ring.get("test1"));
        assertEquals(serverB, ring.get("test2"));
        assertEquals(serverB, ring.get("test3"));
        assertEquals(serverB, ring.get("test4"));
        assertEquals(serverB, ring.get("test5"));
        assertEquals(serverC, ring.get("test6"));
        assertEquals(serverC, ring.get("test7"));
        assertEquals(serverB, ring.get("test8"));
        assertEquals(serverA, ring.get("test9"));
        assertEquals(serverA, ring.get("test10"));
        assertEquals(serverB, ring.get("test11"));
        assertEquals(serverB, ring.get("test12"));
        assertEquals(serverB, ring.get("test13"));
        assertEquals(serverB, ring.get("test14"));
        assertEquals(serverB, ring.get("test15"));
        assertEquals(serverC, ring.get("test16"));
        assertEquals(serverC, ring.get("test17"));
        assertEquals(serverA, ring.get("test18"));
        assertEquals(serverA, ring.get("test19"));
    }

    @Test
    public void testGet2() {
        KetamaRing ring = ring(0);
        KetamaServer node1 = new KetamaServer("node1", "a", 1234);
        KetamaServer node2 = new KetamaServer("node2", "b", 1234);
        KetamaServer node3 = new KetamaServer("node3", "c", 1234);
        KetamaServer node4 = new KetamaServer("node4", "c", 1234);
        ring.add(node1);
        ring.add(node2);
        ring.add(node3);
        ring.add(node4);

        KetamaServer[] expected = {
                node3,
                node1,
                node2,
                node1,
                node1,
                node2,
                node2,
                node1,
                node4,
                node4,
                node3,
                node1,
                node2,
                node2,
                node2,
                node3,
                node4,
                node3,
                node4,
                node1,
                node3,
                node1,
                node4,
                node2,
                node2,
                node3,
                node1,
                node3,
                node4,
                node1,
                node3,
                node2,
                node4,
                node2,
                node2,
                node1,
                node4,
                node3,
                node3,
                node3,
                node3,
                node3,
                node2,
                node1,
                node4,
                node1,
                node3,
                node3,
                node1,
                node2,
                node3,
                node2,
                node1,
                node3,
                node4,
                node2,
                node1,
                node2,
                node3,
                node1,
                node4,
                node1,
                node3,
                node2,
                node1,
                node3,
                node2,
                node3,
                node3,
                node1,
                node2,
                node1,
                node1,
                node2,
                node3,
                node1,
                node4,
                node3,
                node1,
                node3,
                node2,
                node3,
                node4,
                node2,
                node4,
                node4,
                node1,
                node3,
                node1,
                node1,
                node4,
                node4,
                node1,
                node3,
                node1,
                node3,
                node4,
                node3,
                node4,
                node1
        };

        for (int i = 0 ; i < 100 ; i++) {
           assertEquals(expected[i], ring.get("aab" + i));
        }
    }

    /**
     * Returns true if the ring contains the server and the server can be found. Throws an exception if one is true
     * and the other false. Returns false otherwise.
     */
    private boolean ringHasServer(KetamaRing ring, KetamaServer server) {
        boolean contains = ring.contains(server);

        boolean found = false;
        for (int i = 0 ; i < 200 ; i++) {
            if (server.equals(ring.get(UUID.randomUUID().toString()))) {
                found = true;
                break;
            }
        }

        if (contains && found) {
            return true;
        } else if (!contains && !found) {
            return false;
        } else if (contains && !found) {
            throw new RuntimeException("The ring claims the server IS in the ring, but it was NOT found.");
        } else {
            throw new RuntimeException("The ring claims the server is NOT in the ring, but it WAS found.");
        }
    }

    private KetamaServer randomServer() {
        return new KetamaServer(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1234);
    }

    private KetamaRing ring(int serverCount) {
        Set<KetamaServer> servers = new HashSet<KetamaServer>();
        for (int i = 0 ; i < serverCount ; i++) {
            servers.add(randomServer());
        }

        return new KetamaRing(new FNV1AServerHashStrategy(), hashFunction, servers);
    }

}
