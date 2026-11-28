
package com.yourapp.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourapp.dto.objective.CreateObjectiveRequest;
import com.yourapp.model.entity.Department;
import com.yourapp.model.entity.Objective;
import com.yourapp.model.entity.User;
import com.yourapp.model.enums.ObjectiveStatus;
import com.yourapp.model.enums.Role;
import com.yourapp.repository.DepartmentRepository;
import com.yourapp.repository.ObjectiveRepository;
import com.yourapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObjectiveService {

    private final ObjectiveRepository objectiveRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    // ==========================================
    // EXECUTIVE: CREATE COMPANY OBJECTIVE
    // ==========================================

        @Transactional
        public void createExecutiveObjective(CreateObjectiveRequest request) {

                validateObjectiveDates(request);

                User executive = getCurrentExecutive();

                UUID tenantId = executive.getTenantId();

                List<Department> departments =
                        getAndValidateDepartments(
                                request.getDepartmentIds(),
                                tenantId
                        );

                Objective parent = createParentObjective(request, executive);

                parent.setTenantId(tenantId);

                objectiveRepository.save(parent);

                List<Objective> children =
                        createDepartmentObjectives(
                                request,
                                departments,
                                parent,
                                executive
                        );

                children.forEach(child -> child.setTenantId(tenantId));

                objectiveRepository.saveAll(children);
        }
    // ==========================================
    // 1. VALIDATE DATES
    // ==========================================

    private void validateObjectiveDates(CreateObjectiveRequest request) {

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }
    }

    // ==========================================
    // 2. GET AUTHENTICATED EXECUTIVE
    // ==========================================

    private User getCurrentExecutive() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.EXECUTIVE) {
            throw new AccessDeniedException(
                    "Only executives can create company objectives"
            );
        }

        return user;
    }

    // ==========================================
    // 3. GET AND VALIDATE DEPARTMENTS
    // ==========================================

    private List<Department> getAndValidateDepartments(
            List<UUID> departmentIds,
            UUID tenantId) {

        if (departmentIds == null || departmentIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one department must be selected"
            );
        }

        Set<UUID> uniqueIds = new HashSet<>(departmentIds);

        if (uniqueIds.size() != departmentIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate departments are not allowed"
            );
        }

        List<Department> departments =
                departmentRepository.findAllById(departmentIds);

        if (departments.size() != departmentIds.size()) {
            throw new IllegalArgumentException(
                    "One or more departments do not exist"
            );
        }

        boolean invalidTenant = departments.stream()
                .anyMatch(department ->
                        !tenantId.equals(department.getTenantId()));

        if (invalidTenant) {
            throw new IllegalArgumentException(
                    "You cannot assign objectives to another company's departments"
            );
        }

        return departments;
    }

    // ==========================================
    // 4. CREATE PARENT OBJECTIVE
    // ==========================================

    private Objective createParentObjective(
            CreateObjectiveRequest request,
            User executive) {

        Objective objective = new Objective();

        objective.setTitle(request.getTitle());
        objective.setDescription(request.getDescription());
        objective.setStartDate(request.getStartDate());
        objective.setEndDate(request.getEndDate());

        objective.setStatus(ObjectiveStatus.UNCOMPLETED);

        objective.setDepartment(null);
        objective.setParentObjective(null);

        objective.setCreatedBy(executive);

        return objective;
    }

    // ==========================================
    // 5. CREATE DEPARTMENT SUB-OBJECTIVES
    // ==========================================

    private List<Objective> createDepartmentObjectives(
            CreateObjectiveRequest request,
            List<Department> departments,
            Objective parent,
            User executive) {

        List<Objective> children = new ArrayList<>();

        for (Department department : departments) {

            Objective child = new Objective();

            child.setTitle(request.getTitle());
            child.setDescription(request.getDescription());

            child.setStartDate(request.getStartDate());
            child.setEndDate(request.getEndDate());

            child.setStatus(ObjectiveStatus.UNCOMPLETED);

            child.setDepartment(department);
            child.setParentObjective(parent);

            child.setCreatedBy(executive);

            children.add(child);
        }

        return children;
    }
}
