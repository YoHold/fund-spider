package com.yochiu.fund;

import com.yochiu.fund.config.FundConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Author: yochiu
 * @Description:
 * @Date: 2020/4/27
 */
@SpringBootApplication
@Slf4j
public class FundSpiderApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(FundSpiderApplication.class, args);
        FundConfig fundConfig = applicationContext.getBean(FundConfig.class);
    }

}
