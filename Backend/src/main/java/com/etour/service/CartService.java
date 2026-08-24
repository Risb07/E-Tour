package com.etour.service;

import java.util.List;

import com.etour.dto.BookingResponse;
import com.etour.dto.CartRequest;
import com.etour.dto.CartResponse;

public interface CartService {

    CartResponse addToCart(CartRequest request);

    List<CartResponse> getMyCart();

    void removeFromCart(Long cartId);

    // BRD Book-Tour flow: Select Tour -> Add Pax -> Select Add-ons -> Done -> Pay
    // This is the "Done" step: cart becomes a real booking.
    BookingResponse checkout(Long cartId);
}
