package com.example.dataquery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dataquery.model.Order;
import com.example.dataquery.dto.CreateOrderRequest;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {
    List<Order> findAll();
    List<Order> search(String orderNo, Long userId, String status);
    Order createOrder(CreateOrderRequest request);
    Order updateStatus(Long id, String status);
    List<Map<String, Object>> getItems(Long orderId);
    boolean deleteOrder(Long id);
}
