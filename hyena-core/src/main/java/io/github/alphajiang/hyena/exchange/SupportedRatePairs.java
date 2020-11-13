package io.github.alphajiang.hyena.exchange;

import io.github.alphajiang.hyena.utils.StringUtils;

public enum SupportedRatePairs {
    UBTRMB,
    UBTUSD,
    RMBUBT,
    USDUBT;

    public static SupportedRatePairs getSymbol (String from, String to)
    {
        if (StringUtils.equals(from.toUpperCase(),"UBT") && StringUtils.equals(to.toUpperCase(),"RMB"))
            return UBTRMB;
        if (StringUtils.equals(from.toUpperCase(),"RMB") && StringUtils.equals(to.toUpperCase(),"UBT"))
            return RMBUBT;
        if (StringUtils.equals(from.toUpperCase(),"UBT") && StringUtils.equals(to.toUpperCase(),"USD"))
            return UBTUSD;
        if (StringUtils.equals(from.toUpperCase(),"USD") && StringUtils.equals(to.toUpperCase(),"UBT"))
          return USDUBT;

        return null;
    }

    /**
     * Default rate is 1.00
     * @param symbol
     * @return
     */
    public static double getRate(SupportedRatePairs symbol){
        switch(symbol){
            case UBTRMB : return 7.00;
            case UBTUSD : return 1.09;
            case RMBUBT : return 0.14286;
            case USDUBT: return 0.91743;
            default: return 1.00;
        }

    }

    public static double getRate(String from, String to)
    {
        return SupportedRatePairs.getRate(SupportedRatePairs.getSymbol(from,to));
    }

}
