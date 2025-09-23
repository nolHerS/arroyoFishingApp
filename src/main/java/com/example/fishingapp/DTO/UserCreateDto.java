package com.example.fishingapp.DTO;

public record UserCreateDto(Long id,
                            String username,
                            String fullName,
                            String email) {
}
