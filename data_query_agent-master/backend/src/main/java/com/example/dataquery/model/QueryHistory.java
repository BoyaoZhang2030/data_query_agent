package com.example.dataquery.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("query_history")
public class QueryHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String naturalLanguage;
    private String sqlQuery;
    private Integer resultCount;
    private LocalDateTime createdAt;
}