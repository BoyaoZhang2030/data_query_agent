package com.example.dataquery.config;

import com.example.dataquery.util.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLException.class)
    public Map<String, Object> handleSQLException(SQLException e) {
        return Result.error(400, "SQL执行错误: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        return Result.error("系统错误: " + e.getMessage());
    }
}
