package com.yochiu.fund.entity;

import com.yochiu.fund.support.ExcelColumn;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/4/29
 */
@Data
@Builder
public class StockData {

    @ExcelColumn(header = "股票", order = 1)
    private String symbol;

    @ExcelColumn(header = "股票代码", order = 2)
    private String symbolCode;

    @ExcelColumn(header = "当前季度持股数(万股)", order = 3)
    private Double curShare;

    @ExcelColumn(header = "上季度持股数(万股)", order = 4)
    private Double preShare;

    @ExcelColumn(header = "添加的持股数(万股)", order = 5)
    private Double addShare;

    @ExcelColumn(header = "已上市流通A股(万股)", order = 6)
    private Double shareCapital;

    @ExcelColumn(header = "持股数比例", order = 7)
    private Double percent;

    @ExcelColumn(header = "季度初股价", order = 8)
    private Double firstPrice;

    @ExcelColumn(header = "季度末股价", order = 9)
    private Double endPrice;

    @ExcelColumn(header = "季度末股价涨幅", order = 10)
    private Double quoteChange;

    @ExcelColumn(header = "扣非净利润同比增长(%)Q0", order = 11)
    private String profitGrowthQ0;

    @ExcelColumn(header = "扣非净利润同比增长(%)Q1", order = 12)
    private String profitGrowthQ1;

    @ExcelColumn(header = "扣非净利润同比增长(%)Q2", order = 13)
    private String profitGrowthQ2;

    @ExcelColumn(header = "扣非净利润同比增长(%)Q3", order = 14)
    private String profitGrowthQ3;

    @ExcelColumn(header = "近六月盈利预期(收益)Y0", order = 15)
    private String profitForecastY0;

    @ExcelColumn(header = "近六月盈利预期(收益)Y1", order = 16)
    private String profitForecastY1;

    @ExcelColumn(header = "近六月盈利预期(收益)Y2", order = 17)
    private String profitForecastY2;

    @ExcelColumn(header = "近六月盈利预期(收益)Y3", order = 18)
    private String profitForecastY3;


}


