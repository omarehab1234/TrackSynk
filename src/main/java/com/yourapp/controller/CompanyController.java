package com.yourapp.controller;
// rest controller for company creation
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yourapp.dto.company.CreateCompanyRequest;
import com.yourapp.model.Tenant;
import com.yourapp.security.CustomUserDetails;
import com.yourapp.service.TenantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final TenantService tenantService;

    public CompanyController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Tenant tenant = tenantService.createCompany(
                request,
                userDetails
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenant);
    }
}