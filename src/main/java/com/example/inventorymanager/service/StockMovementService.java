package com.example.inventorymanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventorymanager.entity.MovementType;
import com.example.inventorymanager.entity.StockMovement;
import com.example.inventorymanager.mapper.StockMovementMapper;

@Service
public class StockMovementService {

    private final ProductService productService;
    private final StockMovementMapper stockMovementMapper;

    public StockMovementService(
            ProductService productService,
            StockMovementMapper stockMovementMapper) {
        this.productService = productService;
        this.stockMovementMapper = stockMovementMapper;
    }

    public List<StockMovement> findByProductId(Long productId) {
        return stockMovementMapper.findByProductId(productId);
    }

    public List<StockMovement> searchByProductId(
            Long productId,
            MovementType movementType,
            String keyword) {
        productService.findById(productId);
        return stockMovementMapper.searchByProductId(
                productId,
                movementType,
                normalizeKeyword(keyword));
    }

    @Transactional
    public void register(Long productId, MovementType movementType, Integer quantity, String note) {
        productService.findById(productId);
        validateQuantity(quantity);

        if (movementType == MovementType.OUT) {
            int currentStock = stockMovementMapper.calculateStockQuantity(productId);
            if (quantity > currentStock) {
                throw new IllegalArgumentException("出庫数が現在庫数を超えています。");
            }
        }

        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setNote(note == null ? "" : note.trim());

        stockMovementMapper.insert(movement);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("数量は1以上で入力してください。");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }
}
