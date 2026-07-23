package com.example.dataquery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dataquery.model.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    List<Order> findAll();
    List<Order> search(String orderNo, Long userId, String status);
    Order createOrder(Order order);
    boolean deleteOrder(Long id);
}
