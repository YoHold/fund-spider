package com.yochiu.fund.until;

/**
 *
 */

import com.yochiu.fund.common.CommonConsts;

/**
 *
 * @author yochiu
 *
 */
public class SymbolUtil {

    public static final String MARKET_POSTFIX_SH = ".SH";

    public static String getMarketLabelBySymbol(String symbol) {
        if (symbol != null && symbol.matches("[0-9]+")) {
            if (symbol.length() >= 6) {
                return getCnMarket(symbol);
            }
        }
        return "";
    }

    public static boolean isCnIndice(String symbol) {
        if(symbol != null && symbol.length() >= 6) {
            return symbol.startsWith("39") || symbol.endsWith(MARKET_POSTFIX_SH);
        }
        return false;
    }

    public static boolean isCnEtf(String symbol) {
        if (symbol != null && symbol.length() >= 6) {
            if(symbol.startsWith("15") || symbol.startsWith("16") || symbol.startsWith("18")
                    || symbol.startsWith("50") || symbol.startsWith("51") || symbol.startsWith("52")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidCnMarket(String market) {
        if (CommonConsts.MARKET_SZ.equalsIgnoreCase(market) || CommonConsts.MARKET_SH.equalsIgnoreCase(market)) {
            return true;
        }
        return false;
    }


    public static String getSecMarketBySymbol(String symbol) {
        if (symbol != null) {
            return getMarketLabelBySymbol(symbol);
        }
        return null;
    }


    /**
     * 判断symbol是否属于A股市场
     * @param symbol
     * @return
     */
    public static boolean isCnMarket(String symbol) {
        return isValidCnMarket(getSecMarketBySymbol(symbol));
    }


    /**
     * A股:根据id前缀判断市场类型(id为symbol或者stockId)
     * @param id
     * @return 未匹配成功，默认返回SH
     */
    private static String getCnMarket(String id) {
        if (id.startsWith("00")
                || id.startsWith("30")
                || id.startsWith("39")
                || id.startsWith("15")
                || id.startsWith("16")
                || id.startsWith("18")) {
            return CommonConsts.MARKET_SZ;
        } else if (id.startsWith("60")
                || id.startsWith("688")
                || id.startsWith("50")
                || id.startsWith("51")
                || id.startsWith("52")) {
            return CommonConsts.MARKET_SH;
        }

        return CommonConsts.MARKET_SH;
    }

    public static String getStockId(String symbol) {
        String market = getMarketLabelBySymbol(symbol);
        return market + symbol;
    }

    /**
     * 判断symbol是否为科创板股票代码
     *
     * @param symbol
     * @return
     */
    public static boolean isKcbSymbol(String symbol) {
        return symbol != null && symbol.startsWith("688") && isCnMarket(symbol);
    }

    /**
     * 判断symbol是否为创业版股票代码
     * @param symbol
     * @return
     */
    public static boolean isGemSymbol(String symbol) {
        return symbol != null && symbol.startsWith("30") && isCnMarket(symbol);
    }

}
