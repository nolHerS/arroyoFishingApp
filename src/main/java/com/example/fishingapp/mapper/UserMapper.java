package com.example.fishingapp.mapper;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.model.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDto mapUserDto(User user){
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail()
        );
    }

    public static User mapUser(UserDto userDto){
        return new User(
                userDto.id(),
                userDto.username(),
                userDto.fullName(),
                userDto.email()
                );
    }

}
