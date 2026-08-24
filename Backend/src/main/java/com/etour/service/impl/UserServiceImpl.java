package com.etour.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.etour.dto.RegisterRequest;
import com.etour.dto.RegisterResponse;
import com.etour.entity.Customer;
import com.etour.entity.Role;
import com.etour.entity.User;
import com.etour.exception.ResourceConflictException;
import com.etour.repository.CustomerRepository;
import com.etour.repository.RoleRepository;
import com.etour.repository.UserRepository;
import com.etour.security.JwtService;
import com.etour.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_SELF_REGISTER_ROLE = "CUSTOMER";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("An account with this email already exists.");
        }

        // Public self-registration is ALWAYS the CUSTOMER role.
        // Never trust a role coming from the request body here.
        Role role = roleRepository.findByRoleName(DEFAULT_SELF_REGISTER_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role '" + DEFAULT_SELF_REGISTER_ROLE + "' is not seeded in the database"));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setPreferredLanguage("en");
        user.setStatus(true);
        user.setRole(role);

        User saved = userRepository.save(user);

        // Every CUSTOMER-role user needs a Customer profile row for
        // bookings/cart/reviews/passengers to attach to.
        if (DEFAULT_SELF_REGISTER_ROLE.equals(role.getRoleName())) {
            Customer customer = new Customer();
            customer.setUser(saved);
            customer.setFullName((request.getFirstName() + " " + emptyIfNull(request.getLastName())).trim());
            customer.setEmail(saved.getEmail());
            customer.setPhone(saved.getPhone());
            customerRepository.save(customer);
        }

        // Issue the JWT here so the user is logged in immediately after
        // registration - the frontend stores it and redirects straight to
        // the site instead of forcing a second login round trip.
        String token = jwtService.generateToken(saved.getEmail());

        return new RegisterResponse(
                saved.getUserId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRole().getRoleName(),
                token);
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

}