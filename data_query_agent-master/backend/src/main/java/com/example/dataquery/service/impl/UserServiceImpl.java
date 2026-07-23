package com.example.dataquery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dao.UserMapper;
import com.example.dataquery.model.User;
import com.example.dataquery.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public User register(User user) {
        if (user == null || !StringUtils.hasText(user.getUsername())
                || !StringUtils.hasText(user.getPassword()) || !StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("用户名、密码和邮箱不能为空");
        }
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim());
        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            throw new IllegalArgumentException("用户名长度应为 3-50 个字符");
        }
        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于 6 位");
        }
        if (findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, user.getEmail());
        if (baseMapper.selectCount(emailWrapper) > 0) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        user.setRole("user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        baseMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }
        User user = findByUsername(username);
        if (user != null && passwordMatches(password, user.getPassword())) {
            if (!user.getPassword().startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(password));
                baseMapper.updateById(user);
            }
            user.setPassword(null);
            return user;
        }
        return null;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) return false;
        return storedPassword.startsWith("$2")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }
}
