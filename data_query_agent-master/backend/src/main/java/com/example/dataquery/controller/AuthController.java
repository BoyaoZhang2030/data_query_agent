package com.example.dataquery.controller;

import com.example.dataquery.model.User;
import com.example.dataquery.service.UserService;
import com.example.dataquery.util.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        return Result.success("注册成功", userService.register(user));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        User user = userService.login(username, password);
        if (user != null) {
            return Result.success("登录成功", user);
        } else {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestParam Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.error(404, "用户不存在");
        }
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody User user) {
        userService.updateById(user);
        return Result.success(user);
    }
}