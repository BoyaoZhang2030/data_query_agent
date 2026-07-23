package com.example.dataquery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dataquery.model.QueryHistory;

import java.util.List;

public interface QueryHistoryService extends IService<QueryHistory> {
    List<QueryHistory> findByUserId(Long userId);
    void saveHistory(Long userId, String naturalLanguage, String sqlQuery, int resultCount);
}