package com.example.dataquery.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL字段名转中文工具类
 * 将SQL查询结果中的英文字段名转换为中文显示名称
 */
@Component
public class SqlFieldMapper {
    
    private static final Map<String, String> FIELD_MAPPING = new HashMap<>();
    
    static {
        // 用户表字段
        FIELD_MAPPING.put("id", "ID");
        FIELD_MAPPING.put("user_id", "用户ID");
        FIELD_MAPPING.put("username", "用户名");
        FIELD_MAPPING.put("email", "邮箱");
        FIELD_MAPPING.put("phone", "手机号");
        FIELD_MAPPING.put("role", "角色");
        FIELD_MAPPING.put("password", "密码");
        FIELD_MAPPING.put("created_at", "创建时间");
        FIELD_MAPPING.put("updated_at", "更新时间");
        
        // 商品分类表字段
        FIELD_MAPPING.put("name", "名称");
        FIELD_MAPPING.put("parent_id", "父级ID");
        FIELD_MAPPING.put("sort_order", "排序");
        
        // 商品表字段
        FIELD_MAPPING.put("product_id", "商品ID");
        FIELD_MAPPING.put("description", "描述");
        FIELD_MAPPING.put("price", "价格");
        FIELD_MAPPING.put("stock", "库存");
        FIELD_MAPPING.put("category_id", "分类ID");
        FIELD_MAPPING.put("sales_count", "销量");
        FIELD_MAPPING.put("category_name", "分类名称");
        
        // 订单表字段
        FIELD_MAPPING.put("order_id", "订单ID");
        FIELD_MAPPING.put("order_no", "订单编号");
        FIELD_MAPPING.put("total_amount", "订单金额");
        FIELD_MAPPING.put("status", "订单状态");
        FIELD_MAPPING.put("payment_method", "支付方式");
        FIELD_MAPPING.put("shipping_address", "收货地址");
        
        // 订单明细表字段
        FIELD_MAPPING.put("quantity", "数量");
        FIELD_MAPPING.put("unit_price", "单价");
        
        // 常见聚合函数字段
        FIELD_MAPPING.put("total_orders", "订单总数");
        FIELD_MAPPING.put("total_spent", "消费总额");
        FIELD_MAPPING.put("total_sales", "销售总额");
        FIELD_MAPPING.put("total_quantity", "总数量");
        FIELD_MAPPING.put("avg_price", "平均价格");
        FIELD_MAPPING.put("max_price", "最高价格");
        FIELD_MAPPING.put("min_price", "最低价格");
        FIELD_MAPPING.put("order_count", "订单数");
        FIELD_MAPPING.put("product_count", "商品数");
        FIELD_MAPPING.put("user_count", "用户数");
        FIELD_MAPPING.put("category_count", "分类数");
        
        // 订单状态相关
        FIELD_MAPPING.put("pending_payment", "待付款");
        FIELD_MAPPING.put("pending_shipment", "待发货");
        FIELD_MAPPING.put("pending_receipt", "待收货");
        FIELD_MAPPING.put("completed", "已完成");
        FIELD_MAPPING.put("cancelled", "已取消");
        
        // 统计相关
        FIELD_MAPPING.put("cnt", "数量");
        FIELD_MAPPING.put("count", "数量");
        FIELD_MAPPING.put("sum_amount", "总金额");
        FIELD_MAPPING.put("avg_amount", "平均金额");
        FIELD_MAPPING.put("total_revenue", "总收入");
        FIELD_MAPPING.put("revenue", "收入");
        FIELD_MAPPING.put("amount", "金额");
    }
    
    /**
     * 将英文字段名转换为中文
     * @param fieldName 英文字段名
     * @return 中文字段名，如果没有映射则返回原字段名
     */
    public static String toChineseFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        
        // 先尝试精确匹配
        String chineseName = FIELD_MAPPING.get(fieldName.toLowerCase());
        if (chineseName != null) {
            return chineseName;
        }
        
        // 如果是聚合函数别名（如 total_spent, avg_price 等），尝试部分匹配
        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            if (fieldName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        // 如果没有找到映射，返回原字段名
        return fieldName;
    }
}
