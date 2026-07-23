package com.example.dataquery.service.impl;

import com.example.dataquery.service.StatisticsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        
        // 商品总数
        Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        result.put("productCount", productCount);
        
        // 订单总数
        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        result.put("orderCount", orderCount);
        
        // 分类总数
        Integer categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
        result.put("categoryCount", categoryCount);
        
        // 总销售额
        Double totalSales = jdbcTemplate.queryForObject("SELECT SUM(total_amount) FROM orders WHERE status = '已完成'", Double.class);
        result.put("totalSales", totalSales != null ? totalSales : 0.0);

        // 交易额按全部订单计算，确保新增或删除订单后首页指标立即同步变化
        Double transactionAmount = jdbcTemplate.queryForObject("SELECT SUM(total_amount) FROM orders", Double.class);
        result.put("transactionAmount", transactionAmount != null ? transactionAmount : 0.0);
        
        // 用户总数
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        result.put("userCount", userCount);
        
        return result;
    }

    @Override
    public Map<String, Object> getCategoryStatistics() {
        List<Map<String, Object>> categoryStats = jdbcTemplate.queryForList(
            "SELECT c.name as categoryName, COUNT(p.id) as productCount " +
            "FROM categories c " +
            "LEFT JOIN products p ON c.id = p.category_id " +
            "WHERE c.parent_id IS NOT NULL " +
            "GROUP BY c.id, c.name " +
            "ORDER BY productCount DESC"
        );
        return Map.of("categoryStats", categoryStats);
    }

    @Override
    public Map<String, Object> getOrderStatusStatistics() {
        List<Map<String, Object>> orderStatusStats = jdbcTemplate.queryForList(
            "SELECT status, COUNT(*) as count, SUM(total_amount) as totalAmount " +
            "FROM orders " +
            "GROUP BY status " +
            "ORDER BY count DESC"
        );
        return Map.of("orderStatusStats", orderStatusStats);
    }

    @Override
    public Map<String, Object> getSalesTrend() {
        List<Map<String, Object>> salesTrend = jdbcTemplate.queryForList(
            "SELECT DATE(created_at) as date, COUNT(*) as orderCount, SUM(total_amount) as salesAmount " +
            "FROM orders " +
            "WHERE status = '已完成' " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date DESC " +
            "LIMIT 30"
        );
        return Map.of("salesTrend", salesTrend);
    }

    @Override
    public Map<String, Object> getTopProducts() {
        List<Map<String, Object>> topProducts = jdbcTemplate.queryForList(
            "SELECT p.id, p.name, p.price, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.price) as totalSales " +
            "FROM products p " +
            "INNER JOIN order_items oi ON p.id = oi.product_id " +
            "INNER JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.status = '已完成' " +
            "GROUP BY p.id, p.name, p.price " +
            "ORDER BY totalQuantity DESC " +
            "LIMIT 10"
        );
        return Map.of("topProducts", topProducts);
    }
}
