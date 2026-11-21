package com.yourapp.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.dto.company.CreateCompanyRequest;
import com.yourapp.dto.company.JoinCompanyRequest;
import com.yourapp.model.Tenant;
import com.yourapp.model.entity.User;
import com.yourapp.model.enums.Role;
import com.yourapp.model.enums.TenantStatus;
import com.yourapp.model.enums.UserStatus;
import com.yourapp.repository.TenantRepository;
import com.yourapp.repository.UserRepository;
import com.yourapp.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;

    public TenantService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            SecurityContextRepository securityContextRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional(readOnly = true)
    public List<Tenant> getAvailableCompanies() {
        List<Tenant> tenants = tenantRepository.findByStatusOrderByCompanyNameAsc(TenantStatus.ACTIVE);
        return tenants != null ? tenants : List.of();
    }

    @Transactional
    public Tenant createCompany(
            CreateCompanyRequest request,
            CustomUserDetails userDetails,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (userDetails == null || userDetails.getUserId() == null) {
            throw new IllegalStateException("Authenticated user details are missing");
        }

        UUID userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTenantId() != null) {
            throw new IllegalStateException("You already belong to a company");
        }

        String baseSlug = generateSlug(request.getCompanyName());
        String slug = baseSlug;
        int counter = 1;

        while (tenantRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        Tenant tenant = new Tenant(request.getCompanyName());
        tenant.setSlug(slug);
        tenant.setStatus(TenantStatus.ACTIVE);

        tenantRepository.save(tenant);

        user.setTenantId(tenant.getId());
        user.setRole(Role.COMPANY_ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        refreshSecurityContext(savedUser, httpRequest, httpResponse);

        return tenant;
    }

    @Transactional
    public Tenant joinCompany(
            JoinCompanyRequest request,
            CustomUserDetails userDetails,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (userDetails == null || userDetails.getUserId() == null) {
            throw new IllegalStateException("Authenticated user details are missing");
        }

        UUID userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTenantId() != null) {
            throw new IllegalStateException("You already belong to a company");
        }

        String normalizedSlug = request.getSlug().trim().toLowerCase(Locale.ROOT);

        Tenant tenant = tenantRepository.findBySlug(normalizedSlug)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new IllegalStateException("Company is not active");
        }

        user.setTenantId(tenant.getId());
        user.setRole(null); // Pending role assignment for MVP
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        refreshSecurityContext(savedUser, httpRequest, httpResponse);

        return tenant;
    }

    private void refreshSecurityContext(
            User savedUser,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        CustomUserDetails updatedDetails = new CustomUserDetails(savedUser);
        UsernamePasswordAuthenticationToken updatedAuth =
                new UsernamePasswordAuthenticationToken(
                        updatedDetails,
                        null,
                        updatedDetails.getAuthorities()
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(updatedAuth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }

    private String generateSlug(String companyName) {

        String slug = Normalizer.normalize(
                companyName,
                Normalizer.Form.NFD
        );

        slug = slug.replaceAll("\\p{M}", "");

        slug = slug.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Company name must contain valid characters");
        }

        return slug;
    }
}