package com.example.inventorymanager.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.inventorymanager.entity.MovementType;
import com.example.inventorymanager.entity.StockMovement;

@Mapper
public interface StockMovementMapper {

    void insert(StockMovement stockMovement);

    List<StockMovement> findByProductId(Long productId);

    List<StockMovement> searchByProductId(
            @Param("productId") Long productId,
            @Param("movementType") MovementType movementType,
            @Param("keyword") String keyword);

    int countByProductId(Long productId);

    int calculateStockQuantity(Long productId);
}
