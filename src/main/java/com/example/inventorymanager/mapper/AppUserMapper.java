package com.example.inventorymanager.mapper;

import java.util.Optional;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.inventorymanager.entity.AppUser;

@Mapper
public interface AppUserMapper {

    List<AppUser> findAll();

    Optional<AppUser> findByUsername(String username);

    void insert(AppUser appUser);
}
