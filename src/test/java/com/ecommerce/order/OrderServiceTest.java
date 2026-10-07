package com.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.address.Address;
import com.ecommerce.address.AddressRepository;
import com.ecommerce.cart.Cart;
import com.ecommerce.cart.CartItem;
import com.ecommerce.cart.CartRepository;
import com.ecommerce.category.Category;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.user.Role;
import com.ecommerce.user.User;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @InjectMocks
    private OrderService orderService;

    private User user;
    private Address address;
    private Product mouse;
    private Product keyboard;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("joao@email.com").role(Role.USER).build();
        address = Address.builder()
                .id(10L)
                .user(user)
                .street("Rua A")
                .number("100")
                .neighborhood("Centro")
                .city("Belo Horizonte")
                .state("MG")
                .zipCode("30000000")
                .build();
        Category category = Category.builder().id(1L).name("Periféricos").build();
        mouse = Product.builder().id(1L).name("Mouse").price(new BigDecimal("100.00")).stock(10)
                .active(true).category(category).build();
        keyboard = Product.builder().id(2L).name("Teclado").price(new BigDecimal("50.00")).stock(10)
                .active(true).category(category).build();
        cart = Cart.builder().id(1L).user(user).build();
    }

    private void addToCart(Product product, int quantity) {
        cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(quantity).build());
    }

    private void withAddressAndCart() {
        when(authenticatedUser.get()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(address));
        when(cartRepository.findForCheckoutByUserId(1L)).thenReturn(Optional.of(cart));
    }

    @Test
    void shouldCreateOrderFromCartAndDecreaseStock() {
        addToCart(keyboard, 1);
        addToCart(mouse, 2);
        withAddressAndCart();
        when(productRepository.findById(1L)).thenReturn(Optional.of(mouse));
        when(productRepository.findById(2L)).thenReturn(Optional.of(keyboard));
        when(productRepository.adjustStock(1L, -2)).thenReturn(1);
        when(productRepository.adjustStock(2L, -1)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.create(new CreateOrderRequest(10L));

        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(2, response.items().size());
        assertEquals(0, new BigDecimal("250.00").compareTo(response.total()));
        assertTrue(response.shippingAddress().contains("Rua A, 100"));
        assertTrue(cart.getItems().isEmpty());
        verify(productRepository).adjustStock(1L, -2);
        verify(productRepository).adjustStock(2L, -1);
    }

    @Test
    void shouldRejectOrderWithEmptyCart() {
        withAddressAndCart();

        assertThrows(BusinessException.class, () -> orderService.create(new CreateOrderRequest(10L)));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectOrderWithAddressFromAnotherUser() {
        when(authenticatedUser.get()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(new CreateOrderRequest(10L)));
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        addToCart(mouse, 3);
        withAddressAndCart();
        when(productRepository.findById(1L)).thenReturn(Optional.of(mouse));
        when(productRepository.adjustStock(1L, -3)).thenReturn(0);

        assertThrows(BusinessException.class, () -> orderService.create(new CreateOrderRequest(10L)));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectOrderWhenProductIsInactive() {
        mouse.setActive(false);
        addToCart(mouse, 1);
        withAddressAndCart();
        when(productRepository.findById(1L)).thenReturn(Optional.of(mouse));

        assertThrows(BusinessException.class, () -> orderService.create(new CreateOrderRequest(10L)));

        verify(productRepository, never()).adjustStock(anyLong(), anyInt());
    }

    private Order orderWith(OrderStatus status) {
        Order order = Order.builder()
                .id(5L)
                .user(user)
                .status(status)
                .total(new BigDecimal("200.00"))
                .shippingAddress("Rua A, 100")
                .build();
        order.addItem(OrderItem.builder()
                .product(mouse)
                .productName("Mouse")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(2)
                .build());
        return order;
    }

    @Test
    void shouldCancelPendingOrderAndRestoreStock() {
        Order order = orderWith(OrderStatus.PENDING);
        when(authenticatedUser.get()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancel(5L);

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(productRepository).adjustStock(1L, 2);
    }

    @Test
    void shouldNotCancelDeliveredOrder() {
        when(authenticatedUser.get()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(orderWith(OrderStatus.DELIVERED)));

        assertThrows(BusinessException.class, () -> orderService.cancel(5L));

        verify(productRepository, never()).adjustStock(anyLong(), anyInt());
    }

    @Test
    void shouldFailWhenOrderBelongsToAnotherUser() {
        when(authenticatedUser.get()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.get(5L));
    }
}
