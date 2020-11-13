package io.github.alphajiang.hyena.exchange;

public class StaticExchangeRate {

    public static double getExchangeRate(String from, String to) {
        return SupportedRatePairs.getRate(from, to);
    }
}
