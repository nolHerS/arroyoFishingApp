package com.example.fishingapp.service;

import com.example.fishingapp.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto findByUsername (String userUsername);

    UserDto findById (Long id);

    List<UserDto> getAllUsers ();

    UserDto updateUserDto ( UserDto userDto);

    void deleteUser (String username);


}
