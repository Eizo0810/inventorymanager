package com.example.inventorymanager.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.mapper.ProductMapper;

class ProductServiceTest {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductService productService = new ProductService(productMapper);

    @Test
    void registerInsertsProductWhenInputIsValid() {
        Product product = new Product();
        product.setCode(" PRD-100 ");
        product.setName(" テスト商品 ");
        product.setCategory(" テスト ");
        product.setSafetyStock(10);

        when(productMapper.findByCode("PRD-100")).thenReturn(Optional.empty());

        productService.register(product);

        verify(productMapper).insert(product);
    }

    @Test
    void registerThrowsExceptionWhenCodeIsDuplicated() {
        Product product = new Product();
        product.setCode("PRD-100");
        product.setName("テスト商品");
        product.setSafetyStock(10);

        when(productMapper.findByCode("PRD-100"))
                .thenReturn(Optional.of(new Product()));

        assertThatThrownBy(() -> productService.register(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("この商品コードはすでに登録されています。");
    }

    @Test
    void registerThrowsExceptionWhenSafetyStockIsNegative() {
        Product product = new Product();
        product.setCode("PRD-100");
        product.setName("テスト商品");
        product.setSafetyStock(-1);

        assertThatThrownBy(() -> productService.register(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("安全在庫数は0以上で入力してください。");
    }

    @Test
    void updateUpdatesProductWhenInputIsValid() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setCode("PRD-100");

        Product product = new Product();
        product.setCode(" PRD-100 ");
        product.setName(" 更新商品 ");
        product.setCategory(" 更新カテゴリ ");
        product.setSafetyStock(20);

        when(productMapper.findById(1L)).thenReturn(Optional.of(existing));
        when(productMapper.findByCode("PRD-100")).thenReturn(Optional.of(existing));

        productService.update(1L, product);

        verify(productMapper).update(product);
    }

    @Test
    void updateThrowsExceptionWhenCodeIsUsedByOtherProduct() {
        Product current = new Product();
        current.setId(1L);

        Product other = new Product();
        other.setId(2L);

        Product product = new Product();
        product.setCode("PRD-200");
        product.setName("更新商品");
        product.setSafetyStock(20);

        when(productMapper.findById(1L)).thenReturn(Optional.of(current));
        when(productMapper.findByCode("PRD-200")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> productService.update(1L, product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("この商品コードはすでに登録されています。");
    }
}
