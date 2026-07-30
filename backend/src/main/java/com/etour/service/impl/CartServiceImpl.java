package com.etour.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.etour.entity.Cart;
import com.etour.repository.CartRepository;
import com.etour.service.CartService;

import jakarta.validation.Valid;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository repository;

    @Override
    public Cart addToCart(@Valid @RequestBody Cart cart) {

        Cart existing = repository.findByUserIdAndTourId(
                cart.getUserId(),
                cart.getTourId());

        if (existing != null) {

            existing.setQuantity(existing.getQuantity() + 1);

            return repository.save(existing);
        }

        cart.setQuantity(1);

        return repository.save(cart);
    }

    @Override
    public List<Cart> getAllCart() {
        return repository.findAll();
    }

    @Override
    public Cart increaseQuantity(Long userId, Long tourId) {

        Cart cart = repository.findByUserIdAndTourId(userId, tourId);

        if (cart != null) {

            cart.setQuantity(cart.getQuantity() + 1);

            return repository.save(cart);
        }

        return null;
    }

    @Override
    public Cart decreaseQuantity(Long userId, Long tourId) {

        Cart cart = repository.findByUserIdAndTourId(userId, tourId);

        if (cart != null && cart.getQuantity() > 1) {

            cart.setQuantity(cart.getQuantity() - 1);

            return repository.save(cart);
        }

        return cart;
    }

    @Override
    public void removeCart(Long cartId) {

        repository.deleteById(cartId);
    }
}