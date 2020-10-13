package com.yochiu.fund.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yochiu.fund.common.CommonConsts;
import com.yochiu.fund.until.HttpUtil;
import com.yochiu.fund.until.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/10/12
 */
@Slf4j
public class StockDayBarSpider {

    private static final String SZ_DAY_BAR_API = "http://push2his.eastmoney.com/api/qt/stock/kline/get?" +
            "fields1=f5&fields2=f51,f53&klt=101&fqt=1&beg=0&end=20500000";

    //secid=1.600779&
    private static final String SH_DAY_BAR_API = "http://86.push2his.eastmoney.com/api/qt/stock/kline/get?" +
            "fields1=f5&fields2=f51,f53&klt=101&fqt=0&beg=0&end=20500101";

    /**
     * 获取股票日K
     * 解析数据结构如下:
     * {
     *     "rc":0,
     *     "rt":17,
     *     "svr":2887135409,
     *     "lt":1,
     *     "full":0,
     *     "data":{
     *         "dktotal":2981,
     *         "klines":[
     *             "2008-05-22,0.76",
     *             "2008-05-23,0.78",
     *             "2008-05-26,0.76",
     *             "2008-05-27,0.79"
     *         ]
     *     }
     * }
     * @param symbol
     * @return
     */
    public static Pair<Double, Double> getDayBar(String symbol) {
        String content = chooseDayBarApi(symbol);
        if (StringUtils.isEmpty(content)) {
            return null;
        }

        JSONObject dataJson = JSONObject.parseObject(content);
        if (dataJson == null) {
            return null;
        }
        if (dataJson.containsKey("data") && dataJson.getJSONObject("data") != null) {
            JSONArray klineArray = dataJson.getJSONObject("data").getJSONArray("klines");
            int size = klineArray.size();

            int lastIndex = size - 1;
            int oneQuarterlyAgoIndex = lastIndex - 20 * 3 > 0 ? lastIndex - 20 *3 : 0;

            String latestDayBar = klineArray.getString(lastIndex);
            String oneQuarterAgoDayBar = klineArray.getString(oneQuarterlyAgoIndex);

            return Pair.of(extractDayBar(latestDayBar), extractDayBar(oneQuarterAgoDayBar));
        }
        return null;
    }

    private static double extractDayBar(String dayBarText) {
        String[] dayBarArray = dayBarText.split(",");
        if (dayBarArray.length == 2) {
            return Double.valueOf(dayBarArray[1]);
        }
        return 0D;
    }

    private static String chooseDayBarApi(String symbol) {
        String market = SymbolUtil.getMarketLabelBySymbol(symbol);
        Map<String, String> params = Maps.newHashMap();
        String dayBarApi = null;
        if (CommonConsts.MARKET_SZ.equals(market)) {
            params.put("secid", "0." + symbol);
            dayBarApi = SZ_DAY_BAR_API;
        } else if (CommonConsts.MARKET_SH.equals(market)) {
            params.put("secid", "1." + symbol);
            dayBarApi = SH_DAY_BAR_API;
        }
        if (StringUtils.isEmpty(dayBarApi)) {
            return null;
        }
        return HttpUtil.get(dayBarApi, 3000, 3000, "UTF-8", params);
    }

}
