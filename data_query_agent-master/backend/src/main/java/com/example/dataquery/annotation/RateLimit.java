package com.example.dataquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流键前缀
     */
    String key() default "rate_limit";
    
    /**
     * 时间窗口（秒）
     */
    int timeWindow() default 60;
    
    /**
     * 时间窗口内最大请求次数
     */
    int maxRequests() default 100;
}
