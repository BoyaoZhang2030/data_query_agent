package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.CategoryMapper;
import com.example.dataquery.model.Category;
import com.example.dataquery.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public List<Category> findAll() {
        return baseMapper.selectList(null);
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId);
        return baseMapper.selectList(wrapper);
    }
}