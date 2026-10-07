package com.ecommerce.cart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Carrinho")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Consulta o carrinho do usuário autenticado")
    public CartResponse get() {
        return cartService.get();
    }

    @PostMapping("/items")
    @Operation(summary = "Adiciona um produto ao carrinho")
    public CartResponse addItem(@Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(request);
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Altera a quantidade de um produto no carrinho")
    public CartResponse updateQuantity(
            @PathVariable Long productId, @Valid @RequestBody CartItemQuantityRequest request) {
        return cartService.updateQuantity(productId, request);
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove um produto do carrinho")
    public CartResponse removeItem(@PathVariable Long productId) {
        return cartService.removeItem(productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Esvazia o carrinho")
    public void clear() {
        cartService.clear();
    }
}
