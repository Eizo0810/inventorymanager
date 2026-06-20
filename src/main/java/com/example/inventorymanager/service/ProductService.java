package com.example.inventorymanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventorymanager.dto.StockSummary;
import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.mapper.ProductMapper;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> findAll(String keyword) {
        return productMapper.findAll(normalizeKeyword(keyword));
    }

    public Product findById(Long id) {
        return productMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません。"));
    }

    public List<StockSummary> findStockSummaries(String keyword) {
        return productMapper.findStockSummaries(normalizeKeyword(keyword));
    }

    @Transactional
    public void register(Product product) {
        product.setCode(normalizeRequired(product.getCode(), "商品コード"));
        product.setName(normalizeRequired(product.getName(), "商品名"));
        product.setCategory(normalizeOptional(product.getCategory()));

        if (product.getSafetyStock() == null || product.getSafetyStock() < 0) {
            throw new IllegalArgumentException("安全在庫数は0以上で入力してください。");
        }

        if (productMapper.findByCode(product.getCode()).isPresent()) {
            throw new IllegalArgumentException("この商品コードはすでに登録されています。");
        }

        productMapper.insert(product);
    }

    @Transactional
    public void update(Long id, Product product) {
        findById(id);

        product.setId(id);
        product.setCode(normalizeRequired(product.getCode(), "商品コード"));
        product.setName(normalizeRequired(product.getName(), "商品名"));
        product.setCategory(normalizeOptional(product.getCategory()));

        if (product.getSafetyStock() == null || product.getSafetyStock() < 0) {
            throw new IllegalArgumentException("安全在庫数は0以上で入力してください。");
        }

        productMapper.findByCode(product.getCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("この商品コードはすでに登録されています。");
                });

        productMapper.update(product);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    private String normalizeRequired(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "を入力してください。");
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim();
    }
}
