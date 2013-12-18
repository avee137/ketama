import com.wayfair.common.hash.ketama.FNV1AServerHashStrategy;

public class FNV1AServerStrategyTest extends ServerHashStrategyTest<FNV1AServerHashStrategy> {

    @Override
    public FNV1AServerHashStrategy getStrategy() {
        return new FNV1AServerHashStrategy();
    }
}
