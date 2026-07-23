package com.example.dataquery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dataquery.model.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> findAll();
    List<Category> findByParentId(Long parentId);
}