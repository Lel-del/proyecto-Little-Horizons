package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Console view for teacher directory and academic load assignment.
 */
public class TeacherView {

    private final InputValidator input;
    private final TeacherController controller = new TeacherController();

    public TeacherView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("GESTIÓN DE PROFESORES");
            System.out.println("1. Registrar profesor");
            System.out.println("2. Listar profesores");
            System.out.println("3. Buscar profesor por nombre");
            System.out.println("4. Actualizar profesor");
            System.out.println("5. Eliminar profesor");
            System.out.println("6. Asignar materia y horario");
            System.out.println("7. Consultar carga académica");
            System.out.println("8. Listar todas las asignaciones");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 8);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("TeacherView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("TeacherView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                registerTeacher();
                break;
            case 2:
                listTeachers();
                break;
            case 3:
                searchTeachers();
                break;
            case 4:
                updateTeacher();
                break;
            case 5:
                deleteTeacher();
                break;
            case 6:
                assignSubject();
                break;
            case 7:
                consultLoad();
                break;
            case 8:
                listAssignments();
                break;
            default:
                break;
        }
    }

    private void registerTeacher() {
        ConsolePrinter.printSectionHeader("NUEVO PROFESOR");
        Teacher teacher = new Teacher();
        teacher.setDocumentType(input.readChoice("Tipo de documento", Arrays.asList("CC", "CE", "Pasaporte")));
        teacher.setDocumentNumber(input.readNonEmptyString("Número de documento: "));
        teacher.setFirstName(input.readNonEmptyString("Primer nombre: "));
        teacher.setMiddleName(input.readOptionalString("Segundo nombre (opcional): "));
        teacher.setLastName(input.readNonEmptyString("Primer apellido: "));
        teacher.setSecondLastName(input.readOptionalString("Segundo apellido (opcional): "));
        teacher.setEmail(input.readEmail("Correo electrónico: ", true));
        teacher.setPhone(input.readOptionalString("Teléfono: "));
        teacher.setSpecialization(input.readNonEmptyString("Especialización: "));
        teacher.setHireDate(input.readDate("Fecha de contratación", true));
        teacher.setStatus(input.readChoice("Estado", Arrays.asList("Activo", "Inactivo", "Licencia")));
        int id = controller.registerTeacher(teacher);
        ConsolePrinter.printSuccess("Profesor registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listTeachers() {
        ConsolePrinter.printSectionHeader("DIRECTORIO DE PROFESORES");
        List<Teacher> teachers = controller.listTeachers();
        if (teachers.isEmpty()) {
            ConsolePrinter.printWarning("No hay profesores registrados.");
        } else {
            teachers.forEach(System.out::println);
            ConsolePrinter.printInfo("Total: " + teachers.size() + " profesores.");
        }
        input.waitForEnter();
    }

    private void searchTeachers() {
        String term = input.readNonEmptyString("Nombre o apellido a buscar: ");
        List<Teacher> teachers = controller.searchTeachers(term);
        if (teachers.isEmpty()) {
            ConsolePrinter.printWarning("No se encontraron profesores con ese criterio.");
        } else {
            teachers.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void updateTeacher() {
        int id = input.readPositiveInt("ID del profesor a actualizar: ");
        Teacher teacher = controller.findTeacher(id);
        teacher.setDocumentType(input.readStringWithDefault("Tipo de documento", teacher.getDocumentType()));
        teacher.setDocumentNumber(input.readStringWithDefault("Número de documento", teacher.getDocumentNumber()));
        teacher.setFirstName(input.readStringWithDefault("Primer nombre", teacher.getFirstName()));
        teacher.setMiddleName(input.readStringWithDefault("Segundo nombre", teacher.getMiddleName()));
        teacher.setLastName(input.readStringWithDefault("Primer apellido", teacher.getLastName()));
        teacher.setSecondLastName(input.readStringWithDefault("Segundo apellido", teacher.getSecondLastName()));
        teacher.setEmail(input.readStringWithDefault("Correo electrónico", teacher.getEmail()));
        teacher.setPhone(input.readStringWithDefault("Teléfono", teacher.getPhone()));
        teacher.setSpecialization(input.readStringWithDefault("Especialización", teacher.getSpecialization()));
        teacher.setHireDate(input.readDateWithDefault("Fecha de contratación", teacher.getHireDate()));
        teacher.setStatus(input.readStringWithDefault("Estado", teacher.getStatus()));
        controller.updateTeacher(teacher);
        ConsolePrinter.printSuccess("Profesor actualizado correctamente.");
        input.waitForEnter();
    }

    private void deleteTeacher() {
        int id = input.readPositiveInt("ID del profesor a eliminar: ");
        Teacher teacher = controller.findTeacher(id);
        if (!input.readBooleanYN("¿Confirma eliminar a " + teacher.getFullName() + "?")) {
            ConsolePrinter.printInfo("Operación cancelada.");
            input.waitForEnter();
            return;
        }
        controller.deleteTeacher(id);
        ConsolePrinter.printSuccess("Profesor eliminado.");
        input.waitForEnter();
    }

    private void assignSubject() {
        ConsolePrinter.printSectionHeader("ASIGNACIÓN DE MATERIA Y HORARIO");
        int teacherId = input.readPositiveInt("ID del profesor: ");
        int subjectId = input.readPositiveInt("ID de la materia: ");
        int year = input.readInt("Año académico: ", 2000, 2100);
        String section = input.readNonEmptyString("Sección (ej: A): ");
        String day = input.readChoice("Día de la semana",
                Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"));
        LocalTime start = input.readTime("Hora de inicio", true);
        LocalTime end = input.readTime("Hora de fin", true);
        int id = controller.assignSubjectToTeacher(teacherId, subjectId, year, section, day, start, end);
        ConsolePrinter.printSuccess("Asignación registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void consultLoad() {
        int teacherId = input.readPositiveInt("ID del profesor: ");
        Teacher teacher = controller.findTeacher(teacherId);
        ConsolePrinter.printSectionHeader("CARGA ACADÉMICA DE " + teacher.getFullName().toUpperCase());
        List<TeacherAssignment> assignments = controller.listAssignmentsByTeacher(teacherId);
        if (assignments.isEmpty()) {
            ConsolePrinter.printWarning("El profesor no tiene asignaciones registradas.");
        } else {
            assignments.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void listAssignments() {
        ConsolePrinter.printSectionHeader("TODAS LAS ASIGNACIONES ACADÉMICAS");
        List<TeacherAssignment> assignments = controller.listAllAssignments();
        if (assignments.isEmpty()) {
            ConsolePrinter.printWarning("No hay asignaciones registradas.");
        } else {
            assignments.forEach(System.out::println);
        }
        input.waitForEnter();
    }
}
