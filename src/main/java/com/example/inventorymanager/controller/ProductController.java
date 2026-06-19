package com.example.inventorymanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.service.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String index(
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("products", productService.findAll(keyword));
        model.addAttribute("keyword", keyword);

        return "products/index";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());

        return "products/new";
    }

    @PostMapping("/products")
    public String create(Product product, Model model) {
        try {
            productService.register(product);
        } catch (IllegalArgumentException e) {
            model.addAttribute("product", product);
            model.addAttribute("error", e.getMessage());
            return "products/new";
        }

        return "redirect:/products?registered";
    }
}
