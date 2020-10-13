package com.yochiu.fund.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yochiu.fund.until.HttpUtil;
import com.yochiu.fund.until.SymbolUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author: yochiu
 * @Description: 股票盈利预测✌
 * @Date: 2020/10/12
 */
@Slf4j
public class StockProfitForecastSpider {

    private static final String PROFIT_FORECAST_URL = "http://f10.eastmoney.com/ProfitForecast/ProfitForecastAjax";

    /**
     * 获取盈利预测值:
     * 解析数据格式如下:
     * {
     *     "jgyc":{
     *         "baseYear":2019,
     *         "data":[
     *             {
     *                 "jgmc":"六个月平均",
     *                 "sy":"1.16",
     *                 "syl":"42.32",
     *                 "sy1":"1.23",
     *                 "syl1":"28.31",
     *                 "sy2":"1.47",
     *                 "syl2":"23.65",
     *                 "sy3":"1.70",
     *                 "syl3":"20.45"
     *             }
     *         ]
     *     },
     *     "mgsy":[
     *         {
     *             "year":"2019A",
     *             "value":"0.77",
     *             "ratio":"-11.92"
     *         },
     *         {
     *             "year":"2020E",
     *             "value":"1.67",
     *             "ratio":"115.74"
     *         },
     *         {
     *             "year":"2021E",
     *             "value":"1.98",
     *             "ratio":"18.73"
     *         },
     *         {
     *             "year":"2022E",
     *             "value":"2.25",
     *             "ratio":"13.59"
     *         }
     *     ]
     * }
     * @param symbol
     * @return
     */
    public static TreeMap<String, String> getProfitForecast(String symbol) {
        Map<String, String> params = Maps.newHashMap();
        params.put("code", SymbolUtil.getStockId(symbol));
        String content = HttpUtil.get(PROFIT_FORECAST_URL, 3000, 3000, "UTF-8", params);

        TreeMap<String, String> profitForecastMap = new TreeMap<>(Comparator.reverseOrder());
        try {
            JSONObject dataJson = JSONObject.parseObject(content);
            JSONArray dateArrayMsg = dataJson.getJSONArray("mgsy");
            JSONObject profitDataJson = dataJson.getJSONObject("jgyc");
            if (profitDataJson.containsKey("data")) {
                JSONArray profitDataArray = profitDataJson.getJSONArray("data");
                if (profitDataArray.size() > 0) {
                    JSONObject dataItem = profitDataArray.getJSONObject(0);
                    for (int i = 0; i < dateArrayMsg.size(); i++) {
                        JSONObject dateMsgJson = dateArrayMsg.getJSONObject(i);
                        if (dateMsgJson.containsKey("year")) {
                            String key = i == 0 ? "sy" : "sy" + i;
                            profitForecastMap.put(dateMsgJson.getString("year"), dataItem.getString(key));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("StockProfitForecastSpider getProfitForecast error, symbol: {}, content: {}", symbol, content);
        }

        return profitForecastMap;
    }

}
