package com.example.dataquery.interceptor;

import com.example.dataquery.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 请求限流拦截器
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return true;
        }
        
        // 获取客户端IP作为限流key
        String clientIp = getClientIp(request);
        String method = request.getRequestURI();
        String key = rateLimit.key() + ":" + clientIp + ":" + method;
        
        // 获取当前请求次数
        String countStr = stringRedisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        
        if (count >= rateLimit.maxRequests()) {
            log.warn("请求限流：IP={}, URI={}, 已超过限制({}/{})", 
                    clientIp, method, count, rateLimit.maxRequests());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }
        
        // 增加计数
        if (count == 0) {
            // 第一次请求，设置过期时间
            stringRedisTemplate.opsForValue().set(key, "1", rateLimit.timeWindow(), TimeUnit.SECONDS);
        } else {
            // 已存在，递增计数
            stringRedisTemplate.opsForValue().increment(key);
        }
        
        return true;
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
