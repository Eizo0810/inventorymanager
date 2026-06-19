package com.example.inventorymanager.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.inventorymanager.entity.StockMovement;

@Mapper
public interface StockMovementMapper {

    void insert(StockMovement stockMovement);

    List<StockMovement> findByProductId(Long productId);

    int calculateStockQuantity(Long productId);
}
