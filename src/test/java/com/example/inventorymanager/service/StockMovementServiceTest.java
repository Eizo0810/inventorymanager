package com.example.inventorymanager.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.inventorymanager.entity.MovementType;
import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.mapper.StockMovementMapper;

class StockMovementServiceTest {

    private final ProductService productService = mock(ProductService.class);
    private final StockMovementMapper stockMovementMapper =
            mock(StockMovementMapper.class);
    private final StockMovementService stockMovementService =
            new StockMovementService(productService, stockMovementMapper);

    @Test
    void registerInsertsInboundMovement() {
        when(productService.findById(1L)).thenReturn(new Product());

        stockMovementService.register(1L, MovementType.IN, 10, "入庫");

        verify(stockMovementMapper).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registerThrowsExceptionWhenQuantityIsZero() {
        when(productService.findById(1L)).thenReturn(new Product());

        assertThatThrownBy(() -> stockMovementService.register(
                1L, MovementType.IN, 0, "入庫"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("数量は1以上で入力してください。");
    }

    @Test
    void registerThrowsExceptionWhenOutboundQuantityExceedsStock() {
        when(productService.findById(1L)).thenReturn(new Product());
        when(stockMovementMapper.calculateStockQuantity(1L)).thenReturn(5);

        assertThatThrownBy(() -> stockMovementService.register(
                1L, MovementType.OUT, 6, "出庫"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("出庫数が現在庫数を超えています。");
    }

    @Test
    void searchByProductIdPassesNormalizedKeywordToMapper() {
        when(productService.findById(1L)).thenReturn(new Product());

        stockMovementService.searchByProductId(1L, MovementType.IN, " 仕入 ");

        verify(stockMovementMapper).searchByProductId(1L, MovementType.IN, "仕入");
    }

    @Test
    void searchByProductIdConvertsBlankKeywordToNull() {
        when(productService.findById(1L)).thenReturn(new Product());

        stockMovementService.searchByProductId(1L, null, " ");

        verify(stockMovementMapper).searchByProductId(eq(1L), isNull(), isNull());
    }
}
