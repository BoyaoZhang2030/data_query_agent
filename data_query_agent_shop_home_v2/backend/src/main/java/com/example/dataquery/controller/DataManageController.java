package com.example.dataquery.controller;

import com.example.dataquery.service.CategoryService;
import com.example.dataquery.service.ProductService;
import com.example.dataquery.service.OrderService;
import com.example.dataquery.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;
import com.example.dataquery.model.Order;
import com.example.dataquery.model.Product;
import com.example.dataquery.dto.CreateOrderRequest;
import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/data")
public class DataManageController {
    private static final Set<String> ORDER_STATUSES = Set.of(
            "待付款", "已付款", "待发货", "待收货", "已完成", "已取消", "已入库"
    );
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

    @PostMapping("/products")
    public Map<String, Object> createProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().isBlank()
                || product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0
                || product.getStock() == null || product.getStock() < 0
                || product.getCategoryId() == null) {
            return Result.error(400, "商品名称、非负价格、非负库存和商品分类不能为空");
        }
        if (categoryService.getById(product.getCategoryId()) == null) {
            return Result.error(400, "所选商品分类不存在");
        }
        product.setId(null);
        productService.save(product);
        return Result.success("商品添加成功", product);
    }

    @PutMapping("/products/{id}/stock")
    public Map<String, Object> adjustProductStock(@PathVariable Long id,
                                                   @RequestBody Map<String, Integer> request) {
        Integer change = request.get("change");
        if (change == null || change == 0) {
            return Result.error(400, "库存变动数量不能为0");
        }
        Product product = productService.adjustStock(id, change);
        if (product == null) {
            return Result.error(400, "商品不存在或出库数量超过当前库存");
        }
        return Result.success(change > 0 ? "商品入库成功" : "商品出库成功", product);
    }

    @PutMapping("/products/{id}/price")
    public Map<String, Object> updateProductPrice(@PathVariable Long id,
                                                   @RequestBody Map<String, BigDecimal> request) {
        Product product = productService.updatePrice(id, request.get("price"));
        if (product == null) return Result.error(400, "商品不存在或价格无效");
        return Result.success("商品价格修改成功", product);
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

    @GetMapping("/orders/{id}/items")
    public Map<String, Object> getOrderItems(@PathVariable Long id) {
        return Result.success(orderService.getItems(id));
    }

    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody CreateOrderRequest request) {
        if (request.getUserId() == null || request.getStatus() == null || request.getStatus().isBlank()
                || request.getItems() == null || request.getItems().isEmpty()) {
            return Result.error(400, "用户ID、订单状态和商品明细不能为空");
        }
        if (!ORDER_STATUSES.contains(request.getStatus())) {
            return Result.error(400, "无效的订单状态");
        }
        return Result.success("订单新增成功", orderService.createOrder(request));
    }

    @PutMapping("/orders/{id}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || !ORDER_STATUSES.contains(status)) {
            return Result.error(400, "无效的订单状态");
        }
        Order order = orderService.updateStatus(id, status);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success("订单状态修改成功", order);
    }

    @DeleteMapping("/orders/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        if (!orderService.deleteOrder(id)) {
            return Result.error(404, "订单不存在");
        }
        return Result.success("订单删除成功", id);
    }
}
