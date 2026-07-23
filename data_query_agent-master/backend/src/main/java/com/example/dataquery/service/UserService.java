package com.example.dataquery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dataquery.model.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    User register(User user);
    User login(String username, String password);
}