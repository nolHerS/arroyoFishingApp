package com.example.fishingapp.service;

import com.example.fishingapp.DTO.UserCreateDto;
import com.example.fishingapp.DTO.UserDto;

public interface UserService {

    UserCreateDto createUser (UserCreateDto user);

    UserDto findById (String userName);

    UserCreateDto updateUserCreateDto ( UserCreateDto userCreateDto);

    void deleteUser (String username);


}
