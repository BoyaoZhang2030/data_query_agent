package com.example.dataquery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dataquery.model.Product;

import java.util.List;

public interface ProductService extends IService<Product> {
    List<Product> findAll();
    List<Product> findByCategoryId(Long categoryId);
}