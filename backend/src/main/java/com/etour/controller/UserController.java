package com.etour.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.RegisterRequest;
import com.etour.entity.User;
import com.etour.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {

        return userService.register(request);

    }

}