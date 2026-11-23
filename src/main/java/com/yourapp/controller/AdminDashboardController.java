package com.yourapp.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yourapp.dto.department.DepartmentRequest;
import com.yourapp.model.entity.Department;
import com.yourapp.model.entity.User;
import com.yourapp.model.enums.Role;
import com.yourapp.security.CustomUserDetails;
import com.yourapp.service.DepartmentService;
import com.yourapp.service.TenantService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Controller
public class AdminDashboardController {

    private final TenantService tenantService;
    private final DepartmentService departmentService;

    public AdminDashboardController(
            TenantService tenantService,
            DepartmentService departmentService) {
        this.tenantService = tenantService;
        this.departmentService = departmentService;
    }

    public record ApproveJoinRequest(
            @NotNull(message = "Role is required before approving a user")
            Role role,
            UUID departmentId
    ) {}

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (userDetails.getTenantId() == null) {
            return "redirect:/companies/choose";
        }

        if (!Role.COMPANY_ADMIN.name().equals(userDetails.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("tenantId", userDetails.getTenantId());
        model.addAttribute("assignableRoles", List.of(
                Role.EXECUTIVE,
                Role.DEPARTMENT_MANAGER,
                Role.EMPLOYEE
        ));

        return "company/AdminDashboard";
    }

    @GetMapping("/api/admin/join-requests")
    @ResponseBody
    public ResponseEntity<List<User>> getPendingJoinRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(tenantService.getPendingJoinRequests(userDetails));
    }

    @PostMapping("/api/admin/join-requests/{userId}/approve")
    @ResponseBody
    public ResponseEntity<User> approveJoinRequest(
            @PathVariable UUID userId,
            @Valid @RequestBody ApproveJoinRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User approvedUser = tenantService.approveJoinRequest(
                userId,
                request.role(),
                request.departmentId(),
                userDetails
        );
        return ResponseEntity.ok(approvedUser);
    }

    @PostMapping("/api/admin/join-requests/{userId}/deny")
    @ResponseBody
    public ResponseEntity<Void> denyJoinRequest(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        tenantService.denyJoinRequest(userId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/departments")
    @ResponseBody
    public ResponseEntity<List<Department>> listDepartments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID tenantId = requireAdminTenantId(userDetails);
        return ResponseEntity.ok(departmentService.getDepartments(tenantId));
    }

    @PostMapping("/api/admin/departments")
    @ResponseBody
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody DepartmentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID tenantId = requireAdminTenantId(userDetails);
        Department created = departmentService.createDepartment(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private UUID requireAdminTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new IllegalStateException("Authenticated user details are missing");
        }
        if (!Role.COMPANY_ADMIN.name().equals(userDetails.getRole())) {
            throw new AccessDeniedException("Only COMPANY_ADMIN can access admin resources");
        }
        if (userDetails.getTenantId() == null) {
            throw new IllegalStateException("Admin does not belong to a company");
        }
        return userDetails.getTenantId();
    }
}