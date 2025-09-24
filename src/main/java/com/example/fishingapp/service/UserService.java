package com.example.fishingapp.service;

import com.example.fishingapp.dto.UserCreateDto;
import com.example.fishingapp.dto.UserDto;

import java.util.List;

public interface UserService {

    UserCreateDto createUser (UserCreateDto user);

    UserDto findByUsername (String userId);

    List<UserCreateDto> getAllUsers ();

    UserCreateDto updateUserCreateDto ( UserCreateDto userCreateDto);

    void deleteUser (String username);


}
