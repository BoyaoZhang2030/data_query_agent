package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.ProductMapper;
import com.example.dataquery.model.Product;
import com.example.dataquery.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Override
    public List<Product> findAll() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId);
        return baseMapper.selectList(wrapper);
    }
}