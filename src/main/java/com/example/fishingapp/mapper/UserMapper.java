package com.example.fishingapp.mapper;

import com.example.fishingapp.DTO.UserDto;
import com.example.fishingapp.model.User;

public class UserMapper {

    public static UserDto mapUserDto(User user){
        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getCaptures()
                        .stream()
                        .map(FishCaptureMapper::mapFishCaptureDto)
                        .toList()
        );
        return userDto;
    }

    public static UserSummaryDto mapUserSummaryDto(User user){
        UserSummaryDto userSummaryDto = new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail()
        );
        return userSummaryDto;
    }

    public static User mapUser(UserDto userDto){
        return new User(
                userDto.id(),
                userDto.username(),
                userDto.fullName(),
                userDto.email()
        );
    }

    public static User mapToUserFromUserSummaryDto(UserSummaryDto userSummaryDto){

        return new User(
                userSummaryDto.id(),
                userSummaryDto.username(),
                userSummaryDto.fullName(),
                userSummaryDto.email()
        );
    }
}
