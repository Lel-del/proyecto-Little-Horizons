package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDAO;
import com.littlehorizons.erp.dao.ReportDAO;
import com.littlehorizons.erp.service.ReportExportService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Business logic controller for statistics and consolidated reports.
 */
public class ReportController {

    private final ReportDAO reportDAO = new ReportDAO();
    private final AcademicRecordDAO recordDAO = new AcademicRecordDAO();

    public List<String> studentCountByStatus() {
        List<String> lines = new ArrayList<>();
        reportDAO.countStudentsByStatus().forEach((status, total) ->
                lines.add(String.format("%-15s | Estudiantes: %d", status, total)));
        return lines;
    }

    public List<String> studentCountByGrade(int academicYear) {
        return reportDAO.countStudentsByGrade(academicYear);
    }

    public List<String> incomeByConcept(int academicYear) {
        List<String> lines = new ArrayList<>();
        Map<String, BigDecimal> income = reportDAO.incomeByConcept(academicYear);
        income.forEach((concept, total) ->
                lines.add(String.format("%-30s | Recaudado: $%,.2f", concept, total)));
        lines.add("");
        lines.add(String.format("TOTAL RECAUDADO EN %d: $%,.2f", academicYear, reportDAO.totalIncome(academicYear)));
        return lines;
    }

    public List<String> averagesBySubject(int academicYear, String period) {
        return reportDAO.averageBySubject(academicYear, period);
    }

    public List<String> clubParticipation() {
        List<String> lines = new ArrayList<>();
        reportDAO.participationByClub().forEach((club, total) ->
                lines.add(String.format("%-45s | Miembros activos: %d", club, total)));
        return lines;
    }

    public List<String> resourceStatus() {
        List<String> lines = new ArrayList<>();
        reportDAO.resourcesByStatus().forEach((status, total) ->
                lines.add(String.format("%-22s | Recursos: %d", status, total)));
        return lines;
    }

    public List<String> lowPerformanceReport(BigDecimal threshold, int academicYear, String period) {
        return recordDAO.findLowPerformingStudents(threshold, academicYear, period);
    }

    public Path exportReport(String reportName, String title, List<String> lines) {
        return ReportExportService.exportReport(reportName, title, lines);
    }
}
