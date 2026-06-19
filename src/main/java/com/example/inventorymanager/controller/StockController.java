package com.example.inventorymanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.inventorymanager.entity.MovementType;
import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.service.ProductService;
import com.example.inventorymanager.service.StockMovementService;

@Controller
public class StockController {

    private final ProductService productService;
    private final StockMovementService stockMovementService;

    public StockController(
            ProductService productService,
            StockMovementService stockMovementService) {
        this.productService = productService;
        this.stockMovementService = stockMovementService;
    }

    @GetMapping("/stocks")
    public String index(
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("summaries", productService.findStockSummaries(keyword));
        model.addAttribute("keyword", keyword);

        return "stocks/index";
    }

    @GetMapping("/products/{productId}/movements")
    public String movements(@PathVariable Long productId, Model model) {
        Product product = productService.findById(productId);

        model.addAttribute("product", product);
        model.addAttribute("movements", stockMovementService.findByProductId(productId));
        model.addAttribute("movementTypes", MovementType.values());

        return "stocks/movements";
    }

    @PostMapping("/products/{productId}/movements")
    public String createMovement(
            @PathVariable Long productId,
            @RequestParam MovementType movementType,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String note,
            Model model) {

        try {
            stockMovementService.register(productId, movementType, quantity, note);
        } catch (IllegalArgumentException e) {
            Product product = productService.findById(productId);
            model.addAttribute("product", product);
            model.addAttribute("movements", stockMovementService.findByProductId(productId));
            model.addAttribute("movementTypes", MovementType.values());
            model.addAttribute("error", e.getMessage());
            return "stocks/movements";
        }

        return "redirect:/products/" + productId + "/movements?registered";
    }
}
