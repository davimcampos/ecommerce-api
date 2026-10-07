package com.ecommerce.cart;

import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.product.Product;
import com.ecommerce.product.ProductRepository;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.user.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUser authenticatedUser;

    @Transactional(readOnly = true)
    public CartResponse get() {
        User user = authenticatedUser.get();
        return cartRepository.findByUserId(user.getId())
                .map(CartResponse::from)
                .orElseGet(CartResponse::empty);
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        Cart cart = findOrCreate();
        Optional<CartItem> existing = cart.findItem(product.getId());
        int quantity = request.quantity() + existing.map(CartItem::getQuantity).orElse(0);
        ensureStock(product, quantity);
        if (existing.isPresent()) {
            existing.get().setQuantity(quantity);
        } else {
            cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(quantity).build());
        }
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, CartItemQuantityRequest request) {
        Cart cart = findExisting();
        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não está no carrinho"));
        ensureStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeItem(Long productId) {
        Cart cart = findExisting();
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Produto não está no carrinho");
        }
        return CartResponse.from(cart);
    }

    @Transactional
    public void clear() {
        User user = authenticatedUser.get();
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> cart.getItems().clear());
    }

    private Cart findOrCreate() {
        User user = authenticatedUser.get();
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private Cart findExisting() {
        User user = authenticatedUser.get();
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não está no carrinho"));
    }

    private void ensureStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new BusinessException(
                    "Estoque insuficiente para o produto " + product.getName()
                            + " (disponível: " + product.getStock() + ")");
        }
    }
}
