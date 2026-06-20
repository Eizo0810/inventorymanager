package com.example.inventorymanager.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.example.inventorymanager.entity.AppUser;

@Mapper
public interface AppUserMapper {

    Optional<AppUser> findByUsername(String username);
}
