package com.example.dataquery.util;

import java.util.Map;

public class Result {
    public static Map<String, Object> success(Object data) {
        return Map.of("code", 200, "message", "操作成功", "data", data);
    }

    public static Map<String, Object> success(String message, Object data) {
        return Map.of("code", 200, "message", message, "data", data);
    }

    public static Map<String, Object> error(Integer code, String message) {
        return Map.of("code", code, "message", message, "data", Map.of());
    }

    public static Map<String, Object> error(String message) {
        return Map.of("code", 500, "message", message, "data", Map.of());
    }
}