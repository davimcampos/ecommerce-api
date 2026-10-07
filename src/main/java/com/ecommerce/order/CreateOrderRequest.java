package com.ecommerce.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "O endereço de entrega é obrigatório")
        Long addressId) {
}
