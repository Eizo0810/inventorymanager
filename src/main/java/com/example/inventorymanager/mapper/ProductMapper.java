package com.example.inventorymanager.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.inventorymanager.dto.StockSummary;
import com.example.inventorymanager.entity.Product;

@Mapper
public interface ProductMapper {

    List<Product> findAll(@Param("keyword") String keyword);

    Optional<Product> findById(Long id);

    Optional<Product> findByCode(String code);

    void insert(Product product);

    void update(Product product);

    List<StockSummary> findStockSummaries(@Param("keyword") String keyword);
}
