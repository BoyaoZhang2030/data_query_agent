package com.example.dataquery.controller;

import com.example.dataquery.service.DataQueryService;
import com.example.dataquery.service.QueryHistoryService;
import com.example.dataquery.util.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/query")
public class DataQueryController {
    @Resource
    private DataQueryService dataQueryService;
    @Resource
    private QueryHistoryService queryHistoryService;

    @PostMapping("/natural-language")
    public Map<String, Object> queryByNaturalLanguage(@RequestBody Map<String, Object> requestData) {
        String naturalLanguage = (String) requestData.get("naturalLanguage");
        Long userId = Long.valueOf(requestData.get("userId").toString());
        Map<String, Object> result = dataQueryService.queryByNaturalLanguage(naturalLanguage, userId);
        return Result.success("查询成功", result);
    }

    @PostMapping("/sql")
    public Map<String, Object> queryBySql(@RequestBody Map<String, Object> requestData) {
        String sql = (String) requestData.get("sql");
        Long userId = Long.valueOf(requestData.get("userId").toString());
        Map<String, Object> result = dataQueryService.queryBySql(sql, userId);
        return Result.success("查询成功", result);
    }

    @GetMapping("/history")
    public Map<String, Object> getQueryHistory(@RequestParam Long userId) {
        return Result.success(queryHistoryService.findByUserId(userId));
    }

    @GetMapping("/templates")
    public Map<String, Object> getQueryTemplates() {
        return Result.success(Map.of(
                "templates", new String[]{
                        "查询所有商品信息",
                        "查询价格大于5000元的商品",
                        "查询库存不足100的商品",
                        "查询销量最高的前10个商品",
                        "查询每个分类下的商品数量",
                        "查询所有订单",
                        "查询已完成的订单",
                        "查询待发货的订单",
                        "查询最近7天的订单",
                        "查询订单总额超过10000元的订单",
                        "查询总销售额",
                        "查询每个商品的销售总额",
                        "查询销售额最高的前10个商品",
                        "查询商品分类统计",
                        "查询所有用户信息",
                        "查询下单次数最多的用户"
                }
        ));
    }
}