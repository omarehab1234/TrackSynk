package com.yourapp.dto.auth;
import java.util.UUID;


public record PendingJoinRequestResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String status
) {}