package com.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.category.Category;
import com.ecommerce.category.CategoryRepository;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private ProductService productService;

    private final Category category = Category.builder().id(1L).name("Informática").build();

    private Product product(boolean active, int stock) {
        return Product.builder()
                .id(1L)
                .name("Notebook")
                .price(new BigDecimal("3500.00"))
                .stock(stock)
                .active(active)
                .category(category)
                .build();
    }

    @Test
    void shouldCreateProductWithInitialStock() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        ProductResponse response = productService.create(
                new ProductRequest("  Notebook  ", "Ultrafino", new BigDecimal("3500.00"), 10, 1L));

        assertEquals(7L, response.id());
        assertEquals("Notebook", response.name());
        assertEquals(10, response.stock());
        assertEquals("Informática", response.categoryName());
    }

    @Test
    void shouldFailToCreateProductWithUnknownCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.create(new ProductRequest("Notebook", null, BigDecimal.TEN, 1, 99L)));

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldNotExposeInactiveProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(false, 5)));

        assertThrows(ResourceNotFoundException.class, () -> productService.get(1L));
    }

    @Test
    void shouldDeactivateProduct() {
        Product product = product(true, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivate(1L);

        assertEquals(false, product.isActive());
    }

    @Test
    void shouldAdjustStock() {
        when(productRepository.adjustStock(1L, 5)).thenReturn(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(true, 15)));

        ProductResponse response = productService.adjustStock(1L, new StockAdjustmentRequest(5));

        assertEquals(15, response.stock());
    }

    @Test
    void shouldRejectAdjustmentThatMakesStockNegative() {
        when(productRepository.adjustStock(1L, -50)).thenReturn(0);
        when(productRepository.existsById(1L)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> productService.adjustStock(1L, new StockAdjustmentRequest(-50)));
    }

    @Test
    void shouldFailToAdjustStockOfUnknownProduct() {
        when(productRepository.adjustStock(1L, 5)).thenReturn(0);
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.adjustStock(1L, new StockAdjustmentRequest(5)));
    }
}
