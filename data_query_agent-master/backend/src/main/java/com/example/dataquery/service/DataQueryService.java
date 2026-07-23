package com.example.dataquery.service;

import java.util.Map;
import java.util.List;

public interface DataQueryService {
    Map<String, Object> queryByNaturalLanguage(String naturalLanguage, Long userId);
    Map<String, Object> queryBySql(String sql, Long userId);
    String analyzeQueryResult(String question, List<Map<String, Object>> rows);
}
