package com.yourapp.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.dto.company.CreateCompanyRequest;
import com.yourapp.model.Tenant;
import com.yourapp.model.entity.User;
import com.yourapp.model.enums.Role;
import com.yourapp.model.enums.TenantStatus;
import com.yourapp.model.enums.UserStatus;
import com.yourapp.repository.TenantRepository;
import com.yourapp.repository.UserRepository;
import com.yourapp.security.CustomUserDetails;
// createcompany service
// slug generation service generateSlug
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public TenantService(
            TenantRepository tenantRepository,
            UserRepository userRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Tenant createCompany(
            CreateCompanyRequest request,
            CustomUserDetails userDetails) {

        // 1. Get the authenticated user
        UUID userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        // 2. Make sure the user doesn't already belong to a company
        if (user.getTenantId() != null) {
            throw new IllegalStateException(
                    "You already belong to a company");
        }

        // 3. Generate a unique slug
        String baseSlug = generateSlug(request.getCompanyName());
        String slug = baseSlug;
        int counter = 1;

        while (tenantRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        // 4. Create the company
        Tenant tenant = new Tenant(request.getCompanyName());
        tenant.setSlug(slug);
        tenant.setStatus(TenantStatus.ACTIVE);

        tenantRepository.save(tenant);

        // 5. Make the creator the company admin
        user.setTenantId(tenant.getId());
        user.setRole(Role.COMPANY_ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return tenant;
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