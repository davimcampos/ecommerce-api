package com.ecommerce.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartItemResponse> items, int totalItems, BigDecimal total) {

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::from).toList();
        return new CartResponse(
                items,
                items.stream().mapToInt(CartItemResponse::quantity).sum(),
                items.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public static CartResponse empty() {
        return new CartResponse(List.of(), 0, BigDecimal.ZERO);
    }
}
