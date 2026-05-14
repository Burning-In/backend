package com.momentum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class StockRealtimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockRealtimeApplication.class, args);
    }
}
