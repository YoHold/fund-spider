package com.yochiu.fund.support;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/4/29
 */
public interface ExcelValueConverter {

    String converter(Object o);


    class DefaultValueConverter implements ExcelValueConverter {

        @Override
        public String converter(Object o) {
            if (o == null) {
                return "";
            }
            return o.toString();
        }
    }
}
