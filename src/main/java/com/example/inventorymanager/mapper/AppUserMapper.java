package com.example.inventorymanager.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.inventorymanager.entity.AppUser;

@Mapper
public interface AppUserMapper {

    List<AppUser> findAll();

    Optional<AppUser> findById(Long id);

    Optional<AppUser> findByUsername(String username);

    void insert(AppUser appUser);

    void update(AppUser appUser);

    int countEnabledAdmins();
}
