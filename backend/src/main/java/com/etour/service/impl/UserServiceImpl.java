package com.etour.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.etour.dto.RegisterRequest;
import com.etour.entity.Role;
import com.etour.entity.User;
import com.etour.repository.RoleRepository;
import com.etour.repository.UserRepository;
import com.etour.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // Use the passwordHash field from your User entity
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setPhone(request.getPhone());
        user.setPreferredLanguage("en");
        user.setStatus(true);
        user.setRole(role);

        return userRepository.save(user);
    }

}