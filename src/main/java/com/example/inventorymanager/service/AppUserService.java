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

    public AppUser findById(Long id) {
        return appUserMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));
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

    @Transactional
    public void update(Long id, UserForm form) {
        AppUser current = findById(id);
        String username = normalizeRequired(form.getUsername(), "ユーザー名");
        String role = normalizeRole(form.getRole());
        String password = normalizeOptional(form.getPassword());

        appUserMapper.findByUsername(username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("このユーザー名はすでに登録されています。");
                });

        if (isRemovingLastEnabledAdmin(current, role, form.isEnabled())) {
            throw new IllegalArgumentException("最後の有効なADMINユーザーは無効化またはUSERへ変更できません。");
        }

        current.setUsername(username);
        current.setRole(role);
        current.setEnabled(form.isEnabled());

        if (password != null) {
            if (password.length() < 6) {
                throw new IllegalArgumentException("パスワードは6文字以上で入力してください。");
            }
            current.setPassword(passwordEncoder.encode(password));
        }

        appUserMapper.update(current);
    }

    public UserForm toForm(AppUser appUser) {
        UserForm form = new UserForm();
        form.setUsername(appUser.getUsername());
        form.setRole(appUser.getRole());
        form.setEnabled(appUser.isEnabled());
        return form;
    }

    private boolean isRemovingLastEnabledAdmin(AppUser current, String newRole, boolean newEnabled) {
        if (!current.isEnabled() || !"ADMIN".equals(current.getRole())) {
            return false;
        }

        boolean remainsEnabledAdmin = newEnabled && "ADMIN".equals(newRole);
        return !remainsEnabledAdmin && appUserMapper.countEnabledAdmins() <= 1;
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

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
