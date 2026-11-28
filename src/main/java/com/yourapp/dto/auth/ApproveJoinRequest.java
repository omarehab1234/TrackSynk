package com.yourapp.dto.auth;
import java.util.UUID;

import com.yourapp.model.enums.Role;

import jakarta.validation.constraints.NotNull;
public record ApproveJoinRequest(
        @NotNull Role role,
        @NotNull UUID departmentId
) {}
