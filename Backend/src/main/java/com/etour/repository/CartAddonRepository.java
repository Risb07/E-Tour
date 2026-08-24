package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.CartAddon;

public interface CartAddonRepository extends JpaRepository<CartAddon, Long> {
    List<CartAddon> findByCart_CartId(Long cartId);
    void deleteByCart_CartId(Long cartId);
}
