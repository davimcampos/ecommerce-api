package com.ecommerce.product;

import com.ecommerce.category.Category;
import com.ecommerce.category.CategoryRepository;
import com.ecommerce.common.PageResponse;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, boolean inStockOnly,
            Pageable pageable) {
        return PageResponse.of(
                productRepository.findAll(
                        ProductSpecifications.search(name, categoryId, minPrice, maxPrice, inStockOnly), pageable),
                ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .active(true)
                .category(findCategory(request.categoryId()))
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(findCategory(request.categoryId()));
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        product.setActive(false);
    }

    @Transactional
    public ProductResponse adjustStock(Long id, StockAdjustmentRequest request) {
        int updated = productRepository.adjustStock(id, request.delta());
        if (updated == 0) {
            if (!productRepository.existsById(id)) {
                throw new ResourceNotFoundException("Produto não encontrado");
            }
            throw new BusinessException("O ajuste deixaria o estoque negativo");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        return ProductResponse.from(product);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}
