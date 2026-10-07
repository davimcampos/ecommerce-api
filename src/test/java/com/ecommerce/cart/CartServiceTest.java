package com.ecommerce.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("joao@email.com").role(Role.USER).build();
        product = Product.builder()
                .id(1L)
                .name("Mouse")
                .price(new BigDecimal("100.00"))
                .stock(5)
                .active(true)
                .category(Category.builder().id(1L).name("Periféricos").build())
                .build();
        cart = Cart.builder().id(1L).user(user).build();
    }

    private void withExistingCart() {
        when(authenticatedUser.get()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }

    private void withItem(int quantity) {
        cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(quantity).build());
    }

    @Test
    void shouldReturnEmptyCartWhenUserHasNone() {
        when(authenticatedUser.get()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        CartResponse response = cartService.get();

        assertTrue(response.items().isEmpty());
        assertEquals(0, response.totalItems());
    }

    @Test
    void shouldAddNewProductToCart() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        withExistingCart();

        CartResponse response = cartService.addItem(new CartItemRequest(1L, 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.totalItems());
        assertEquals(0, new BigDecimal("200.00").compareTo(response.total()));
    }

    @Test
    void shouldCreateCartWhenUserHasNone() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(authenticatedUser.get()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(new CartItemRequest(1L, 1));

        verify(cartRepository).save(any(Cart.class));
        assertEquals(1, response.totalItems());
    }

    @Test
    void shouldMergeQuantityWhenProductIsAlreadyInCart() {
        withItem(2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        withExistingCart();

        CartResponse response = cartService.addItem(new CartItemRequest(1L, 2));

        assertEquals(1, response.items().size());
        assertEquals(4, response.totalItems());
    }

    @Test
    void shouldRejectAddingMoreThanAvailableStock() {
        withItem(4);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        withExistingCart();

        assertThrows(BusinessException.class, () -> cartService.addItem(new CartItemRequest(1L, 2)));
    }

    @Test
    void shouldRejectUnknownProduct() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(new CartItemRequest(9L, 1)));
    }

    @Test
    void shouldRejectInactiveProduct() {
        product.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(new CartItemRequest(1L, 1)));
    }

    @Test
    void shouldUpdateQuantity() {
        withItem(1);
        withExistingCart();

        CartResponse response = cartService.updateQuantity(1L, new CartItemQuantityRequest(3));

        assertEquals(3, response.totalItems());
    }

    @Test
    void shouldRejectQuantityAboveStock() {
        withItem(1);
        withExistingCart();

        assertThrows(BusinessException.class,
                () -> cartService.updateQuantity(1L, new CartItemQuantityRequest(6)));
    }

    @Test
    void shouldFailToUpdateProductThatIsNotInCart() {
        withExistingCart();

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateQuantity(1L, new CartItemQuantityRequest(2)));
    }

    @Test
    void shouldRemoveProductFromCart() {
        withItem(2);
        withExistingCart();

        CartResponse response = cartService.removeItem(1L);

        assertTrue(response.items().isEmpty());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldFailToRemoveProductThatIsNotInCart() {
        withExistingCart();

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(1L));
    }

    @Test
    void shouldClearCart() {
        withItem(2);
        withExistingCart();

        cartService.clear();

        assertTrue(cart.getItems().isEmpty());
    }
}
