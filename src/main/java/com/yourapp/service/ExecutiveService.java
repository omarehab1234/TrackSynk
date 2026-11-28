package com.yourapp.service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yourapp.dto.auth.ExecutiveDashboardResponse;
import com.yourapp.model.enums.ObjectiveStatus;
import com.yourapp.model.enums.TaskStatus;
import com.yourapp.repository.DepartmentRepository;
import com.yourapp.repository.KpiRepository;
import com.yourapp.repository.ObjectiveRepository;
import com.yourapp.repository.TaskRepository;
import com.yourapp.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ExecutiveService {

    private final ObjectiveRepository objectiveRepository;
    private final DepartmentRepository departmentRepository;
    private final KpiRepository kpiRepository;
    private final TaskRepository taskRepository;

    public ExecutiveDashboardResponse getDashboard(
            CustomUserDetails userDetails) {

        UUID tenantId = userDetails.getTenantId();

        long activeObjectiveCount = getActiveObjectiveCount(tenantId);
        long departmentCount = getDepartmentCount(tenantId);
        long overdueTaskCount = getOverdueTaskCount(tenantId);

        List<ExecutiveDashboardResponse.ObjectiveSummary> companyObjectives =
                getCompanyObjectives(tenantId);

        List<ExecutiveDashboardResponse.DepartmentSummary> departments =
                getDepartments(tenantId);

        List<ExecutiveDashboardResponse.KpiSummary> kpis =
                getKpis(tenantId);

        List<ExecutiveDashboardResponse.OverdueTaskSummary> overdueTasks =
                getOverdueTasks(tenantId);

        return new ExecutiveDashboardResponse(
                activeObjectiveCount,
                departmentCount,
                overdueTaskCount,
                companyObjectives,
                departments,
                kpis,
                overdueTasks
        );
    }

    // Statistics

    private long getActiveObjectiveCount(UUID tenantId) {
        return objectiveRepository.countByTenantIdAndStatus(
                tenantId, ObjectiveStatus.UNCOMPLETED);
    }

    private long getDepartmentCount(UUID tenantId) {
        return departmentRepository.countByTenantId(tenantId);
    }

    private long getOverdueTaskCount(UUID tenantId) {
        return taskRepository.countByTenantIdAndDueDateBeforeAndStatusNotIn(
                tenantId, LocalDate.now(), List.of(TaskStatus.DONE, TaskStatus.CANCELLED));
    }

    // Lists

    private List<ExecutiveDashboardResponse.ObjectiveSummary>
    getCompanyObjectives(UUID tenantId) {

        return objectiveRepository.findByTenantId(tenantId)
                .stream()
                .map(objective ->
                        new ExecutiveDashboardResponse.ObjectiveSummary(
                                objective.getId(),
                                objective.getTitle(),
                                objective.getStatus().name(),
                                objective.getEndDate()
                        ))
                .toList();
    }

    private List<ExecutiveDashboardResponse.DepartmentSummary>
    getDepartments(UUID tenantId) {

        return departmentRepository.findAllByTenantId(tenantId)
                .stream()
                .map(department ->
                        new ExecutiveDashboardResponse.DepartmentSummary(
                                department.getId(),
                                department.getName(),
                                department.getDescription()
                        ))
                .toList();
    }

    private List<ExecutiveDashboardResponse.KpiSummary>
    getKpis(UUID tenantId) {

        return kpiRepository.findAllByTenantId(tenantId)
                .stream()
                .map(kpi ->
                        new ExecutiveDashboardResponse.KpiSummary(
                                kpi.getId(),
                                kpi.getName(),
                                kpi.getTargetValue(),
                                kpi.getUnit(),
                                kpi.getFrequency().name()
                        ))
                .toList();
    }

    private List<ExecutiveDashboardResponse.OverdueTaskSummary>
    getOverdueTasks(UUID tenantId) {

        return taskRepository.findAllByTenantIdAndDueDateBeforeAndStatusNotIn(
                        tenantId, LocalDate.now(),List.of(TaskStatus.DONE, TaskStatus.CANCELLED))
                .stream()
                .map(task ->
                        new ExecutiveDashboardResponse.OverdueTaskSummary(
                                task.getId(),
                                task.getTitle(),
                                task.getStatus().name(),
                                task.getDueDate()
                        ))
                .toList();
    }
}