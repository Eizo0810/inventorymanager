package com.example.inventorymanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.inventorymanager.entity.AppUser;
import com.example.inventorymanager.mapper.AppUserMapper;

class DatabaseUserDetailsServiceTest {

    private static final String ADMIN_PASSWORD_HASH =
            "$2a$10$TlvgzQFP3Y6NGea48CmuSOth0lOaiU6lh2LADpuRGGQV6YG1Ka0Ea";

    private final AppUserMapper appUserMapper = mock(AppUserMapper.class);
    private final DatabaseUserDetailsService service = new DatabaseUserDetailsService(appUserMapper);

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        AppUser appUser = new AppUser();
        appUser.setUsername("admin");
        appUser.setPassword(ADMIN_PASSWORD_HASH);
        appUser.setRole("ADMIN");
        appUser.setEnabled(true);

        when(appUserMapper.findByUsername("admin")).thenReturn(Optional.of(appUser));

        UserDetails userDetails = service.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo(ADMIN_PASSWORD_HASH);
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameReturnsDisabledUserDetails() {
        AppUser appUser = new AppUser();
        appUser.setUsername("stopped");
        appUser.setPassword(ADMIN_PASSWORD_HASH);
        appUser.setRole("ADMIN");
        appUser.setEnabled(false);

        when(appUserMapper.findByUsername("stopped")).thenReturn(Optional.of(appUser));

        UserDetails userDetails = service.loadUserByUsername("stopped");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsernameThrowsExceptionWhenUserDoesNotExist() {
        when(appUserMapper.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("ユーザーが見つかりません。");
    }

    @Test
    void adminInitialPasswordHashMatchesPassword() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        assertThat(passwordEncoder.matches("password", ADMIN_PASSWORD_HASH)).isTrue();
    }
}
