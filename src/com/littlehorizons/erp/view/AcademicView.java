package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Console view for academic management: subjects, grades, attendance and averages.
 */
public class AcademicView {

    private final InputValidator input;
    private final AcademicController controller = new AcademicController();

    public AcademicView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("GESTIÓN ACADÉMICA");
            System.out.println("1. Registrar materia");
            System.out.println("2. Listar materias");
            System.out.println("3. Actualizar materia");
            System.out.println("4. Registrar calificación");
            System.out.println("5. Consultar notas por estudiante");
            System.out.println("6. Consultar historial académico");
            System.out.println("7. Calcular promedios");
            System.out.println("8. Registrar asistencia");
            System.out.println("9. Reporte de inasistencias");
            System.out.println("10. Reporte de bajo rendimiento");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 10);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("AcademicView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("AcademicView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                registerSubject();
                break;
            case 2:
                listSubjects();
                break;
            case 3:
                updateSubject();
                break;
            case 4:
                registerGrade();
                break;
            case 5:
                consultGrades();
                break;
            case 6:
                consultHistory();
                break;
            case 7:
                calculateAverages();
                break;
            case 8:
                registerAttendance();
                break;
            case 9:
                absenceReport();
                break;
            case 10:
                lowPerformanceReport();
                break;
            default:
                break;
        }
    }

    private void registerSubject() {
        ConsolePrinter.printSectionHeader("NUEVA MATERIA");
        controller.listGrades().forEach(System.out::println);
        Subject subject = new Subject();
        subject.setSubjectCode(input.readNonEmptyString("Código de la materia: "));
        subject.setSubjectName(input.readNonEmptyString("Nombre de la materia: "));
        subject.setDescription(input.readOptionalString("Descripción (opcional): "));
        subject.setGradeId(input.readPositiveInt("ID del grado: "));
        subject.setWeeklyHours(input.readInt("Horas semanales: ", 1, 40));
        subject.setActive(input.readBooleanYN("¿Materia activa?"));
        int id = controller.registerSubject(subject);
        ConsolePrinter.printSuccess("Materia registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void listSubjects() {
        ConsolePrinter.printSectionHeader("CATÁLOGO DE MATERIAS");
        List<Subject> subjects = controller.listSubjects();
        if (subjects.isEmpty()) {
            ConsolePrinter.printWarning("No hay materias registradas.");
        } else {
            subjects.forEach(System.out::println);
            ConsolePrinter.printInfo("Total: " + subjects.size() + " materias.");
        }
        input.waitForEnter();
    }

    private void updateSubject() {
        int id = input.readPositiveInt("ID de la materia a actualizar: ");
        Subject subject = controller.findSubject(id);
        subject.setSubjectCode(input.readStringWithDefault("Código", subject.getSubjectCode()));
        subject.setSubjectName(input.readStringWithDefault("Nombre", subject.getSubjectName()));
        subject.setDescription(input.readStringWithDefault("Descripción", subject.getDescription()));
        subject.setGradeId(input.readInt("ID del grado [" + subject.getGradeId() + "]: ", 1, 99));
        subject.setWeeklyHours(input.readInt("Horas semanales [" + subject.getWeeklyHours() + "]: ", 1, 40));
        subject.setActive(input.readBooleanYN("¿Materia activa?"));
        controller.updateSubject(subject);
        ConsolePrinter.printSuccess("Materia actualizada correctamente.");
        input.waitForEnter();
    }

    private void registerGrade() {
        ConsolePrinter.printSectionHeader("REGISTRO DE CALIFICACIÓN");
        AcademicRecord record = new AcademicRecord();
        record.setStudentId(input.readPositiveInt("ID del estudiante: "));
        record.setSubjectId(input.readPositiveInt("ID de la materia: "));
        record.setTeacherId(input.readPositiveInt("ID del profesor: "));
        record.setAcademicYear(input.readInt("Año académico: ", 2000, 2100));
        record.setAcademicPeriod(input.readChoice("Período", Arrays.asList("I", "II", "III", "IV", "Final")));
        record.setGradeValue(input.readGrade("Nota (0.0 - 5.0): "));
        record.setObservations(input.readOptionalString("Observaciones (opcional): "));
        record.setRecordDate(input.readDate("Fecha de registro", true));
        int id = controller.registerGradeRecord(record);
        ConsolePrinter.printSuccess("Calificación registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void consultGrades() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        int year = input.readInt("Año académico: ", 2000, 2100);
        ConsolePrinter.printSectionHeader("NOTAS DEL ESTUDIANTE");
        List<AcademicRecord> records = controller.listRecordsByStudent(studentId, year);
        if (records.isEmpty()) {
            ConsolePrinter.printWarning("No hay notas registradas para ese año.");
        } else {
            records.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void consultHistory() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        ConsolePrinter.printSectionHeader("HISTORIAL ACADÉMICO COMPLETO");
        List<AcademicRecord> history = controller.listStudentHistory(studentId);
        if (history.isEmpty()) {
            ConsolePrinter.printWarning("El estudiante no tiene historial académico registrado.");
        } else {
            history.forEach(System.out::println);
            ConsolePrinter.printInfo("Total de registros: " + history.size());
        }
        input.waitForEnter();
    }

    private void calculateAverages() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        int year = input.readInt("Año académico: ", 2000, 2100);
        String period = input.readChoice("Período", Arrays.asList("I", "II", "III", "IV"));
        Map<String, BigDecimal> averages = controller.calculateStudentAverages(studentId, year, period);
        ConsolePrinter.printSectionHeader("PROMEDIOS DEL ESTUDIANTE");
        if (averages.isEmpty()) {
            ConsolePrinter.printWarning("No hay información suficiente para calcular promedios.");
        } else {
            averages.forEach((key, value) -> System.out.printf("%-55s : %s%n", key, value));
        }
        input.waitForEnter();
    }

    private void registerAttendance() {
        ConsolePrinter.printSectionHeader("REGISTRO DE ASISTENCIA");
        Attendance attendance = new Attendance();
        attendance.setStudentId(input.readPositiveInt("ID del estudiante: "));
        attendance.setSubjectId(input.readPositiveInt("ID de la materia: "));
        attendance.setAttendanceDate(input.readDate("Fecha de asistencia", true));
        attendance.setAttendanceStatus(input.readChoice("Estado de asistencia",
                Arrays.asList("Presente", "Ausente", "Tardanza", "Excusa Médica")));
        attendance.setObservations(input.readOptionalString("Observaciones (opcional): "));
        if (input.readBooleanYN("¿Desea registrar el profesor que toma asistencia?")) {
            attendance.setRecordedByTeacherId(input.readPositiveInt("ID del profesor: "));
        }
        int id = controller.registerAttendance(attendance);
        ConsolePrinter.printSuccess("Asistencia registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void absenceReport() {
        ConsolePrinter.printSectionHeader("REPORTE DE INASISTENCIAS");
        int minimum = input.readInt("Mínimo de ausencias para alertar: ", 1, 100);
        LocalDate from = input.readDate("Fecha inicial", true);
        LocalDate to = input.readDate("Fecha final", true);
        List<String> report = controller.generateAbsenceReport(minimum, from, to);
        if (report.isEmpty()) {
            ConsolePrinter.printInfo("No se encontraron estudiantes con ese patrón de inasistencia.");
        } else {
            report.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void lowPerformanceReport() {
        ConsolePrinter.printSectionHeader("REPORTE DE BAJO RENDIMIENTO");
        BigDecimal threshold = input.readGrade("Umbral de nota (0.0 - 5.0): ");
        int year = input.readInt("Año académico: ", 2000, 2100);
        String period = input.readChoice("Período", Arrays.asList("I", "II", "III", "IV"));
        List<String> report = controller.generateLowPerformanceReport(threshold, year, period);
        if (report.isEmpty()) {
            ConsolePrinter.printInfo("Ningún estudiante está por debajo del umbral indicado.");
        } else {
            report.forEach(System.out::println);
        }
        input.waitForEnter();
    }
}
