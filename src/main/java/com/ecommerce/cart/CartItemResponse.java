package com.ecommerce.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {

    public static CartItemResponse from(CartItem item) {
        BigDecimal price = item.getProduct().getPrice();
        return new CartItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                price,
                item.getQuantity(),
                price.multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
