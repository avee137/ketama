import com.wayfair.common.hash.ketama.KetamaServer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class KetamaServerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBlankHost() {
        new KetamaServer("a", "", 1234);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBlankName() {
        new KetamaServer("", "test", 1234);
    }

    @Test
    public void testConstructor() {
        KetamaServer server = new KetamaServer("a", "test", 123);
        assertEquals("a", server.getName());
        assertEquals("test", server.getHostname());
        assertEquals(123, server.getPort());
        assertEquals("a: test:123", server.toString());
    }

    @Test
    public void testEquals() {
        KetamaServer server0 = new KetamaServer("a", "test", 123);
        KetamaServer server1 = new KetamaServer("a", "test", 123);
        KetamaServer server2 = new KetamaServer("b", "test", 123);
        KetamaServer server3 = new KetamaServer("a", "tess", 123);
        KetamaServer server4 = new KetamaServer("a", "test", 124);
        assertTrue(server0.equals(server1));
        assertFalse(server0.equals(server2));
        assertFalse(server0.equals(server3));
        assertFalse(server0.equals(server4));

        Map<KetamaServer, KetamaServer> map = new HashMap<KetamaServer, KetamaServer>();
        map.put(server0, server0);
        assertTrue(map.containsKey(server0));
        assertTrue(map.containsKey(server1));
        assertFalse(map.containsKey(server2));
        assertFalse(map.containsKey(server3));
        assertFalse(map.containsKey(server4));
    }

}
