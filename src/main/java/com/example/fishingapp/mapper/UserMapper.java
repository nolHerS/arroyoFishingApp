package com.example.fishingapp.mapper;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.model.User;

public class UserMapper {

    /**
     * UserDto -> User (SIN AuthUser, para consultas)
     */
    public static User mapUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.id());
        user.setUsername(userDto.username());
        user.setFullName(userDto.fullName());
        user.setEmail(userDto.email());
        return user;
    }

    /**
     * User -> UserDto (para respuestas)
     */
    public static UserDto mapUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail()
        );
    }
}