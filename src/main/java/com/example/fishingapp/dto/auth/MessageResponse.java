package com.example.fishingapp.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;

    @Builder.Default
    private Boolean success = true;

    public MessageResponse(String message) {
        this.message = message;
        this.success = true;
    }
}
