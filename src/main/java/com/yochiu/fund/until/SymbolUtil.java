package com.yochiu.fund.until;

/**
 * @(#)SymbolUtil.java, 2015年1月2日.
 *
 * Copyright 2015 TigerBrokers, Inc. All rights reserved.
 * Tiger PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import com.yochiu.fund.common.CommonConsts;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author cheneg
 *
 */
public class SymbolUtil {

    public static final String MARKET_POSTFIX_US = ".US";

    public static final String MARKET_POSTFIX_HK = ".HK";

    public static final String MARKET_POSTFIX_SH = ".SH";

    public static final String MARKET_POSTFIX_UK = ".UK";

    public static final String MARKET_POSTFIX_SZ = ".SZ";

    public static final String MARKET_POSTFIX_NZ = ".NZ";

    public static final String MARKET_POSTFIX_SI = ".SI";

    public static final String FUT_MAIN_POSTFIX = "main";

    public static final String[] US_INDICE_ALIAS = new String[]{".DJI", ".IXIC", ".INX"};
    public static final String[] US_INDICE_NAMES = new String[]{"道琼斯", "纳斯达克", "标普500"};
    public static final String[] US_INDICE_NAMES_EN = new String[]{"DJIA", "NASDAQ", "S&P500"};
    public static final List<String> US_INDICE_ALIAS_LIST = Arrays.asList(US_INDICE_ALIAS);

    public static final String[] HK_INDICE_ALIAS = new String[]{"HSI", "HSCEI", "HSCCI"};
    public static final String[] HK_INDICE_NAMES = new String[]{"恒生指数", "国企指数", "红筹指数"};
    public static final String[] HK_INDICE_NAMES_EN = new String[]{"HSI", "HSCEI", "HSCCI"};
    public static final List<String> HK_INDICE_ALIAS_LIST = Arrays.asList(HK_INDICE_ALIAS);

    public static final String[] UK_INDICE_ALIAS = new String[]{".UKX.UK", ".ASX.UK", ".AIM5.UK"};
    public static final String[] UK_INDICE_NAMES = new String[]{"富时100指数", "富时全股指数", "富时AIM英国50指数"};
    public static final String[] UK_INDICE_NAMES_EN = new String[]{"FTSE 100", "FTSE All Share", "FTSE AIM UK 50"};
    public static final List<String> UK_INDICE_ALIAS_LIST = Arrays.asList(UK_INDICE_ALIAS);

    public static final String[] NZ_INDICE_ALIAS = new String[]{};
    public static final String[] NZ_INDICE_NAMES = new String[]{};
    public static final String[] NZ_INDICE_NAMES_EN = new String[]{};
    public static final List<String> NZ_INDICE_ALIAS_LIST = Arrays.asList(NZ_INDICE_ALIAS);

    public static final String[] SI_INDICE_ALIAS = new String[]{};
    public static final String[] SI_INDICE_NAMES = new String[]{};
    public static final String[] SI_INDICE_NAMES_EN = new String[]{};
    public static final List<String> SI_INDICE_ALIAS_LIST = Arrays.asList(SI_INDICE_ALIAS);

    public static final String[] CN_INDICE_ALIAS = new String[]{"000001.SH", "399001", "399006"};
    public static final String[] CN_INDICE_NAMES = new String[]{"上证指数", "深证成指", "创业板指"};
    public static final String[] CN_INDICE_NAMES_EN = new String[]{"SSE Comp", "SZSE Comp", "ChiNext"};
    public static final List<String> CN_INDICE_ALIAS_LIST = Arrays.asList(CN_INDICE_ALIAS);

    public static final List<String> CN_INDICE_MORE_LIST = Arrays.asList("000001.SH", "399001", "399006",
            "000300.SH", "000016.SH", "399903");
    public static final List<String> CN_INDICE_MORE_NAMES_EN = Arrays.asList("SSE Comp", "SZSE Comp", "ChiNext",
            "CSI300", "SEE50", "CSI100");


    public static final String STK = "STK";
    public static final String OPT = "OPT";
    public static final String WAR = "WAR";
    public static final String IOPT = "IOPT";
    public static final String FUT = "FUT";  //期货

    public static final String ETF = "ETF";
    public static final String ETP = "ETP";
    public static final String ETN = "ETN";

    public static final Pattern FUTURE_PATTERN = Pattern.compile("^[0-9A-Z]+(main|[0-9]{4})$");  //期货正则表达式
    public static final Pattern NOT_FUTURE_PATTERN = Pattern.compile("^[0-9]{5,}$"); //期货的symbol不能为纯数字，需要排除

    /**
     * transform stockId to symbol
     *
     * @return
     */
    public static String stockId2Symbol(String stockId) {
        if (stockId != null) {
            if (stockId.endsWith(MARKET_POSTFIX_UK)
                    || stockId.endsWith(MARKET_POSTFIX_NZ)
                    || stockId.endsWith(MARKET_POSTFIX_SI)) {
                return stockId;
            }
            if (stockId.endsWith(MARKET_POSTFIX_US)
                    || stockId.endsWith(MARKET_POSTFIX_HK)) {
                return stockId.substring(0, stockId.length() - 3);
            }

            // SH特殊处理(指数)
            if (stockId.endsWith(MARKET_POSTFIX_SH)) {
                String market = getCnMarket(stockId);
                if (!StringUtils.equals(CommonConsts.MARKET_SH, market)) {
                    // market与后缀不一致，可能是SH指数，直接返回
                    return stockId;
                }
                return stockId.substring(0, stockId.length() - 3);
            }

            // SZ没有特殊(指数)
            if (stockId.endsWith(MARKET_POSTFIX_SZ)) {
                return stockId.substring(0, stockId.length() - 3);
            }
        }

        return stockId;
    }

    public static String getMarketLabelBySymbol(String symbol) {
        if (symbol != null && symbol.matches("[0-9]+")) {
            if (symbol.length() >= 6) {
                return getCnMarket(symbol);
            }
            return CommonConsts.MARKET_HK;
        } else if (symbol != null && symbol.endsWith(MARKET_POSTFIX_SH)) {
            return CommonConsts.MARKET_SH;
        } else if (symbol != null && symbol.endsWith(MARKET_POSTFIX_UK)) {
            return CommonConsts.MARKET_UK;
        } else if (symbol != null && symbol.endsWith(MARKET_POSTFIX_NZ)) {
            return CommonConsts.MARKET_NZ;
        } else if (symbol != null && symbol.endsWith(MARKET_POSTFIX_SI)) {
            return CommonConsts.MARKET_SI;
        } else if (symbol != null && HK_INDICE_ALIAS_LIST.indexOf(symbol) != -1) {
            return CommonConsts.MARKET_HK;
        }

        return CommonConsts.MARKET_US;
    }

    /**
     * @param symbol
     * @return
     */
    public static String symbol2StockId(String symbol) {
        if (symbol != null) {
            if (symbol.matches("[0-9]+")) {
                // sz or sh
                if (symbol.length() >= 6) {
                    String market = getCnMarket(symbol);
                    if (StringUtils.equals(CommonConsts.MARKET_SZ, market)) {
                        return symbol + MARKET_POSTFIX_SZ;
                    } else {
                        return symbol + MARKET_POSTFIX_SH;
                    }
                }

                if (!symbol.endsWith(MARKET_POSTFIX_HK)) {
                    return StringUtils.leftPad(symbol, 5, '0') + MARKET_POSTFIX_HK;
                }
            } else {
                if (HK_INDICE_ALIAS_LIST.indexOf(symbol) != -1 && !symbol.endsWith(MARKET_POSTFIX_HK)) {
                    return symbol + MARKET_POSTFIX_HK;
                }
                if (symbol.endsWith(MARKET_POSTFIX_UK)) {
                    return symbol;
                }
                if (symbol.endsWith(MARKET_POSTFIX_NZ)) {
                    return symbol;
                }
                if (symbol.endsWith(MARKET_POSTFIX_SI)) {
                    return symbol;
                }
                if (symbol.endsWith(MARKET_POSTFIX_SH)) {
                    // SH指数
                    return symbol;
                }
                if (!symbol.endsWith(MARKET_POSTFIX_US)) {
                    return symbol + MARKET_POSTFIX_US;
                }
            }

        }

        return symbol;
    }

    /**
     * 判断symbol是否属于美股指数
     * @param symbol
     * @return
     */
    public static boolean isUSIndice(String symbol) {
        return US_INDICE_ALIAS_LIST.contains(symbol);
    }

    /**
     * 判断symbol是否属于美股期权
     * @param symbol
     * @return
     */
    public static boolean isUSOption(String symbol) {
        return symbol != null && symbol.length() > 10 && (symbol.endsWith(" CALL") || symbol.endsWith(" PUT"));
    }

    /**
     * 是否为港股股本证券
     * @param symbol
     * @return
     */
    public static boolean isHkEquity(String symbol) {
        return symbol != null && symbol.length() == 5 && STK.equals(getHkSecType(symbol))
                && !isHkDebt(symbol) && !isHkEtf(symbol) && !isHkInlineWarrant(symbol);
    }

    /**
     * 是否为港股债券
     * @param symbol
     * @return
     */
    public static boolean isHkDebt(String symbol) {
        return symbol != null && symbol.length() == 5
                && (symbol.compareTo("04300") >= 0 && symbol.compareTo("04329") <= 0
                || symbol.compareTo("04400") >= 0 && symbol.compareTo("04599") <= 0
                || symbol.compareTo("05000") >= 0 && symbol.compareTo("06029") <= 0
                || symbol.compareTo("84300") >= 0 && symbol.compareTo("84329") <= 0
                || symbol.compareTo("84400") >= 0 && symbol.compareTo("84599") <= 0
                || symbol.compareTo("85000") >= 0 && symbol.compareTo("85743") <= 0
                || symbol.compareTo("85901") >= 0 && symbol.compareTo("86029") <= 0
                || symbol.compareTo("40000") >= 0 && symbol.compareTo("40399") <= 0);
    }

    /**
     * 是否为港股ETF
     * @param symbol
     * @return
     */
    public static boolean isHkEtf(String symbol) {
        return symbol != null && symbol.length() == 5
                && (symbol.compareTo("02800") >= 0 && symbol.compareTo("02849") <= 0
                || symbol.compareTo("03000") >= 0 && symbol.compareTo("03199") <= 0
                || symbol.compareTo("07200") >= 0 && symbol.compareTo("07399") <= 0
                || symbol.compareTo("07500") >= 0 && symbol.compareTo("07599") <= 0
                || symbol.compareTo("09000") >= 0 && symbol.compareTo("09399") <= 0
                || symbol.compareTo("09500") >= 0 && symbol.compareTo("09599") <= 0
                || symbol.compareTo("09800") >= 0 && symbol.compareTo("09849") <= 0
                || symbol.compareTo("82800") >= 0 && symbol.compareTo("82849") <= 0
                || symbol.compareTo("83000") >= 0 && symbol.compareTo("83199") <= 0
                || symbol.compareTo("87200") >= 0 && symbol.compareTo("87399") <= 0
                || symbol.compareTo("87500") >= 0 && symbol.compareTo("87599") <= 0);
    }

    /**
     * 是否为港股界内证
     * @param symbol
     * @return
     */
    public static boolean isHkInlineWarrant(String symbol) {
        return symbol != null && symbol.length() == 5
                && (symbol.compareTo("47000") >= 0 && symbol.compareTo("48999") <= 0);

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

    public static boolean isCnImportantIndice(String symbol) {
        return CN_INDICE_ALIAS_LIST.contains(symbol);
    }

    /**
     * 按symbol得到市场类型
     * @param symbol
     * @return
     */
    public static String transformCnMarket(Object symbol) {
        if(symbol == null){
            return null;
        }
        String symbolID = symbol.toString();
        if(symbolID == null){
            return null;
        } else if (symbolID.length() == 6) {
            return getCnMarket(symbolID);
        } else if(symbolID.endsWith(MARKET_POSTFIX_SH)){
            return CommonConsts.MARKET_SH;
        }
        return null;
    }

    /**
     * 获取市场的重要指数
     * @param market
     * @return
     */
    public static List<String> getIndicesAlias(String market) {
        switch (market) {
            case CommonConsts.MARKET_US:
                return US_INDICE_ALIAS_LIST;
            case CommonConsts.MARKET_HK:
                return HK_INDICE_ALIAS_LIST;
            case CommonConsts.MARKET_CN:
                return CN_INDICE_ALIAS_LIST;
            case CommonConsts.MARKET_UK:
                return UK_INDICE_ALIAS_LIST;
            case CommonConsts.MARKET_NZ:
                return NZ_INDICE_ALIAS_LIST;
            default:
                return null;
        }
    }

    /**
     * 获取市场重要指数的代号和名称的映射
     * @param market
     * @return
     */
    public static Map<String, String> getIndicesName(String market) {
        switch (market) {
            case CommonConsts.MARKET_US:
                Map<String, String> usNameMap = new HashMap<>();
                for (int i = 0; i < US_INDICE_ALIAS.length; ++i) {
                    usNameMap.put(US_INDICE_ALIAS[i], US_INDICE_NAMES[i]);
                }
                return usNameMap;
            case CommonConsts.MARKET_HK:
                Map<String, String> hkNameMap = new HashMap<>();
                for (int i = 0; i < HK_INDICE_ALIAS.length; ++i) {
                    hkNameMap.put(HK_INDICE_ALIAS[i], HK_INDICE_NAMES[i]);
                }
                return hkNameMap;
            case CommonConsts.MARKET_CN:
                Map<String, String> cnNameMap = new HashMap<>();
                for (int i = 0; i < CN_INDICE_ALIAS.length; ++i) {
                    cnNameMap.put(CN_INDICE_ALIAS[i], CN_INDICE_NAMES[i]);
                }
                return cnNameMap;
            case CommonConsts.MARKET_UK:
                Map<String, String> ukNameMap = new HashMap<>();
                for (int i = 0; i < UK_INDICE_ALIAS.length; ++i) {
                    ukNameMap.put(UK_INDICE_ALIAS[i], UK_INDICE_NAMES[i]);
                }
                return ukNameMap;
            case CommonConsts.MARKET_NZ:
                Map<String, String> nzNameMap = new HashMap<>();
                for (int i = 0; i < NZ_INDICE_ALIAS.length; ++i) {
                    nzNameMap.put(NZ_INDICE_ALIAS[i], NZ_INDICE_NAMES[i]);
                }
                return nzNameMap;
            default:
                return null;
        }
    }

    public static String getMarketByBKCode(String bkCode) {
        if (StringUtils.isNotEmpty(bkCode)) {
            if (bkCode.startsWith("BK4")) {
                return CommonConsts.MARKET_NZ;
            } else if (bkCode.startsWith("BK3")) {
                return CommonConsts.MARKET_UK;
            } else if (bkCode.startsWith("BK2")) {
                return CommonConsts.MARKET_US;
            } else if (bkCode.startsWith("BK1")) {
                return CommonConsts.MARKET_HK;
            } else if (bkCode.startsWith("BK0")) {
                return CommonConsts.MARKET_CN;
            }
        }

        return null;
    }


    private static String getHkSecType(String symbol) {
        if (symbol != null && symbol.length() == 5) {
            if (symbol.compareTo("10000") >= 0 && symbol.compareTo("29999") <= 0) {
                return WAR;
            } else if (symbol.compareTo("50000") >= 0 && symbol.compareTo("69999") <= 0) {
                return IOPT;
            }
        }

        return STK;
    }

    /**
     * 通过证券代码判断证券类型
     * 注：输入可以认为是合法存在的证券代码，因此只简单通过字符长度判断，不做字母、数字严格校验
     * @param symbol
     * @return
     */
    public static String getSecTypeBySymbol(String symbol) {
        if (symbol != null) {
            if (isFuture(symbol)) {
                return FUT;
            } else if (isUSOption(symbol)) {
                return OPT;
            } else if (symbol.length() == 5) {
                return getHkSecType(symbol);
            }
        }

        return STK;
    }

    public static String getSecMarketBySymbol(String symbol) {
        if (symbol != null) {
            if (isFuture(symbol)) {
                return CommonConsts.MARKET_GLOBAL_FUTURE;
            } else if (isUSOption(symbol)) {
                return CommonConsts.MARKET_USOPT;
            } else {
                return getMarketLabelBySymbol(symbol);
            }
        }

        return null;
    }

    /**
     * 判断symbol是否表示期货
     * 期货的symbol分为两部分，前缀可能是数字和字符的组合，但是不可能为纯数字；后缀为"main"或者四位数字
     * @param symbol
     * @return
     */
    public static boolean isFuture(String symbol) {
        if (null == symbol || symbol.length() <= 4 || symbol.startsWith("BK")) {
            return false;
        }
        // 不能为纯数字
        if (NOT_FUTURE_PATTERN.matcher(symbol).matches()) {
            return false;
        }
        if (FUTURE_PATTERN.matcher(symbol).matches()) {
            return true;
        }
        return false;
    }

    /**
     * 判断symbol是否属于美股市场，只包含正股，排除期货、期权。
     * @param symbol
     * @return
     */
    public static boolean isUsMarket(String symbol) {
        return StringUtils.equalsIgnoreCase(CommonConsts.MARKET_US, getSecMarketBySymbol(symbol));
    }

    /**
     * 判断symbol是否属于港股市场
     * @param symbol
     * @return
     */
    public static boolean isHkMarket(String symbol) {
        return StringUtils.equalsIgnoreCase(CommonConsts.MARKET_HK, getSecMarketBySymbol(symbol));
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
     * 判断symbol是否属于英股市场
     * @param symbol
     * @return
     */
    public static boolean isUkMarket(String symbol) {
        return StringUtils.equalsIgnoreCase(CommonConsts.MARKET_UK, getSecMarketBySymbol(symbol));
    }

    /**
     * 判断symbol是否属于新加坡市场
     * @param symbol
     * @return
     */
    public static boolean isSiMarket(String symbol) {
        return StringUtils.equalsIgnoreCase(CommonConsts.MARKET_SI, getSecMarketBySymbol(symbol));
    }

    /**
     * 判断symbol是否属于新西兰市场
     * @param symbol
     * @return
     */
    public static boolean isNzMarket(String symbol) {
        return StringUtils.equalsIgnoreCase(CommonConsts.MARKET_NZ, getSecMarketBySymbol(symbol));
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

    /**
     * 判断symbol是否为科创板股票代码
     *
     * @param symbol
     * @return
     */
    public static boolean isKcbSymbol(String symbol) {
        return symbol != null && symbol.startsWith("688") && isCnMarket(symbol);
    }

}
