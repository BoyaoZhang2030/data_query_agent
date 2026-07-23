package com.example.dataquery.service;

import java.util.Map;

public interface DataQueryService {
    Map<String, Object> queryByNaturalLanguage(String naturalLanguage, Long userId);
    Map<String, Object> queryBySql(String sql, Long userId);
}