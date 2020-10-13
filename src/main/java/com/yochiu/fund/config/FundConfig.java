package com.yochiu.fund.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/10/12
 */
@Data
@PropertySource(value = "classpath:application.yml", encoding = "utf-8")
@Configuration
@ConfigurationProperties(prefix = "fund")
public class FundConfig {

    private List<String> codes;

    private String fileName;

}
