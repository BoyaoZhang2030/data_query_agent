package com.example.dataquery.config;

import com.example.dataquery.model.User;
import com.example.dataquery.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultAccountConfig {
    @Bean
    CommandLineRunner ensureDefaultAccount(UserService userService) {
        return args -> {
            if (userService.findByUsername("bf") == null) {
                User user = new User();
                user.setUsername("bf");
                user.setPassword("123456");
                user.setEmail("bf@dataquery.local");
                user.setRole("admin");
                userService.save(user);
            }
        };
    }
}
