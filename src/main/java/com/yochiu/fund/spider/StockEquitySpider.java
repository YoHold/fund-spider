package com.yochiu.fund.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yochiu.fund.until.HttpUtil;
import com.yochiu.fund.until.SymbolUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * @Author: yochiu
 * @Description: 获取股票总股本
 * @Date: 2020/10/12
 */
public class StockEquitySpider {

    private static final String STOCK_EQUITY = "http://f10.eastmoney.com/CapitalStockStructure/CapitalStockStructureAjax";

    /**
     * 获取股票流通股本
     * 解析数据结构如下:
     * {
     *     "ShareChangeList":[
     *         {
     *             "des":"单位:万股",
     *             "changeList":[
     *                 "2020-09-10",
     *                 "2020-09-07"
     *             ]
     *         },
     *         {
     *             "des":"总股本",
     *             "changeList":[
     *                 "344,151.77",
     *                 "344,151.77"
     *             ]
     *         }
     *     ]
     * }
     * @param symbol
     * @return
     */
    public static Pair<String, Double> getStockEquity(String symbol) {
        Map<String, String> params = Maps.newHashMap();
        params.put("code", SymbolUtil.getStockId(symbol));
        String content = HttpUtil.get(STOCK_EQUITY, 3000, 3000, "UTF-8", params);
        JSONObject dataJson = JSONObject.parseObject(content);

        JSONArray shareChangeList = dataJson.getJSONArray("ShareChangeList");
        if (shareChangeList.size() >= 2) {
            JSONObject shareChangeDateList = shareChangeList.getJSONObject(0);
            JSONObject shareChangeDataList = shareChangeList.getJSONObject(1);
            JSONArray dateList = shareChangeDateList.getJSONArray("changeList");
            JSONArray shareList =  shareChangeDataList.getJSONArray("changeList");
            if (dateList.size() > 0 && shareList.size() > 0) {
                return Pair.of(dateList.getString(0), Double.valueOf(shareList.getString(0).replaceAll(",","")));
            }

        }
        return null;
    }

}
