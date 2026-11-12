package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Console view for student, guardian and enrollment management.
 */
public class StudentView {

    private final InputValidator input;
    private final StudentController controller = new StudentController();

    public StudentView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("GESTIÓN DE ESTUDIANTES");
            System.out.println("1. Registrar estudiante");
            System.out.println("2. Listar estudiantes");
            System.out.println("3. Buscar estudiante por nombre");
            System.out.println("4. Actualizar estudiante");
            System.out.println("5. Eliminar estudiante");
            System.out.println("6. Registrar acudiente");
            System.out.println("7. Listar acudientes");
            System.out.println("8. Asociar acudiente a estudiante");
            System.out.println("9. Matricular estudiante en grado");
            System.out.println("10. Inscribir estudiante en materia");
            System.out.println("11. Consultar matrículas e inscripciones");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 11);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("StudentView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("StudentView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                registerStudent();
                break;
            case 2:
                listStudents();
                break;
            case 3:
                searchStudents();
                break;
            case 4:
                updateStudent();
                break;
            case 5:
                deleteStudent();
                break;
            case 6:
                registerGuardian();
                break;
            case 7:
                listGuardians();
                break;
            case 8:
                linkGuardian();
                break;
            case 9:
                enrollInGrade();
                break;
            case 10:
                enrollInSubject();
                break;
            case 11:
                consultEnrollments();
                break;
            default:
                break;
        }
    }

    private void registerStudent() {
        ConsolePrinter.printSectionHeader("NUEVO ESTUDIANTE");
        Student student = new Student();
        student.setDocumentType(input.readChoice("Tipo de documento", Arrays.asList("TI", "CC", "CE", "Pasaporte")));
        student.setDocumentNumber(input.readNonEmptyString("Número de documento: "));
        student.setFirstName(input.readNonEmptyString("Primer nombre: "));
        student.setMiddleName(input.readOptionalString("Segundo nombre (opcional): "));
        student.setLastName(input.readNonEmptyString("Primer apellido: "));
        student.setSecondLastName(input.readOptionalString("Segundo apellido (opcional): "));
        student.setBirthDate(input.readDate("Fecha de nacimiento", true));
        student.setGender(input.readChoice("Género", Arrays.asList("M", "F", "Otro")));
        student.setAddress(input.readOptionalString("Dirección: "));
        student.setPhone(input.readOptionalString("Teléfono: "));
        student.setEmail(input.readEmail("Correo electrónico (opcional): ", false));
        student.setBloodType(input.readOptionalString("Tipo de sangre (opcional): "));
        student.setAllergies(input.readOptionalString("Alergias (opcional): "));
        student.setMedicalConditions(input.readOptionalString("Condiciones médicas (opcional): "));
        student.setEntryDate(input.readDate("Fecha de ingreso", true));
        student.setStatus(input.readChoice("Estado", Arrays.asList("Activo", "Graduado", "Retirado", "Suspendido")));

        int id = controller.registerStudent(student);
        ConsolePrinter.printSuccess("Estudiante registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listStudents() {
        ConsolePrinter.printSectionHeader("LISTADO DE ESTUDIANTES");
        List<Student> students = controller.listStudents();
        if (students.isEmpty()) {
            ConsolePrinter.printWarning("No hay estudiantes registrados.");
        } else {
            students.forEach(System.out::println);
            ConsolePrinter.printInfo("Total: " + students.size() + " estudiantes.");
        }
        input.waitForEnter();
    }

    private void searchStudents() {
        String term = input.readNonEmptyString("Ingrese nombre o apellido a buscar: ");
        List<Student> students = controller.searchStudents(term);
        if (students.isEmpty()) {
            ConsolePrinter.printWarning("No se encontraron estudiantes con ese criterio.");
        } else {
            students.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void updateStudent() {
        int id = input.readPositiveInt("ID del estudiante a actualizar: ");
        Student student = controller.findStudent(id);
        ConsolePrinter.printInfo("Editando: " + student.getFullName() + " (deje vacío para conservar el valor actual)");
        student.setDocumentType(input.readStringWithDefault("Tipo de documento", student.getDocumentType()));
        student.setDocumentNumber(input.readStringWithDefault("Número de documento", student.getDocumentNumber()));
        student.setFirstName(input.readStringWithDefault("Primer nombre", student.getFirstName()));
        student.setMiddleName(input.readStringWithDefault("Segundo nombre", student.getMiddleName()));
        student.setLastName(input.readStringWithDefault("Primer apellido", student.getLastName()));
        student.setSecondLastName(input.readStringWithDefault("Segundo apellido", student.getSecondLastName()));
        student.setBirthDate(input.readDateWithDefault("Fecha de nacimiento", student.getBirthDate()));
        student.setGender(input.readStringWithDefault("Género (M/F/Otro)", student.getGender()));
        student.setAddress(input.readStringWithDefault("Dirección", student.getAddress()));
        student.setPhone(input.readStringWithDefault("Teléfono", student.getPhone()));
        student.setEmail(input.readStringWithDefault("Correo electrónico", student.getEmail()));
        student.setBloodType(input.readStringWithDefault("Tipo de sangre", student.getBloodType()));
        student.setAllergies(input.readStringWithDefault("Alergias", student.getAllergies()));
        student.setMedicalConditions(input.readStringWithDefault("Condiciones médicas", student.getMedicalConditions()));
        student.setEntryDate(input.readDateWithDefault("Fecha de ingreso", student.getEntryDate()));
        student.setStatus(input.readStringWithDefault("Estado", student.getStatus()));
        controller.updateStudent(student);
        ConsolePrinter.printSuccess("Estudiante actualizado correctamente.");
        input.waitForEnter();
    }

    private void deleteStudent() {
        int id = input.readPositiveInt("ID del estudiante a eliminar: ");
        Student student = controller.findStudent(id);
        if (!input.readBooleanYN("¿Confirma eliminar a " + student.getFullName() + "?")) {
            ConsolePrinter.printInfo("Operación cancelada.");
            input.waitForEnter();
            return;
        }
        controller.deleteStudent(id);
        ConsolePrinter.printSuccess("Estudiante eliminado.");
        input.waitForEnter();
    }

    private void registerGuardian() {
        ConsolePrinter.printSectionHeader("NUEVO ACUDIENTE");
        Guardian guardian = new Guardian();
        guardian.setDocumentType(input.readChoice("Tipo de documento", Arrays.asList("CC", "CE", "Pasaporte")));
        guardian.setDocumentNumber(input.readNonEmptyString("Número de documento: "));
        guardian.setFirstName(input.readNonEmptyString("Primer nombre: "));
        guardian.setMiddleName(input.readOptionalString("Segundo nombre (opcional): "));
        guardian.setLastName(input.readNonEmptyString("Primer apellido: "));
        guardian.setSecondLastName(input.readOptionalString("Segundo apellido (opcional): "));
        guardian.setRelationship(input.readNonEmptyString("Parentesco (Padre/Madre/Tutor): "));
        guardian.setPrimaryPhone(input.readNonEmptyString("Teléfono principal: "));
        guardian.setSecondaryPhone(input.readOptionalString("Teléfono secundario (opcional): "));
        guardian.setEmail(input.readEmail("Correo electrónico (opcional): ", false));
        guardian.setAddress(input.readOptionalString("Dirección: "));
        guardian.setOccupation(input.readOptionalString("Ocupación: "));
        guardian.setFinancialResponsible(input.readBooleanYN("¿Es responsable financiero?"));
        int id = controller.registerGuardian(guardian);
        ConsolePrinter.printSuccess("Acudiente registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listGuardians() {
        ConsolePrinter.printSectionHeader("LISTADO DE ACUDIENTES");
        List<Guardian> guardians = controller.listGuardians();
        if (guardians.isEmpty()) {
            ConsolePrinter.printWarning("No hay acudientes registrados.");
        } else {
            guardians.forEach(System.out::println);
            ConsolePrinter.printInfo("Total: " + guardians.size() + " acudientes.");
        }
        input.waitForEnter();
    }

    private void linkGuardian() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        int guardianId = input.readPositiveInt("ID del acudiente: ");
        boolean principal = input.readBooleanYN("¿Es acudiente principal?");
        boolean emergency = input.readBooleanYN("¿Es contacto de emergencia?");
        controller.linkGuardian(studentId, guardianId, principal, emergency);
        ConsolePrinter.printSuccess("Acudiente asociado al estudiante correctamente.");
        input.waitForEnter();
    }

    private void enrollInGrade() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        ConsolePrinter.printInfo("Grados disponibles:");
        controller.listGrades().forEach(System.out::println);
        int gradeId = input.readPositiveInt("ID del grado: ");
        int year = input.readInt("Año académico: ", 2000, 2100);
        LocalDate date = input.readDate("Fecha de matrícula", true);
        String observations = input.readOptionalString("Observaciones (opcional): ");
        int id = controller.enrollStudentInGrade(studentId, gradeId, year, date, observations);
        ConsolePrinter.printSuccess("Matrícula registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void enrollInSubject() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        int gradeId = input.readPositiveInt("ID del grado del estudiante: ");
        List<Subject> subjects = controller.listSubjectsByGrade(gradeId);
        if (subjects.isEmpty()) {
            ConsolePrinter.printWarning("El grado seleccionado no tiene materias activas registradas.");
            input.waitForEnter();
            return;
        }
        subjects.forEach(System.out::println);
        int subjectId = input.readPositiveInt("ID de la materia: ");
        int year = input.readInt("Año académico: ", 2000, 2100);
        String period = input.readChoice("Período académico", Arrays.asList("I", "II", "III", "IV"));
        String section = input.readOptionalString("Sección (opcional): ");
        LocalDate date = input.readDate("Fecha de inscripción", true);
        int id = controller.enrollStudentInSubject(studentId, subjectId, year, period, section, date);
        ConsolePrinter.printSuccess("Inscripción registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void consultEnrollments() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        List<Enrollment> enrollments = controller.listEnrollmentsByStudent(studentId);
        ConsolePrinter.printSectionHeader("MATRÍCULAS DEL ESTUDIANTE");
        if (enrollments.isEmpty()) {
            ConsolePrinter.printWarning("El estudiante no tiene matrículas registradas.");
        } else {
            enrollments.forEach(System.out::println);
        }
        int year = input.readInt("Año académico para consultar materias: ", 2000, 2100);
        List<SubjectEnrollment> subjectEnrollments = controller.listSubjectEnrollments(studentId, year);
        ConsolePrinter.printSectionHeader("MATERIAS INSCRITAS " + year);
        if (subjectEnrollments.isEmpty()) {
            ConsolePrinter.printWarning("No hay materias inscritas para ese año.");
        } else {
            subjectEnrollments.forEach(System.out::println);
        }
        input.waitForEnter();
    }
}
