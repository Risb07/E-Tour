package com.etour.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.etour.entity.Customer;
import com.etour.entity.User;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CustomerRepository;
import com.etour.repository.UserRepository;

/**
 * Resolves "who is making this request" from the JWT-authenticated
 * SecurityContext, instead of trusting a customerId/userId supplied in the
 * request body. Every service that acts on behalf of a customer (booking,
 * cart, passengers, reviews) should go through this rather than accepting an
 * ID from the client.
 */
@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public CurrentUserProvider(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    public String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return auth.getName();
    }

    public User currentUser() {
        return userRepository.findByEmail(currentEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    public Customer currentCustomer() {
        return customerRepository.findByUser_Email(currentEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No customer profile for this account. Admin accounts do not have one."));
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
