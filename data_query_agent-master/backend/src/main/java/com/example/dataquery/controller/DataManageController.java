package com.example.dataquery.controller;

import com.example.dataquery.service.CategoryService;
import com.example.dataquery.service.ProductService;
import com.example.dataquery.service.OrderService;
import com.example.dataquery.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;
import com.example.dataquery.model.Order;
import java.math.BigDecimal;

@RestController
@RequestMapping("/data")
public class DataManageController {
    @Resource
    private ProductService productService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private OrderService orderService;

    @GetMapping("/products")
    public Map<String, Object> getProducts(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return Result.success(productService.findByCategoryId(categoryId));
        } else {
            return Result.success(productService.findAll());
        }
    }

    @GetMapping("/categories")
    public Map<String, Object> getCategories(@RequestParam(required = false) Long parentId) {
        if (parentId != null) {
            return Result.success(categoryService.findByParentId(parentId));
        } else {
            return Result.success(categoryService.findAll());
        }
    }

    @GetMapping("/orders")
    public Map<String, Object> getOrders(@RequestParam(required = false) String orderNo,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String status) {
        return Result.success(orderService.search(orderNo, userId, status));
    }

    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody Order order) {
        if (order.getUserId() == null || order.getTotalAmount() == null
                || order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0
                || order.getStatus() == null || order.getStatus().isBlank()) {
            return Result.error(400, "用户ID、非负订单金额和订单状态不能为空");
        }
        return Result.success("订单新增成功", orderService.createOrder(order));
    }

    @DeleteMapping("/orders/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        if (!orderService.deleteOrder(id)) {
            return Result.error(404, "订单不存在");
        }
        return Result.success("订单删除成功", id);
    }
}
