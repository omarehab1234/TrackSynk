package com.yourapp.dto.auth;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String status,
        String role
) {}