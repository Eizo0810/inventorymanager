package com.example.inventorymanager.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.inventorymanager.entity.AppUser;
import com.example.inventorymanager.mapper.AppUserMapper;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserMapper appUserMapper;

    public DatabaseUserDetailsService(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser appUser = appUserMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません。"));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPassword())
                .authorities("ROLE_" + appUser.getRole())
                .disabled(!appUser.isEnabled())
                .build();
    }
}
