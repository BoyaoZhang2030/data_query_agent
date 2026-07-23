package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.QueryHistoryMapper;
import com.example.dataquery.model.QueryHistory;
import com.example.dataquery.service.QueryHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryHistoryServiceImpl extends ServiceImpl<QueryHistoryMapper, QueryHistory> implements QueryHistoryService {
    @Override
    public List<QueryHistory> findByUserId(Long userId) {
        LambdaQueryWrapper<QueryHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueryHistory::getUserId, userId)
               .orderByDesc(QueryHistory::getCreatedAt);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void saveHistory(Long userId, String naturalLanguage, String sqlQuery, int resultCount) {
        QueryHistory history = new QueryHistory();
        history.setUserId(userId);
        history.setNaturalLanguage(naturalLanguage);
        history.setSqlQuery(sqlQuery);
        history.setResultCount(resultCount);
        baseMapper.insert(history);
    }
}