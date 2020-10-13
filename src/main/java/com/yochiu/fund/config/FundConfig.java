package com.yochiu.fund.config;

import com.yochiu.fund.support.YmlResourceFactory;
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
@PropertySource(value = "classpath:application.yml", encoding = "utf-8", factory = YmlResourceFactory.class)
@Configuration
@ConfigurationProperties(prefix = "fund")
public class FundConfig {

    private List<String> codes;

    private String filePath;

}
