package com.momentum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class StockBatchApplication {

    public static void main(String[] args) {
        int exitCode = SpringApplication.exit(SpringApplication.run(StockBatchApplication.class, args));
        System.exit(exitCode);
    }
}
