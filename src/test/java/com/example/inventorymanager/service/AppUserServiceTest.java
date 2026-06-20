package com.example.inventorymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.inventorymanager.dto.UserForm;
import com.example.inventorymanager.entity.AppUser;
import com.example.inventorymanager.mapper.AppUserMapper;

class AppUserServiceTest {

    private final AppUserMapper appUserMapper = mock(AppUserMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AppUserService appUserService = new AppUserService(appUserMapper, passwordEncoder);

    @Test
    void registerInsertsUserWithEncodedPassword() {
        UserForm form = new UserForm();
        form.setUsername(" new-user ");
        form.setPassword("secret1");
        form.setRole("admin");
        form.setEnabled(true);

        when(appUserMapper.findByUsername("new-user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-password");

        appUserService.register(form);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserMapper).insert(captor.capture());

        AppUser appUser = captor.getValue();
        assertThat(appUser.getUsername()).isEqualTo("new-user");
        assertThat(appUser.getPassword()).isEqualTo("encoded-password");
        assertThat(appUser.getRole()).isEqualTo("ADMIN");
        assertThat(appUser.isEnabled()).isTrue();
    }

    @Test
    void registerThrowsExceptionWhenUsernameIsDuplicated() {
        UserForm form = new UserForm();
        form.setUsername("admin");
        form.setPassword("secret1");
        form.setRole("ADMIN");

        when(appUserMapper.findByUsername("admin")).thenReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> appUserService.register(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("このユーザー名はすでに登録されています。");

        verify(appUserMapper, never()).insert(any());
    }

    @Test
    void registerThrowsExceptionWhenPasswordIsTooShort() {
        UserForm form = new UserForm();
        form.setUsername("user");
        form.setPassword("12345");
        form.setRole("USER");

        assertThatThrownBy(() -> appUserService.register(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("パスワードは6文字以上で入力してください。");

        verify(appUserMapper, never()).insert(any());
    }

    @Test
    void registerThrowsExceptionWhenRoleIsInvalid() {
        UserForm form = new UserForm();
        form.setUsername("user");
        form.setPassword("secret1");
        form.setRole("MANAGER");

        assertThatThrownBy(() -> appUserService.register(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("権限はADMINまたはUSERを選択してください。");

        verify(appUserMapper, never()).insert(any());
    }
}
