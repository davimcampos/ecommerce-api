package com.ecommerce.product;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull(message = "A variação de estoque é obrigatória")
        Integer delta) {
}
