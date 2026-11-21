package com.yourapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yourapp.dto.company.CreateCompanyRequest;
import com.yourapp.dto.company.JoinCompanyRequest;
import com.yourapp.model.Tenant;
import com.yourapp.security.CustomUserDetails;
import com.yourapp.service.TenantService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final TenantService tenantService;

    public CompanyController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> listCompanies() {
        return ResponseEntity.ok(tenantService.getAvailableCompanies());
    }

    @PostMapping
    public ResponseEntity<Tenant> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Tenant tenant = tenantService.createCompany(
                request,
                userDetails,
                httpRequest,
                httpResponse
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenant);
    }

    @PostMapping("/join")
    public ResponseEntity<Tenant> joinCompany(
            @Valid @RequestBody JoinCompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Tenant tenant = tenantService.joinCompany(
                request,
                userDetails,
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok(tenant);
    }
}