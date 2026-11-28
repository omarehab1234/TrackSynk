package com.yourapp.dto.auth;


import java.util.UUID;

public record DenyJoinResponse(
        UUID userId,
        String message
) {}
