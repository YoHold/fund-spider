package com.yochiu.fund.spider;

import com.google.common.collect.Maps;
import com.yochiu.fund.entity.StockShare;
import com.yochiu.fund.until.BigDecimalUtil;
import com.yochiu.fund.until.HttpUtil;
import com.yochiu.fund.until.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.yochiu.fund.common.CommonConsts.*;

/**
 * @Author: yochiu
 * @Description: 获取基金最近一个季度跟上个季度持仓
 * @Date: 2020/10/12
 */
@Slf4j
public class FundPositionSpider {

    private static final String POSITION_FUND = "http://fundf10.eastmoney.com/FundArchivesDatas.aspx?type=jjcc&topline=20";


    public static Pair<Map<String, StockShare>, Map<String, StockShare>> getStockPosition(List<String> fundCodes) {

        Map<String, StockShare> firstStockShares = Maps.newHashMap();
        Map<String, StockShare> nextStockShares = Maps.newHashMap();

        fundCodes.parallelStream().forEach(code -> {
            Map<String, String> params = Maps.newHashMap();
            params.put(CODE, code);
            String content = HttpUtil.get(POSITION_FUND, 6000, 6000, "UTF-8", params);
            String resContent = trimContent(content);
            Document document = Jsoup.parse(resContent);
            Elements divElements = document.select("div[class~=boxitem w790]");
            int divElementSize = divElements.size();
            if (divElementSize > 0) {
                try {
                    Element firstDivElement = divElements.first();
                    //String date = firstDivElements.select("font[class~=px12]").first().text();
                    extractStockShare(firstDivElement, firstStockShares);

                    Element nextDivElements = divElementSize > 1 ? divElements.get(1) : null;
                    extractStockShare(nextDivElements, nextStockShares);
                } catch (Exception e) {
                    log.info("FundPositionSpider extract stockShare error, symbol: {}", code);
                }
            }
        });

        return Pair.of(firstStockShares, nextStockShares);
    }


    private static String trimContent(String content) {
        int endIndex = content.indexOf(END_SUFFIX);
        int startIndex = START_PREFIX.length();
        return content.substring(startIndex, endIndex);
    }

    private static void extractStockShare(Element divElement, Map<String, StockShare> stockShareMap) {
        if (divElement == null) {
            return;
        }
        Elements tableElements = divElement.select("tbody").select("tr");
        for (Element element : tableElements) {
            String symbolCode = element.select("a[href]").first().text();
            if (!SymbolUtil.isCnMarket(symbolCode)) {
                continue;
            }
            String symbol = element.select("td[class=tol]").text();
            Elements subElements = element.select("td[class=tor]");
            String shareText = subElements.last().previousElementSibling().text();
            double share = new BigDecimal(shareText.replaceAll(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            if (stockShareMap.containsKey(symbolCode)) {
                StockShare stockShare = stockShareMap.get(symbolCode);
                stockShare.setShare(BigDecimalUtil.add(share, stockShare.getShare()));
            } else {
                StockShare stockShare = new StockShare();
                stockShare.setSymbol(symbol);
                stockShare.setSymbolCode(symbolCode);
                stockShare.setShare(share);
                stockShareMap.put(symbolCode, stockShare);
            }
        }
    }


}
