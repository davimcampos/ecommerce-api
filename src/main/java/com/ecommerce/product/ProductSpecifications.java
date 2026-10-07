package com.ecommerce.product;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> search(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, boolean inStockOnly) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isTrue(root.get("active")));
            if (name != null && !name.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(builder.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (inStockOnly) {
                predicates.add(builder.greaterThan(root.get("stock"), 0));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
