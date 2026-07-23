package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.ProductMapper;
import com.example.dataquery.model.Product;
import com.example.dataquery.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;

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

    @Override
    public Product adjustStock(Long id, int change) {
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .ge(change < 0, Product::getStock, -change)
                .setSql("stock = stock + " + change);
        if (baseMapper.update(null, wrapper) == 0) {
            return null;
        }
        return baseMapper.selectById(id);
    }

    @Override
    public Product updatePrice(Long id, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0 || baseMapper.selectById(id) == null) return null;
        Product product = new Product();
        product.setId(id);
        product.setPrice(price);
        baseMapper.updateById(product);
        return baseMapper.selectById(id);
    }
}
