package com.example.fishingapp.dto;

import java.time.Instant;

public record FileMetaData(
        long contentLength,
        String contentType,
        Instant lastModified
) {}
