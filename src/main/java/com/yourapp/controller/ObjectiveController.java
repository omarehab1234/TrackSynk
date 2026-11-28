
package com.yourapp.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yourapp.dto.objective.CreateObjectiveRequest;
import com.yourapp.model.entity.Department;
import com.yourapp.security.CustomUserDetails;
import com.yourapp.service.DepartmentService;
import com.yourapp.service.ObjectiveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ObjectiveController {

    private final ObjectiveService objectiveService;
    private final DepartmentService departmentService;

    // Load departments
    private void loadDepartments(
            Model model,
            CustomUserDetails userDetails) {

        List<Department> departments =
                departmentService.getDepartments(userDetails.getTenantId());

        model.addAttribute("departments", departments);
    }

    // GET: Open objective creation page
    @GetMapping("/executive/objectives/new")
    public String showCreateObjectiveForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        loadDepartments(model, userDetails);

        model.addAttribute(
                "objectiveRequest",
                new CreateObjectiveRequest()
        );

        return "executive/objective-create";
    }

    // POST: Create objective
    @PostMapping("/executive/objectives")
    public String createObjective(
            @Valid @ModelAttribute("objectiveRequest")
            CreateObjectiveRequest request,

            BindingResult bindingResult,

            @AuthenticationPrincipal CustomUserDetails userDetails,

            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            loadDepartments(model, userDetails);
            return "executive/objective-create";
        }

        try {
            objectiveService.createExecutiveObjective(request);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Company objective created successfully!"
            );

            return "redirect:/executive/dashboard";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());
            loadDepartments(model, userDetails);

            return "executive/objective-create";
        }
    }
}
