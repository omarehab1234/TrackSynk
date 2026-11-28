package com.yourapp.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yourapp.dto.auth.ExecutiveDashboardResponse;
import com.yourapp.security.CustomUserDetails;
import com.yourapp.service.ExecutiveService;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/executive")
public class ExecutiveRestController {

    private final ExecutiveService executiveService;

    @GetMapping("/dashboard")
    public ResponseEntity<ExecutiveDashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                executiveService.getDashboard(userDetails)
        );
    }
}