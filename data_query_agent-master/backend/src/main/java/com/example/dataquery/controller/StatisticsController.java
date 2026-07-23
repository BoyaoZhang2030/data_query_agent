package com.example.dataquery.controller;

import com.example.dataquery.service.StatisticsService;
import com.example.dataquery.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Resource
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/category")
    public Map<String, Object> getCategoryStatistics() {
        return Result.success(statisticsService.getCategoryStatistics());
    }

    @GetMapping("/order-status")
    public Map<String, Object> getOrderStatusStatistics() {
        return Result.success(statisticsService.getOrderStatusStatistics());
    }

    @GetMapping("/sales-trend")
    public Map<String, Object> getSalesTrend() {
        return Result.success(statisticsService.getSalesTrend());
    }

    @GetMapping("/top-products")
    public Map<String, Object> getTopProducts() {
        return Result.success(statisticsService.getTopProducts());
    }
}