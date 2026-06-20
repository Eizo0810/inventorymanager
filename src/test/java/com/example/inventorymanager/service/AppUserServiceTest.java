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

    @Test
    void updateUpdatesUserWithoutChangingPasswordWhenPasswordIsBlank() {
        AppUser current = appUser(2L, "operator", "old-password", "USER", true);

        UserForm form = new UserForm();
        form.setUsername(" operator2 ");
        form.setPassword(" ");
        form.setRole("user");
        form.setEnabled(false);

        when(appUserMapper.findById(2L)).thenReturn(Optional.of(current));
        when(appUserMapper.findByUsername("operator2")).thenReturn(Optional.empty());

        appUserService.update(2L, form);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserMapper).update(captor.capture());

        AppUser updated = captor.getValue();
        assertThat(updated.getUsername()).isEqualTo("operator2");
        assertThat(updated.getPassword()).isEqualTo("old-password");
        assertThat(updated.getRole()).isEqualTo("USER");
        assertThat(updated.isEnabled()).isFalse();
    }

    @Test
    void updateChangesPasswordWhenPasswordIsEntered() {
        AppUser current = appUser(2L, "operator", "old-password", "USER", true);

        UserForm form = new UserForm();
        form.setUsername("operator");
        form.setPassword("newpass");
        form.setRole("USER");
        form.setEnabled(true);

        when(appUserMapper.findById(2L)).thenReturn(Optional.of(current));
        when(appUserMapper.findByUsername("operator")).thenReturn(Optional.of(current));
        when(passwordEncoder.encode("newpass")).thenReturn("new-encoded-password");

        appUserService.update(2L, form);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserMapper).update(captor.capture());

        assertThat(captor.getValue().getPassword()).isEqualTo("new-encoded-password");
    }

    @Test
    void updateThrowsExceptionWhenUsernameIsUsedByOtherUser() {
        AppUser current = appUser(2L, "operator", "password", "USER", true);
        AppUser other = appUser(3L, "other", "password", "USER", true);

        UserForm form = new UserForm();
        form.setUsername("other");
        form.setPassword("");
        form.setRole("USER");
        form.setEnabled(true);

        when(appUserMapper.findById(2L)).thenReturn(Optional.of(current));
        when(appUserMapper.findByUsername("other")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> appUserService.update(2L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("このユーザー名はすでに登録されています。");

        verify(appUserMapper, never()).update(any());
    }

    @Test
    void updateThrowsExceptionWhenLastEnabledAdminWouldBeDisabled() {
        AppUser current = appUser(1L, "admin", "password", "ADMIN", true);

        UserForm form = new UserForm();
        form.setUsername("admin");
        form.setPassword("");
        form.setRole("ADMIN");
        form.setEnabled(false);

        when(appUserMapper.findById(1L)).thenReturn(Optional.of(current));
        when(appUserMapper.findByUsername("admin")).thenReturn(Optional.of(current));
        when(appUserMapper.countEnabledAdmins()).thenReturn(1);

        assertThatThrownBy(() -> appUserService.update(1L, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("最後の有効なADMINユーザーは無効化またはUSERへ変更できません。");

        verify(appUserMapper, never()).update(any());
    }

    private AppUser appUser(Long id, String username, String password, String role, boolean enabled) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setPassword(password);
        appUser.setRole(role);
        appUser.setEnabled(enabled);
        return appUser;
    }
}
