package com.yochiu.fund.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yochiu.fund.until.HttpUtil;
import com.yochiu.fund.until.SymbolUtil;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author: yochiu
 * @Description: 财务分析
 * @Date: 2020/10/12
 */
public class StockFinanceSpider {

    //财务分析接口, type参数可能会变化
    private static final String STOCK_FINANCE = "http://f10.eastmoney.com/NewFinanceAnalysis/MainTargetAjax?type=0";

    /**
     * 获取扣非净利润同比增长(%)
     * 解析数据格式如下:
     * {
     *     "date":"2020-06-30",
     *     "jbmgsy":"0.1800",
     *     "kfmgsy":null,
     *     "xsmgsy":null,
     *     "mgjzc":"8.3672",
     *     "mggjj":"1.8047",
     *     "mgwfply":"5.3730",
     *     "mgjyxjl":"1.1410",
     *     "yyzsr":"25.7亿",
     *     "mlr":"15.1亿",
     *     "gsjlr":"6.56亿",
     *     "kfjlr":"5.54亿",
     *     "yyzsrtbzz":"59.12",
     *     "gsjlrtbzz":"22.82",
     *     "kfjlrtbzz":"8.66",
     *     "yyzsrgdhbzz":"15.28"
     * }
     * @param symbol
     * @return
     */
    public static TreeMap<String, String> getIncreaseProfit(String symbol) {
        Map<String, String> params = Maps.newHashMap();
        params.put("code", SymbolUtil.getStockId(symbol));

        TreeMap<String, String> profitMap = new TreeMap<>(Comparator.reverseOrder());
        String content = HttpUtil.get(STOCK_FINANCE, 3000, 3000, "UTF-8", params);

        JSONArray dataArray = JSONArray.parseArray(content);
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject item = dataArray.getJSONObject(i);
            String date = item.getString("date");
            String increasedProfit = item.getString("kfjlrtbzz");
            profitMap.put(date, increasedProfit);
        }
        return profitMap;
    }

}
