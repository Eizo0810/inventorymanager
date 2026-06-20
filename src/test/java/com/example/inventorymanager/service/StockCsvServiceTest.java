package com.example.inventorymanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.inventorymanager.dto.StockSummary;

class StockCsvServiceTest {

    private final StockCsvService stockCsvService = new StockCsvService();

    @Test
    void exportCreatesUtf8CsvWithStockStatus() {
        StockSummary normal = new StockSummary();
        normal.setCode("PRD-001");
        normal.setName("コピー用紙 A4");
        normal.setCategory("事務用品");
        normal.setStockQuantity(120);
        normal.setSafetyStock(30);

        StockSummary alert = new StockSummary();
        alert.setCode("PRD-002");
        alert.setName("テープ, 透明");
        alert.setCategory("消耗品");
        alert.setStockQuantity(5);
        alert.setSafetyStock(10);

        byte[] csvBytes = stockCsvService.export(List.of(normal, alert));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).startsWith("\uFEFF商品コード,商品名,カテゴリ,現在庫数,安全在庫数,状態");
        assertThat(csv).contains("PRD-001,コピー用紙 A4,事務用品,120,30,正常");
        assertThat(csv).contains("PRD-002,\"テープ, 透明\",消耗品,5,10,要補充");
    }
}
