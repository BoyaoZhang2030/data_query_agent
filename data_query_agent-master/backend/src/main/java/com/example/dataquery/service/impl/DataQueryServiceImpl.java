package com.example.dataquery.service.impl;

import com.example.dataquery.service.DataQueryService;
import com.example.dataquery.service.QueryHistoryService;
import com.example.dataquery.util.SqlFieldMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DataQueryServiceImpl implements DataQueryService {
    private static final Logger log = LoggerFactory.getLogger(DataQueryServiceImpl.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private QueryHistoryService queryHistoryService;

    @Value("${deepseek.api-key:}")
    private String deepSeekApiKey;
    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String deepSeekBaseUrl;
    @Value("${deepseek.model:deepseek-v4-flash}")
    private String deepSeekModel;

    private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
        ".*\\b(DROP|DELETE|UPDATE|INSERT|ALTER|CREATE|TRUNCATE|REPLACE|GRANT|REVOKE|LOCK|UNLOCK|EXEC|EXECUTE|xp_|sp_)\\b.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile(
        ".*(--|/\\*|\\*/|\\|\\|).*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BUSINESS_TABLE_PATTERN = Pattern.compile(
        "\\b(users|categories|products|orders|order_items|query_history)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final int MAX_SQL_LENGTH = 5000;

    private static final String SQL_GENERATION_PROMPT = """
        你是一个专业的电商数据分析助手，擅长将用户的自然语言问题转换为准确的MySQL SQL查询。

        ## 数据库表结构

        **用户表 users**
        - id: 用户ID
        - username: 用户名
        - email: 邮箱
        - phone: 手机号
        - role: 角色（admin/user）
        - created_at: 创建时间
        - updated_at: 更新时间

        **商品分类表 categories**
        - id: 分类ID
        - name: 分类名称
        - parent_id: 父分类ID（用于层级分类）
        - sort_order: 排序顺序
        - created_at: 创建时间
        - updated_at: 更新时间

        **商品表 products**
        - id: 商品ID
        - name: 商品名称
        - description: 商品描述
        - price: 价格（DECIMAL）
        - stock: 库存数量
        - category_id: 分类ID
        - sales_count: 销售数量
        - created_at: 创建时间
        - updated_at: 更新时间

        **订单表 orders**
        - id: 订单ID
        - order_no: 订单编号
        - user_id: 用户ID
        - total_amount: 订单总额（DECIMAL）
        - status: 订单状态（待付款/待发货/待收货/已完成/已取消）
        - payment_method: 支付方式
        - shipping_address: 收货地址
        - created_at: 创建时间
        - updated_at: 更新时间

        **订单明细表 order_items**
        - id: 明细ID
        - order_id: 订单ID
        - product_id: 商品ID
        - quantity: 数量
        - price: 单价（DECIMAL）
        - created_at: 创建时间

        **查询历史表 query_history**
        - id: 历史记录ID
        - user_id: 用户ID
        - natural_language: 自然语言查询
        - sql_query: SQL查询语句
        - result_count: 结果数量
        - created_at: 创建时间

        ## 查询要求

        1. 始终只返回SELECT查询语句
        2. 不要包含任何注释、解释或markdown标记
        3. 直接返回SQL语句本身
        4. 注意金额字段使用DECIMAL类型，比较时注意精度
        5. 不要在结果中返回用户的password字段
        6. 对于聚合函数或计算字段，请使用有意义的英文别名（AS关键字），例如：
           - COUNT(*) AS order_count
           - SUM(total_amount) AS total_sales
           - AVG(price) AS avg_price
           - COUNT(DISTINCT user_id) AS user_count
        7. 多表关联查询时，使用表别名提高可读性

        请将以下自然语言问题转换为SQL：
        {query}
        """;

    @Override
    public Map<String, Object> queryByNaturalLanguage(String naturalLanguage, Long userId) {
        if (naturalLanguage == null || naturalLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("查询语句不能为空");
        }

        if (naturalLanguage.length() > 1000) {
            throw new IllegalArgumentException("查询语句过长，请控制在1000字符以内");
        }

        String sql = generateSqlFromNaturalLanguage(naturalLanguage);
        log.info("AI生成的SQL: {}", sql);

        if (sql.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("生成的SQL过长，可能存在异常");
        }

        if (DANGEROUS_SQL_PATTERN.matcher(sql).matches()) {
            log.warn("检测到危险SQL操作，已拦截: {}", sql);
            throw new IllegalArgumentException("生成的SQL包含危险操作，已被拦截");
        }

        if (SQL_COMMENT_PATTERN.matcher(sql).matches()) {
            log.warn("检测到SQL注释符或特殊字符，已拦截: {}", sql);
            throw new IllegalArgumentException("SQL语句包含非法字符");
        }

        if (!sql.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("只允许执行SELECT查询");
        }

        if (!BUSINESS_TABLE_PATTERN.matcher(sql).find()) {
            throw new IllegalArgumentException("请输入具体的数据查询问题，例如：查询销量最高的前10个商品");
        }

        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            // 将字段名转换为中文
            List<Map<String, Object>> convertedResult = convertFieldNamesToChinese(result);
            queryHistoryService.saveHistory(userId, naturalLanguage, sql, result.size());
            return Map.of("sql", sql, "result", convertedResult, "count", result.size());
        } catch (Exception e) {
            log.error("SQL执行失败: {}", e.getMessage());
            throw new RuntimeException("查询执行失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> queryBySql(String sql, Long userId) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        sql = sql.trim();

        if (sql.length() > MAX_SQL_LENGTH) {
            throw new IllegalArgumentException("SQL语句过长，请控制在" + MAX_SQL_LENGTH + "字符以内");
        }

        if (DANGEROUS_SQL_PATTERN.matcher(sql).matches()) {
            log.warn("检测到危险SQL操作，已拦截: {}", sql);
            throw new IllegalArgumentException("不允许执行DROP、DELETE、UPDATE等危险操作");
        }

        if (SQL_COMMENT_PATTERN.matcher(sql).matches()) {
            log.warn("检测到SQL注释符或特殊字符，已拦截: {}", sql);
            throw new IllegalArgumentException("SQL语句包含非法字符");
        }

        if (!sql.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("只允许执行SELECT查询");
        }

        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            // 将字段名转换为中文
            List<Map<String, Object>> convertedResult = convertFieldNamesToChinese(result);
            queryHistoryService.saveHistory(userId, "SQL查询", sql, result.size());
            return Map.of("sql", sql, "result", convertedResult, "count", result.size());
        } catch (Exception e) {
            log.error("SQL执行失败: {}", e.getMessage());
            throw new RuntimeException("查询执行失败: " + e.getMessage());
        }
    }

    /**
     * 将查询结果中的英文字段名转换为中文
     * @param originalResult 原始查询结果
     * @return 转换后的查询结果
     */
    private List<Map<String, Object>> convertFieldNamesToChinese(List<Map<String, Object>> originalResult) {
        if (originalResult == null || originalResult.isEmpty()) {
            return originalResult;
        }
        
        return originalResult.stream()
            .map(row -> {
                Map<String, Object> convertedRow = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String chineseFieldName = SqlFieldMapper.toChineseFieldName(entry.getKey());
                    convertedRow.put(chineseFieldName, entry.getValue());
                }
                return convertedRow;
            })
            .collect(Collectors.toList());
    }

    private String generateSqlFromNaturalLanguage(String naturalLanguage) {
        try {
            if (deepSeekApiKey == null || deepSeekApiKey.isBlank()) {
                throw new IllegalStateException("DeepSeek API Key 未配置，请设置环境变量 DEEPSEEK_API_KEY");
            }
            String systemPrompt = SQL_GENERATION_PROMPT.replace(
                "请将以下自然语言问题转换为SQL：\n        {query}",
                "只处理电商数据库查询。若输入不是明确的数据查询，也必须要求用户提供具体问题，禁止用SELECT字符串常量模拟回答。"
            );
            Map<?, ?> response = RestClient.builder()
                .baseUrl(deepSeekBaseUrl)
                .defaultHeader("Authorization", "Bearer " + deepSeekApiKey)
                .build().post().uri("/chat/completions")
                .body(Map.of(
                    "model", deepSeekModel,
                    "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", naturalLanguage)
                    ),
                    "temperature", 0
                ))
                .retrieve().body(Map.class);
            String sql = extractDeepSeekContent(response);

            sql = sql.replaceAll("```sql", "").replaceAll("```", "").replaceAll("`", "").trim();

            if (sql.isEmpty()) {
                throw new RuntimeException("AI未能生成有效的SQL语句");
            }

            return sql;
        } catch (Exception e) {
            log.error("AI生成SQL失败: {}", e.getMessage());
            throw new RuntimeException("AI生成SQL失败: " + e.getMessage());
        }
    }

    private String extractDeepSeekContent(Map<?, ?> response) {
        if (response == null || !(response.get("choices") instanceof List<?> choices) || choices.isEmpty()) {
            throw new RuntimeException("DeepSeek 返回了空响应");
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choice)
            || !(choice.get("message") instanceof Map<?, ?> message)
            || !(message.get("content") instanceof String content)) {
            throw new RuntimeException("无法解析 DeepSeek 响应");
        }
        return content;
    }

    @Override
    public String analyzeQueryResult(String question, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "本次查询没有返回数据，暂时无法形成趋势结论。建议调整筛选条件或扩大时间范围。";
        }

        List<Map<String, Object>> sample = rows.stream().limit(50).toList();
        if (deepSeekApiKey == null || deepSeekApiKey.isBlank()) {
            return buildLocalSummary(rows);
        }

        try {
            String prompt = """
                你是一名电商数据分析师。请根据用户问题和查询结果，用中文输出简洁、客观的分析。
                要求：先给出核心结论，再列出2-4条数据洞察，最后给出一条可执行建议；不要编造结果中不存在的数据。
                用户问题：%s
                查询结果（最多50行）：%s
                """.formatted(question, sample);
            Map<?, ?> response = RestClient.builder()
                    .baseUrl(deepSeekBaseUrl)
                    .defaultHeader("Authorization", "Bearer " + deepSeekApiKey)
                    .build().post().uri("/chat/completions")
                    .body(Map.of(
                            "model", deepSeekModel,
                            "messages", List.of(Map.of("role", "user", "content", prompt)),
                            "temperature", 0.2
                    ))
                    .retrieve().body(Map.class);
            return extractDeepSeekContent(response).trim();
        } catch (Exception e) {
            log.warn("AI analysis failed, using local summary: {}", e.getMessage());
            return buildLocalSummary(rows);
        }
    }

    private String buildLocalSummary(List<Map<String, Object>> rows) {
        Set<String> fields = rows.get(0).keySet();
        List<String> numericFields = fields.stream()
                .filter(field -> rows.stream().anyMatch(row -> row.get(field) instanceof Number))
                .toList();
        StringBuilder summary = new StringBuilder("本次查询共返回 ")
                .append(rows.size()).append(" 条记录，包含字段：")
                .append(String.join("、", fields)).append("。");
        for (String field : numericFields.stream().limit(3).toList()) {
            DoubleSummaryStatistics stats = rows.stream()
                    .map(row -> row.get(field))
                    .filter(Number.class::isInstance)
                    .mapToDouble(value -> ((Number) value).doubleValue())
                    .summaryStatistics();
            summary.append("\n- ").append(field).append("：合计 ")
                    .append(String.format(Locale.ROOT, "%.2f", stats.getSum()))
                    .append("，平均 ").append(String.format(Locale.ROOT, "%.2f", stats.getAverage()))
                    .append("，范围 ").append(String.format(Locale.ROOT, "%.2f - %.2f", stats.getMin(), stats.getMax()))
                    .append("。");
        }
        summary.append("\n建议结合业务目标继续按时间、分类或商品维度细分，以定位主要贡献项和异常点。");
        return summary.toString();
    }
}
