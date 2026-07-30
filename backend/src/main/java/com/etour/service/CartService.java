package com.etour.service;

import java.util.List;

import com.etour.entity.Cart;

public interface CartService {

    Cart addToCart(Cart cart);

    List<Cart> getAllCart();

    Cart increaseQuantity(Long userId, Long tourId);

    Cart decreaseQuantity(Long userId, Long tourId);

    void removeCart(Long cartId);
}
