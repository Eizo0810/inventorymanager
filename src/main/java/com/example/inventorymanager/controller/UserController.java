package com.example.inventorymanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.inventorymanager.dto.UserForm;
import com.example.inventorymanager.service.AppUserService;

@Controller
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/users")
    public String index(Model model) {
        model.addAttribute("users", appUserService.findAll());

        return "users/index";
    }

    @GetMapping("/users/new")
    public String newForm(Model model) {
        model.addAttribute("userForm", new UserForm());

        return "users/new";
    }

    @PostMapping("/users")
    public String create(UserForm userForm, Model model) {
        try {
            appUserService.register(userForm);
        } catch (IllegalArgumentException e) {
            model.addAttribute("userForm", userForm);
            model.addAttribute("error", e.getMessage());
            return "users/new";
        }

        return "redirect:/users?registered";
    }
}
