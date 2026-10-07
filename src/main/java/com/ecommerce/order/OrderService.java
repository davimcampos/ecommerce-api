package com.ecommerce.order;

import com.ecommerce.address.Address;
import com.ecommerce.address.AddressRepository;
import com.ecommerce.cart.Cart;
import com.ecommerce.cart.CartItem;
import com.ecommerce.cart.CartRepository;
import com.ecommerce.common.PageResponse;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.user.User;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUser authenticatedUser;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        User user = authenticatedUser.get();
        Address address = addressRepository.findByIdAndUserId(request.addressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));
        Cart cart = cartRepository.findForCheckoutByUserId(user.getId())
                .filter(found -> !found.getItems().isEmpty())
                .orElseThrow(() -> new BusinessException("O carrinho está vazio"));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(address.formatted())
                .total(BigDecimal.ZERO)
                .build();

        List<CartItem> cartItems = cart.getItems().stream()
                .sorted(Comparator.comparing((CartItem item) -> item.getProduct().getId()))
                .toList();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .filter(Product::isActive)
                    .orElseThrow(() -> new BusinessException("Produto indisponível: " + productId));
            if (productRepository.adjustStock(productId, -cartItem.getQuantity()) == 0) {
                throw new BusinessException("Estoque insuficiente para o produto " + product.getName());
            }
            order.addItem(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotal(total);
        orderRepository.save(order);
        cart.getItems().clear();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = find(id);
        if (!order.getStatus().isCancellable()) {
            throw new BusinessException("Pedido não pode ser cancelado no status " + order.getStatus());
        }
        order.getItems().stream()
                .sorted(Comparator.comparing((OrderItem item) -> item.getProduct().getId()))
                .forEach(item -> productRepository.adjustStock(item.getProduct().getId(), item.getQuantity()));
        order.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> history(Pageable pageable) {
        User user = authenticatedUser.get();
        return PageResponse.of(orderRepository.findByUserId(user.getId(), pageable), OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(find(id));
    }

    private Order find(Long id) {
        User user = authenticatedUser.get();
        return orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }
}
