package com.yochiu.fund.spider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yochiu.fund.config.FundConfig;
import com.yochiu.fund.entity.StockBoard;
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

import javax.annotation.PostConstruct;
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

    private Map<String, Integer> hotBoardMap;

    private String workDir;

    private String filePath;

    public FundSpiderRunner() {
        this.hotBoardMap = Maps.newHashMap();
        this.workDir = System.getProperty("user.dir");
    }

    @PostConstruct
    public void init(){
        this.filePath = this.workDir + "/" + fundConfig.getFileName();
    }


    @Override
    public void run(String... args) {

        List<String> fundCodes = fundConfig.getCodes();
        Pair<Map<String, StockShare>, Map<String, StockShare>> stockSharePair = FundPositionSpider.getStockPosition(fundCodes);
        Map<String, StockData> increaseStockShareMap = getIncreaseStockShare(stockSharePair.getLeft(), stockSharePair.getRight());

        increaseStockShareMap.entrySet().parallelStream().forEach(entry -> {
            String symbol = entry.getKey();
            StockData stockData = entry.getValue();
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
            if (stockEquityData != null && stockEquityData.getRight() != null) {
                stockData.setShareCapital(stockEquityData.getRight());
                stockData.setPercent(BigDecimalUtil.divide(stockData.getCurShare(), stockData.getShareCapital()));
            }

            Pair<Double, Double> dayBarPair = StockDayBarSpider.getDayBar(symbol);
            if (dayBarPair != null) {
                stockData.setEndPrice(dayBarPair.getLeft());
                stockData.setFirstPrice(dayBarPair.getRight());
                if (dayBarPair.getRight() > 0) {
                    stockData.setQuoteChange(BigDecimalUtil.divide(dayBarPair.getLeft()-dayBarPair.getRight(), dayBarPair.getRight()));
                }
            }

            StockBoard stockBoard = StockBoardSpider.getStockBoard(symbol);
            if (stockBoard != null) {
                stockData.setBoardDesc(stockBoard.getBoardDesc());
                stockData.setBusinessDesc(stockBoard.getBusinessDesc());
                summaryBoard(stockBoard.getBoardDesc());
            }
        });

        saveStockData(Lists.newArrayList(increaseStockShareMap.values()));
        collectHotBoard();
    }

    private void saveStockData(List<StockData> stockDataList) {
        if (CollectionUtils.isEmpty(stockDataList)) {
            return;
        }
        stockDataList.sort(Comparator.comparing(StockData::getAddShare).reversed());
        File file = new File(this.filePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            ExcelUtil.write(outputStream, stockDataList);
        } catch (Exception e) {
            log.error("FundSpiderRunner save fund data error", e);
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
                    StockData stockData = StockData.builder()
                            .symbol(latestStockShare.getSymbol())
                            .symbolCode(latestStockShare.getSymbolCode())
                            .curShare(latestStockShare.getShare())
                            .addShare(new BigDecimal(increaseShare).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue())
                            .preShare(preShare).build();
                    increaseStockShareMap.put(stockData.getSymbolCode(), stockData);
                } catch (Exception e) {
                    log.info("getIncrease stock share, latestStockShare: {} , lastStockShare: {}", latestStockShare, latestStockShare);
                }

            }
        });

        return increaseStockShareMap;
    }

    private void summaryBoard(String boardDesc) {
        String[] boardArray = boardDesc.split(" ");
        for (String board: boardArray) {
            hotBoardMap.put(board, hotBoardMap.getOrDefault(board, 0) + 1);
        }
    }

    private void collectHotBoard() {
        List<Map.Entry<String,Integer>> entryList = new ArrayList<>(hotBoardMap.entrySet());
        //降序排序
        entryList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        StringBuilder hotBoardDesc = new StringBuilder();
        for (int i = 0; i < Math.min(15, entryList.size()); i++) {
            Map.Entry<String, Integer> entry = entryList.get(i);
            hotBoardDesc.append(entry.getKey() + ":" + entry.getValue()).append("\n");
        }
        log.info("collectHotBoard result :\n{}", hotBoardDesc.toString());
    }

}
