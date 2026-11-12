package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Console view for reports and statistics.
 */
public class ReportView {

    private final InputValidator input;
    private final ReportController controller = new ReportController();

    public ReportView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("REPORTES Y ESTADÍSTICAS");
            System.out.println("1. Estudiantes por estado");
            System.out.println("2. Estudiantes por grado");
            System.out.println("3. Ingresos por concepto de pago");
            System.out.println("4. Promedios por materia");
            System.out.println("5. Participación en clubes");
            System.out.println("6. Recursos por estado");
            System.out.println("7. Exportar reporte consolidado a TXT");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 7);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("ReportView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("ReportView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                studentStatusReport();
                break;
            case 2:
                studentGradeReport();
                break;
            case 3:
                incomeReport();
                break;
            case 4:
                averagesReport();
                break;
            case 5:
                clubReport();
                break;
            case 6:
                resourceReport();
                break;
            case 7:
                consolidatedReport();
                break;
            default:
                break;
        }
    }

    private void studentStatusReport() {
        printLines("ESTUDIANTES POR ESTADO", controller.studentCountByStatus());
        input.waitForEnter();
    }

    private void studentGradeReport() {
        int year = input.readInt("Año académico: ", 2000, 2100);
        printLines("ESTUDIANTES POR GRADO " + year, controller.studentCountByGrade(year));
        input.waitForEnter();
    }

    private void incomeReport() {
        int year = input.readInt("Año académico: ", 2000, 2100);
        printLines("INGRESOS POR CONCEPTO " + year, controller.incomeByConcept(year));
        input.waitForEnter();
    }

    private void averagesReport() {
        int year = input.readInt("Año académico: ", 2000, 2100);
        String period = input.readChoice("Período", Arrays.asList("I", "II", "III", "IV"));
        printLines("PROMEDIOS POR MATERIA - " + year + " PERÍODO " + period,
                controller.averagesBySubject(year, period));
        input.waitForEnter();
    }

    private void clubReport() {
        printLines("PARTICIPACIÓN EN CLUBES", controller.clubParticipation());
        input.waitForEnter();
    }

    private void resourceReport() {
        printLines("RECURSOS POR ESTADO", controller.resourceStatus());
        input.waitForEnter();
    }

    private void consolidatedReport() {
        int year = input.readInt("Año académico: ", 2000, 2100);
        String period = input.readChoice("Período", Arrays.asList("I", "II", "III", "IV"));
        BigDecimal threshold = input.readGrade("Umbral de bajo rendimiento: ");

        List<String> lines = new ArrayList<>();
        lines.add("### ESTUDIANTES POR ESTADO");
        lines.addAll(controller.studentCountByStatus());
        lines.add("");
        lines.add("### ESTUDIANTES POR GRADO " + year);
        lines.addAll(controller.studentCountByGrade(year));
        lines.add("");
        lines.add("### INGRESOS POR CONCEPTO " + year);
        lines.addAll(controller.incomeByConcept(year));
        lines.add("");
        lines.add("### PROMEDIOS POR MATERIA - PERÍODO " + period);
        lines.addAll(controller.averagesBySubject(year, period));
        lines.add("");
        lines.add("### PARTICIPACIÓN EN CLUBES");
        lines.addAll(controller.clubParticipation());
        lines.add("");
        lines.add("### RECURSOS POR ESTADO");
        lines.addAll(controller.resourceStatus());
        lines.add("");
        lines.add("### ESTUDIANTES CON BAJO RENDIMIENTO (nota < " + threshold + ")");
        List<String> lowPerformance = controller.lowPerformanceReport(threshold, year, period);
        if (lowPerformance.isEmpty()) {
            lines.add("Ningún estudiante por debajo del umbral.");
        } else {
            lines.addAll(lowPerformance);
        }

        lines.forEach(System.out::println);
        if (input.readBooleanYN("¿Desea exportar este reporte a un archivo TXT?")) {
            Path path = controller.exportReport("reporte_consolidado", "REPORTE CONSOLIDADO " + year, lines);
            ConsolePrinter.printSuccess("Reporte exportado en: " + path);
        }
        input.waitForEnter();
    }

    private void printLines(String title, List<String> lines) {
        ConsolePrinter.printSectionHeader(title);
        if (lines.isEmpty()) {
            ConsolePrinter.printWarning("No hay datos disponibles para este reporte.");
        } else {
            lines.forEach(System.out::println);
        }
    }
}
