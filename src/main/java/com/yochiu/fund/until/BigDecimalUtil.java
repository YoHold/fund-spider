package com.yochiu.fund.until;

import java.math.BigDecimal;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/10/12
 */
public class BigDecimalUtil {

    public static double divide(double a, double b) {
        return new BigDecimal(a).divide(new BigDecimal(b), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double add(double a, double b) {
        return new BigDecimal(a).add(new BigDecimal(b)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
