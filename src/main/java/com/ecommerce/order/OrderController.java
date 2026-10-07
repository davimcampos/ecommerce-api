package com.ecommerce.order;

import com.ecommerce.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um pedido a partir do carrinho e baixa o estoque")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancela um pedido e devolve os itens ao estoque")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @GetMapping
    @Operation(summary = "Histórico paginado de pedidos do usuário autenticado")
    public PageResponse<OrderResponse> history(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return orderService.history(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um pedido do usuário autenticado")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }
}
