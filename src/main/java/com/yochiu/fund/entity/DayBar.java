package com.yochiu.fund.entity;

import lombok.Data;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/8/2
 */
@Data
public class DayBar {

    private String symbol;

    private Double firstDayPrice;

    private Double endDayPrice;

    private Double quoteChange;

}
