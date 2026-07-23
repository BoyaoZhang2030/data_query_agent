package com.example.dataquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.dataquery.dao")
public class DataQueryAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataQueryAgentApplication.class, args);
    }
}