package com.example.fishingapp.mapper;

import com.example.fishingapp.dto.UserCreateDto;
import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.model.User;

public class UserMapper {

    public static UserDto mapUserDto(User user){
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getCaptures()
                        .stream()
                        .map(FishCaptureMapper::mapFishCaptureDto)
                        .toList()
        );
    }

    public static UserCreateDto mapUserCreateDto(User user){
        return new UserCreateDto(
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
                userDto.email(),
                userDto.fishCaptures()
                        .stream()
                        .map(FishCaptureMapper::mapFishCapture)
                        .toList()
        );
    }

    public static User mapUserCreateDto(UserCreateDto userCreateDto){
        return new User(
                userCreateDto.id(),
                userCreateDto.username(),
                userCreateDto.fullName(),
                userCreateDto.email()
        );
    }

}
