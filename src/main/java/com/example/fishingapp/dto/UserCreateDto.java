package com.example.fishingapp.dto;

public record UserCreateDto(Long id,
                            String username,
                            String fullName,
                            String email) {
}
