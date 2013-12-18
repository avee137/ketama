import com.wayfair.common.hash.ketama.FNV1A32HashFunction;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class FNV1A32HashFunctionTest {

    private FNV1A32HashFunction function;

    @Before
    public void setup() {
        function = new FNV1A32HashFunction();
    }

    @Test(expected = NullPointerException.class)
    public void testCalculateHashNPE() {
        function.calculateHash(null);
    }

    @Test
    public void testCalculateHash() {
        testHash("FC596F33", "393f7f81-ecd1-4e01-ba24-0d81384a7eb8");
        testHash("9CBA6626", "71918a68-1343-4ffd-8f7b-c7c925a9dc28");
        testHash("D999812E", "1561159a-9ef6-49e1-ab5b-db00db6e6a09");
        testHash("823ED923", "38e6f9df-3932-45a3-8999-78df33423a34");
        testHash("BA5DBD60", "c41d5368-a642-4db6-8125-44844d04aa1e");
    }

    private void testHash(String expectedValue, String input) {
        assertEquals(new BigInteger(expectedValue, 16).longValue(), function.calculateHash(input));
    }

    @Test(expected = NullPointerException.class)
    public void testCalculateHashSeedNPE() {
        function.calculateHash(null, 123);
    }

    @Test
    public void testCalculateHashSeed() {
        assertEquals(2802148759L, function.calculateHash("393f7f81-ecd1-4e01-ba24-0d81384a7eb8", 12345));
        assertEquals(794058721L, function.calculateHash("393f7f81-ecd1-4e01-ba24-0d81384a7eb8", 2802148759L));
        assertEquals(2739688623L, function.calculateHash("393f7f81-ecd1-4e01-ba24-0d81384a7eb8", 794058721L));
        assertEquals(1703322857L, function.calculateHash("393f7f81-ecd1-4e01-ba24-0d81384a7eb8", 2739688623L));
        assertEquals(1880337671L, function.calculateHash("393f7f81-ecd1-4e01-ba24-0d81384a7eb8", 1703322857L));
    }
}
