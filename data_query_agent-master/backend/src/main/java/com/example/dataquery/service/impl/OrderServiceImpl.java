package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.OrderMapper;
import com.example.dataquery.dao.OrderItemMapper;
import com.example.dataquery.model.Order;
import com.example.dataquery.model.OrderItem;
import com.example.dataquery.model.Product;
import com.example.dataquery.dao.ProductMapper;
import com.example.dataquery.dto.CreateOrderRequest;
import com.example.dataquery.dto.OrderProductRequest;
import com.example.dataquery.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ProductMapper productMapper;

    @Override
    public List<Order> findAll() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<Order> search(String orderNo, Long userId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(orderNo != null && !orderNo.isBlank(), Order::getOrderNo, orderNo)
                .eq(userId != null, Order::getUserId, userId)
                .eq(status != null && !status.isBlank(), Order::getStatus, status)
                .orderByDesc(Order::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        boolean purchase = "PURCHASE".equalsIgnoreCase(request.getOrderType());
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderProductRequest item : request.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品和购买数量必须有效");
            }
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productMapper.selectById(entry.getKey());
            if (product == null) {
                throw new IllegalArgumentException("商品不存在，ID：" + entry.getKey());
            }
            int quantity = entry.getValue();
            BigDecimal itemPrice = purchase
                    ? request.getItems().stream().filter(i -> product.getId().equals(i.getProductId())).findFirst().map(OrderProductRequest::getUnitPrice).orElse(null)
                    : product.getPrice();
            if (itemPrice == null || itemPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("商品“" + product.getName() + "”的采购单价无效");
            }
            int changed = productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, product.getId())
                    .ge(!purchase, Product::getStock, quantity)
                    .setSql("stock = stock " + (purchase ? "+ " : "- ") + quantity));
            if (changed == 0) {
                throw new IllegalArgumentException("商品“" + product.getName() + "”库存不足，当前库存：" + product.getStock());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(itemPrice);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        Order order = new Order();
        order.setOrderNo(request.getOrderNo());
        order.setUserId(request.getUserId());
        order.setStatus(request.getStatus());
        order.setPaymentMethod(purchase ? "PURCHASE" : "SALES");
        order.setShippingAddress(request.getRemark());
        order.setTotalAmount(totalAmount);
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        baseMapper.insert(order);
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return order;
    }

    @Override
    public Order updateStatus(Long id, String status) {
        Order order = baseMapper.selectById(id);
        if (order == null) {
            return null;
        }
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(order);
        return order;
    }

    @Override
    public List<Map<String, Object>> getItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId))
                .stream().map(item -> {
                    Product product = productMapper.selectById(item.getProductId());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("productId", item.getProductId());
                    row.put("productName", product == null ? "已删除商品" : product.getName());
                    row.put("quantity", item.getQuantity());
                    row.put("price", item.getPrice());
                    row.put("subtotal", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return row;
                }).toList();
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id));
        if (baseMapper.selectById(id) == null) {
            return false;
        }
        for (OrderItem item : items) {
            Order order = baseMapper.selectById(id);
            boolean purchase = order != null && "PURCHASE".equals(order.getPaymentMethod());
            int changed = productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, item.getProductId())
                    .ge(purchase, Product::getStock, item.getQuantity())
                    .setSql("stock = stock " + (purchase ? "- " : "+ ") + item.getQuantity()));
            if (changed == 0) throw new IllegalArgumentException("库存不足，无法删除该入库订单");
        }
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        return baseMapper.deleteById(id) > 0;
    }
}
