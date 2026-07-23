package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.OrderMapper;
import com.example.dataquery.dao.OrderItemMapper;
import com.example.dataquery.model.Order;
import com.example.dataquery.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Resource
    private OrderItemMapper orderItemMapper;

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
    public Order createOrder(Order order) {
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        baseMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        orderItemMapper.delete(new LambdaQueryWrapper<com.example.dataquery.model.OrderItem>()
                .eq(com.example.dataquery.model.OrderItem::getOrderId, id));
        return baseMapper.deleteById(id) > 0;
    }
}
