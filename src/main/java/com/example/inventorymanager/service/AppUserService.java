package com.example.inventorymanager.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventorymanager.dto.UserForm;
import com.example.inventorymanager.entity.AppUser;
import com.example.inventorymanager.mapper.AppUserMapper;

@Service
public class AppUserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "USER");

    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserMapper appUserMapper,
            PasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> findAll() {
        return appUserMapper.findAll();
    }

    @Transactional
    public void register(UserForm form) {
        String username = normalizeRequired(form.getUsername(), "ユーザー名");
        String password = normalizeRequired(form.getPassword(), "パスワード");
        String role = normalizeRole(form.getRole());

        if (password.length() < 6) {
            throw new IllegalArgumentException("パスワードは6文字以上で入力してください。");
        }

        if (appUserMapper.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("このユーザー名はすでに登録されています。");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(password));
        appUser.setRole(role);
        appUser.setEnabled(form.isEnabled());

        appUserMapper.insert(appUser);
    }

    private String normalizeRole(String role) {
        String normalizedRole = normalizeRequired(role, "権限").toUpperCase();
        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("権限はADMINまたはUSERを選択してください。");
        }

        return normalizedRole;
    }

    private String normalizeRequired(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "を入力してください。");
        }

        return value.trim();
    }
}
