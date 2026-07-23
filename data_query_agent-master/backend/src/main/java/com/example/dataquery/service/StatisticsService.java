package com.example.dataquery.service;

import java.util.Map;

public interface StatisticsService {
    /**
     * 获取数据概览统计
     */
    Map<String, Object> getOverview();
    
    /**
     * 获取商品分类统计
     */
    Map<String, Object> getCategoryStatistics();
    
    /**
     * 获取订单状态统计
     */
    Map<String, Object> getOrderStatusStatistics();
    
    /**
     * 获取销售趋势统计（按天）
     */
    Map<String, Object> getSalesTrend();
    
    /**
     * 获取热销商品TOP10
     */
    Map<String, Object> getTopProducts();
}
