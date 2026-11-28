package com.yourapp.dto.auth;
// here we define the CompanyStatisticsResponse class, used to encapsulate various statistics related to a company.
// class includes fields for total departments, total employees, total managers, active KPIs, average KPI achievement,
// and counts of achieved, behind target, and not started KPIs.code.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatisticsResponse {

    private long totalDepartments;
    private long totalEmployees;
    private long totalManagers;

    private long activeKpis;

    private double averageKpiAchievement;

    private long achievedKpis;
    private long behindTargetKpis;
    private long notStartedKpis;
}