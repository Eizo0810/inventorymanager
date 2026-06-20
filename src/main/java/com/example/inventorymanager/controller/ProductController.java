package com.example.inventorymanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventorymanager.entity.Product;
import com.example.inventorymanager.service.ProductService;

@Controller
public class ProductController {

    private static final int PRODUCT_PAGE_SIZE = 3;

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
            @RequestParam(required = false) Integer page,
            Model model) {

        var productPage = productService.findPage(keyword, page, PRODUCT_PAGE_SIZE);

        model.addAttribute("products", productPage.getItems());
        model.addAttribute("productPage", productPage);
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

    @GetMapping("/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));

        return "products/edit";
    }

    @PostMapping("/products/{id}")
    public String update(
            @PathVariable Long id,
            Product product,
            Model model) {

        try {
            productService.update(id, product);
        } catch (IllegalArgumentException e) {
            product.setId(id);
            model.addAttribute("product", product);
            model.addAttribute("error", e.getMessage());
            return "products/edit";
        }

        return "redirect:/products?updated";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }

        return "redirect:/products?deleted";
    }
}
