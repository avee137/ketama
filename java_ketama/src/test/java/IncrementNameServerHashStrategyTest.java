import com.wayfair.common.hash.ketama.FNV1AServerHashStrategy;
import com.wayfair.common.hash.ketama.IncrementNameServerHashStrategy;
import com.wayfair.common.hash.ketama.NativeHashFunction;
import org.junit.Test;

public class IncrementNameServerHashStrategyTest extends ServerHashStrategyTest<IncrementNameServerHashStrategy> {

    @Override
    public IncrementNameServerHashStrategy getStrategy() {
        return new IncrementNameServerHashStrategy(new NativeHashFunction());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNPE() {
        new IncrementNameServerHashStrategy(null);
    }
}
