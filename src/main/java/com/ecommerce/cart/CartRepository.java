package com.ecommerce.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findForCheckoutByUserId(Long userId);
}
