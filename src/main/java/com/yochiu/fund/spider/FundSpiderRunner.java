package com.yochiu.fund.spider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yochiu.fund.config.FundConfig;
import com.yochiu.fund.entity.StockData;
import com.yochiu.fund.entity.StockShare;
import com.yochiu.fund.until.BigDecimalUtil;
import com.yochiu.fund.until.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/10/12
 */
@Slf4j
@Component
public class FundSpiderRunner implements CommandLineRunner {

    @Autowired
    private FundConfig fundConfig;


    @Override
    public void run(String... args) {

        List<String> fundCodes = fundConfig.getCodes();

        Pair<Map<String, StockShare>, Map<String, StockShare>> stockSharePair = FundPositionSpider.getStockPosition(fundCodes);
        Map<String, StockData> increaseStockShareMap = getIncreaseStockShare(stockSharePair.getLeft(), stockSharePair.getRight());

        increaseStockShareMap.forEach((symbol, stockData) -> {
            String[] profitForecastData = StockProfitForecastSpider.getProfitForecast(symbol).values().toArray(new String[]{});
            if (profitForecastData.length >= 4) {
                stockData.setProfitForecastY0(profitForecastData[0]);
                stockData.setProfitForecastY1(profitForecastData[1]);
                stockData.setProfitForecastY2(profitForecastData[2]);
                stockData.setProfitForecastY3(profitForecastData[3]);
            }

            String[] increaseProfitData = StockFinanceSpider.getIncreaseProfit(symbol).values().toArray(new String[]{});
            if (increaseProfitData.length >= 4) {
                stockData.setProfitGrowthQ0(increaseProfitData[0]);
                stockData.setProfitGrowthQ1(increaseProfitData[1]);
                stockData.setProfitGrowthQ2(increaseProfitData[2]);
                stockData.setProfitGrowthQ3(increaseProfitData[3]);
            }

            Pair<String, Double> stockEquityData = StockEquitySpider.getStockEquity(symbol);
            stockData.setShareCapital(stockEquityData.getRight());

            Pair<Double, Double> dayBarPair = StockDayBarSpider.getDayBar(symbol);
            if (dayBarPair != null) {
                stockData.setEndPrice(dayBarPair.getLeft());
                stockData.setFirstPrice(dayBarPair.getRight());
                if (dayBarPair.getRight() > 0) {
                    stockData.setQuoteChange(BigDecimalUtil.divide(dayBarPair.getLeft(), dayBarPair.getRight()));
                }
            }
        });

        saveStockData(Lists.newArrayList(increaseStockShareMap.values()));
    }

    private void saveStockData(List<StockData> stockDataList) {
        if (CollectionUtils.isEmpty(stockDataList)) {
            return;
        }
        File file = new File(fundConfig.getFilePath());
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            ExcelUtil.write(outputStream, stockDataList);
        } catch (Exception e) {
            log.error("save stock data error", e);
        }
    }


    private Map<String, StockData> getIncreaseStockShare(Map<String, StockShare> latestQuarterStockShare, Map<String, StockShare> lastQuarterStockShares) {
        Map<String, StockData> increaseStockShareMap = Maps.newHashMap();
        latestQuarterStockShare.forEach((symbol, latestStockShare) -> {
            double increaseShare = lastQuarterStockShares.containsKey(symbol) ?
                    latestStockShare.getShare() - lastQuarterStockShares.get(symbol).getShare() : latestStockShare.getShare();
            if (increaseShare > 0) {
                try {
                    Double preShare = lastQuarterStockShares.containsKey(symbol) ? lastQuarterStockShares.get(symbol).getShare() : 0D;
                    Double percent = preShare == 0 ? 1 : BigDecimalUtil.divide(increaseShare, preShare);
                    StockData stockData = StockData.builder()
                            .symbol(latestStockShare.getSymbol())
                            .symbolCode(latestStockShare.getSymbolCode())
                            .curShare(latestStockShare.getShare())
                            .addShare(new BigDecimal(increaseShare).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue())
                            .percent(percent)
                            .preShare(preShare).build();
                    increaseStockShareMap.put(stockData.getSymbolCode(), stockData);
                } catch (Exception e) {
                    log.info("getIncrease stock share, latestStockShare: {} , lastStockShare: {}", latestStockShare, latestStockShare);
                }

            }
        });

        return increaseStockShareMap;
    }


}
