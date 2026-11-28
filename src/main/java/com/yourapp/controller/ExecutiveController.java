package com.yourapp.controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yourapp.security.CustomUserDetails;
import com.yourapp.service.ExecutiveService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/executive")
public class ExecutiveController {
    
    private final ExecutiveService executiveService;

    @GetMapping("/dashboard")
    public String getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
            model.addAttribute(
                    "dashboard",
                    executiveService.getDashboard(userDetails)
            );

            return "company/ExecutiveDashboard";
        }
}