import com.wayfair.common.hash.ketama.KetamaServer;
import com.wayfair.common.hash.ketama.ServerHashStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public abstract class ServerHashStrategyTest<T extends ServerHashStrategy> {

    private T strategy;

    public abstract T getStrategy();

    @Before
    public void setup() {
        strategy = getStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void testAddServerNPE() {
        strategy.addServer(null, new TreeMap<Long, KetamaServer>(), 10);
    }

    @Test(expected = NullPointerException.class)
    public void testAddServerNPE2() {
        strategy.addServer(randomServer(), null, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddServerZeroPoints() {
        strategy.addServer(randomServer(), new TreeMap<Long, KetamaServer>(), 0);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveServerNPE() {
        strategy.removeServer(null, new TreeMap<Long, KetamaServer>(), 10);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveServerNPE2() {
        strategy.removeServer(randomServer(), null, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveServerZeroPoints() {
        strategy.removeServer(randomServer(), new TreeMap<Long, KetamaServer>(), 0);
    }

    @Test
    public void testAddRemoveServer() {
        TreeMap<Long, KetamaServer> map = new TreeMap<Long, KetamaServer>();
        KetamaServer server0 = randomServer();
        KetamaServer server1 = randomServer();

        strategy.addServer(server0, map, 10);
        assertEquals(10, map.size());
        assertServerCount(10, server0, map);

        // The server will map to the same 10 points, making no different in the map
        strategy.addServer(server0, map, 10);
        assertEquals(10, map.size());
        assertServerCount(10, server0, map);

        strategy.addServer(server1, map, 10);
        assertEquals(20, map.size());
        assertServerCount(10, server1, map);

        strategy.removeServer(server1, map, 10);
        assertEquals(10, map.size());
        assertServerCount(10, server0, map);
        assertServerCount(0, server1, map);

        strategy.removeServer(server0, map, 10);
        assertEquals(0, map.size());
        assertServerCount(0, server0, map);
        assertServerCount(0, server1, map);
    }

    private void assertServerCount(int expectedCount, KetamaServer server, Map<Long, KetamaServer> map) {
        int count = 0;
        for (KetamaServer s : map.values()) {
            if (s.equals(server)) {
                count++;
            }
        }

        assertEquals(expectedCount, count);
    }

    private KetamaServer randomServer() {
        return new KetamaServer(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1234);
    }
}
