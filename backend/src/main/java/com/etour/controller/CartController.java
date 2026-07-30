package com.etour.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.etour.entity.Cart;

import jakarta.validation.Valid;

import com.etour.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    CartService service;

    @PostMapping("/add")
    public Cart addToCart(@Valid @RequestBody Cart cart) {
        return service.addToCart(cart);
    }

    @GetMapping
    public List<Cart> getAllCart() {
        return service.getAllCart();
    }

    @PutMapping("/increase/{userId}/{tourId}")
    public Cart increase(@PathVariable Long userId,
            @PathVariable Long tourId) {

        return service.increaseQuantity(userId, tourId);
    }

    @PutMapping("/decrease/{userId}/{tourId}")
    public Cart decrease(@PathVariable Long userId,
            @PathVariable Long tourId) {

        return service.decreaseQuantity(userId, tourId);
    }

    @DeleteMapping("/{cartId}")
    public void delete(@PathVariable Long cartId) {
        service.removeCart(cartId);
    }
}