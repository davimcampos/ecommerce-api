package com.ecommerce.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        boolean active,
        Long categoryId,
        String categoryName) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.isActive(),
                product.getCategory().getId(),
                product.getCategory().getName());
    }
}
