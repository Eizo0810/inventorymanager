package com.example.inventorymanager.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.inventorymanager.entity.MovementType;
import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.service.ProductService;
import com.example.inventorymanager.service.StockCsvService;
import com.example.inventorymanager.service.StockMovementService;

@Controller
public class StockController {

    private final ProductService productService;
    private final StockMovementService stockMovementService;
    private final StockCsvService stockCsvService;

    public StockController(
            ProductService productService,
            StockMovementService stockMovementService,
            StockCsvService stockCsvService) {
        this.productService = productService;
        this.stockMovementService = stockMovementService;
        this.stockCsvService = stockCsvService;
    }

    @GetMapping("/stocks")
    public String index(
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("summaries", productService.findStockSummaries(keyword));
        model.addAttribute("keyword", keyword);

        return "stocks/index";
    }

    @GetMapping("/stocks/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String keyword) {
        byte[] csv = stockCsvService.export(productService.findStockSummaries(keyword));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("stock-list.csv")
                                .build()
                                .toString())
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @GetMapping("/products/{productId}/movements")
    public String movements(
            @PathVariable Long productId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false) String keyword,
            Model model) {
        Product product = productService.findById(productId);

        model.addAttribute("product", product);
        model.addAttribute("movements",
                stockMovementService.searchByProductId(productId, movementType, keyword));
        model.addAttribute("movementTypes", MovementType.values());
        model.addAttribute("selectedMovementType", movementType);
        model.addAttribute("keyword", keyword);

        return "stocks/movements";
    }

    @PostMapping("/products/{productId}/movements")
    public String createMovement(
            @PathVariable Long productId,
            @RequestParam MovementType movementType,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) MovementType searchMovementType,
            @RequestParam(required = false) String keyword,
            Model model) {

        try {
            stockMovementService.register(productId, movementType, quantity, note);
        } catch (IllegalArgumentException e) {
            Product product = productService.findById(productId);
            model.addAttribute("product", product);
            model.addAttribute("movements",
                    stockMovementService.searchByProductId(productId, searchMovementType, keyword));
            model.addAttribute("movementTypes", MovementType.values());
            model.addAttribute("selectedMovementType", searchMovementType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("error", e.getMessage());
            return "stocks/movements";
        }

        return "redirect:/products/" + productId + "/movements?registered";
    }
}
