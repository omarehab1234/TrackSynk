package com.yourapp.dto.auth;

import java.util.UUID;

public record ApproveJoinResponse(
        UUID userId,
        String message
) {}