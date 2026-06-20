package com.example.inventorymanager.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.inventorymanager.dto.StockSummary;

@Service
public class StockCsvService {

    public byte[] export(List<StockSummary> summaries) {
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("商品コード,商品名,カテゴリ,現在庫数,安全在庫数,状態\r\n");

        for (StockSummary summary : summaries) {
            appendRow(csv, summary);
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, StockSummary summary) {
        csv.append(escape(summary.getCode())).append(',')
                .append(escape(summary.getName())).append(',')
                .append(escape(summary.getCategory())).append(',')
                .append(summary.getStockQuantity()).append(',')
                .append(summary.getSafetyStock()).append(',')
                .append(summary.isAlert() ? "要補充" : "正常")
                .append("\r\n");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"")
                || value.contains("\r") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
