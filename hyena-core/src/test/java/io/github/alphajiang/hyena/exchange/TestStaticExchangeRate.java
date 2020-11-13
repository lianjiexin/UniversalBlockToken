package io.github.alphajiang.hyena.exchange;

import io.github.alphajiang.hyena.HyenaTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStaticExchangeRate extends HyenaTestBase {

    @Test
    public void test_RMBUBT()
    {
        Assertions.assertEquals(StaticExchangeRate.getExchangeRate("RMB", "UBT"), 0.14286);
    }
}
