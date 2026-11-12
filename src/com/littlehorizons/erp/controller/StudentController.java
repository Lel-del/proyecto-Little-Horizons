package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EnrollmentDAO;
import com.littlehorizons.erp.dao.GradeDAO;
import com.littlehorizons.erp.dao.GuardianDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.dao.SubjectDAO;
import com.littlehorizons.erp.dao.SubjectEnrollmentDAO;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.AppLogger;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic controller for student registration, guardians and enrollment.
 */
public class StudentController {

    private final StudentDAO studentDAO = new StudentDAO();
    private final GuardianDAO guardianDAO = new GuardianDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final GradeDAO gradeDAO = new GradeDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final SubjectEnrollmentDAO subjectEnrollmentDAO = new SubjectEnrollmentDAO();

    public int registerStudent(Student student) {
        validateStudent(student);
        if (studentDAO.existsDocument(student.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un estudiante con el documento " + student.getDocumentNumber());
        }
        int id = studentDAO.insert(student);
        AppLogger.getInstance().logInfo("StudentController.registerStudent", "Estudiante registrado ID " + id);
        return id;
    }

    public boolean updateStudent(Student student) {
        validateStudent(student);
        if (student.getStudentId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un estudiante válido para actualizar.");
        }
        boolean updated = studentDAO.update(student);
        AppLogger.getInstance().logInfo("StudentController.updateStudent", "Estudiante actualizado ID " + student.getStudentId());
        return updated;
    }

    public boolean deleteStudent(int studentId) {
        Student existing = studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        boolean deleted = studentDAO.delete(existing.getStudentId());
        AppLogger.getInstance().logInfo("StudentController.deleteStudent", "Estudiante eliminado ID " + studentId);
        return deleted;
    }

    public Student findStudent(int studentId) {
        return studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
    }

    public List<Student> listStudents() {
        return studentDAO.findAll();
    }

    public List<Student> searchStudents(String term) {
        return studentDAO.searchByName(term);
    }

    public List<Student> listStudentsByStatus(String status) {
        return studentDAO.findByStatus(status);
    }

    public List<Student> listStudentsByGuardian(int guardianId) {
        return studentDAO.findByGuardian(guardianId);
    }

    public List<Grade> listGrades() {
        return gradeDAO.findAll();
    }

    public List<Subject> listSubjectsByGrade(int gradeId) {
        return subjectDAO.findByGrade(gradeId);
    }

    public int registerGuardian(Guardian guardian) {
        validateGuardian(guardian);
        if (guardianDAO.existsDocument(guardian.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un acudiente con el documento " + guardian.getDocumentNumber());
        }
        int id = guardianDAO.insert(guardian);
        AppLogger.getInstance().logInfo("StudentController.registerGuardian", "Acudiente registrado ID " + id);
        return id;
    }

    public boolean updateGuardian(Guardian guardian) {
        validateGuardian(guardian);
        if (guardian.getGuardianId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un acudiente válido para actualizar.");
        }
        return guardianDAO.update(guardian);
    }

    public boolean deleteGuardian(int guardianId) {
        return guardianDAO.delete(guardianId);
    }

    public Guardian findGuardian(int guardianId) {
        return guardianDAO.findById(guardianId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el acudiente con ID " + guardianId));
    }

    public List<Guardian> listGuardians() {
        return guardianDAO.findAll();
    }

    public List<Guardian> listGuardiansByStudent(int studentId) {
        return guardianDAO.findByStudent(studentId);
    }

    /**
     * Associates a guardian with a student. Every student must keep at least one guardian.
     */
    public void linkGuardian(int studentId, int guardianId, boolean principal, boolean emergencyContact) {
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        guardianDAO.findById(guardianId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el acudiente con ID " + guardianId));
        if (!guardianDAO.linkToStudent(studentId, guardianId, principal, emergencyContact)) {
            throw new IllegalStateException("No fue posible asociar el acudiente al estudiante.");
        }
        AppLogger.getInstance().logInfo("StudentController.linkGuardian",
                "Acudiente " + guardianId + " asociado al estudiante " + studentId);
    }

    public void unlinkGuardian(int studentId, int guardianId) {
        List<Guardian> current = guardianDAO.findByStudent(studentId);
        if (current.size() <= 1) {
            throw new IllegalStateException("El estudiante debe tener al menos un acudiente asociado.");
        }
        if (!guardianDAO.unlinkFromStudent(studentId, guardianId)) {
            throw new IllegalStateException("No fue posible desasociar el acudiente indicado.");
        }
    }

    /**
     * Enrolls a student in a grade for an academic year (matrícula).
     */
    public int enrollStudentInGrade(int studentId, int gradeId, int academicYear, LocalDate enrollmentDate, String observations) {
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        gradeDAO.findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el grado con ID " + gradeId));
        if (academicYear < 2000 || academicYear > 2100) {
            throw new IllegalArgumentException("El año académico debe estar entre 2000 y 2100.");
        }
        if (enrollmentDAO.existsForYear(studentId, academicYear)) {
            throw new IllegalStateException("El estudiante ya tiene una matrícula registrada para el año " + academicYear);
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setGradeId(gradeId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setEnrollmentStatus("Activa");
        enrollment.setEnrollmentDate(enrollmentDate != null ? enrollmentDate : LocalDate.now());
        enrollment.setObservations(observations);
        int id = enrollmentDAO.insert(enrollment);
        AppLogger.getInstance().logInfo("StudentController.enrollStudentInGrade",
                "Matrícula " + id + " creada para estudiante " + studentId);
        return id;
    }

    public List<Enrollment> listEnrollmentsByStudent(int studentId) {
        return enrollmentDAO.findByStudent(studentId);
    }

    public List<Enrollment> listEnrollmentsByYear(int academicYear) {
        return enrollmentDAO.findByAcademicYear(academicYear);
    }

    public boolean withdrawEnrollment(int enrollmentId, LocalDate withdrawalDate) {
        return enrollmentDAO.updateStatus(enrollmentId, "Retirada", withdrawalDate);
    }

    public int enrollStudentInSubject(int studentId, int subjectId, int academicYear, String period,
                                      String section, LocalDate enrollmentDate) {
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        Subject subject = subjectDAO.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la materia con ID " + subjectId));
        if (!subject.isActive()) {
            throw new IllegalStateException("La materia seleccionada no está activa.");
        }
        if (subjectEnrollmentDAO.exists(studentId, subjectId, academicYear, period)) {
            throw new IllegalStateException("El estudiante ya está inscrito en esa materia para el período indicado.");
        }
        SubjectEnrollment enrollment = new SubjectEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setSubjectId(subjectId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setAcademicPeriod(period);
        enrollment.setSection(section);
        enrollment.setEnrollmentDate(enrollmentDate != null ? enrollmentDate : LocalDate.now());
        enrollment.setStatus("Activa");
        int id = subjectEnrollmentDAO.insert(enrollment);
        AppLogger.getInstance().logInfo("StudentController.enrollStudentInSubject",
                "Inscripción " + id + " creada para estudiante " + studentId + " en materia " + subjectId);
        return id;
    }

    public List<SubjectEnrollment> listSubjectEnrollments(int studentId, int academicYear) {
        return subjectEnrollmentDAO.findByStudent(studentId, academicYear);
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Los datos del estudiante son obligatorios.");
        }
        if (isBlank(student.getFirstName()) || isBlank(student.getLastName())) {
            throw new IllegalArgumentException("El nombre y el primer apellido del estudiante son obligatorios.");
        }
        if (isBlank(student.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento del estudiante es obligatorio.");
        }
        if (student.getBirthDate() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (student.getBirthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        if (student.getEntryDate() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }
        if (student.getEntryDate().isBefore(student.getBirthDate())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser anterior a la fecha de nacimiento.");
        }
        if (isBlank(student.getGender())) {
            throw new IllegalArgumentException("El género es obligatorio.");
        }
    }

    private void validateGuardian(Guardian guardian) {
        if (guardian == null) {
            throw new IllegalArgumentException("Los datos del acudiente son obligatorios.");
        }
        if (isBlank(guardian.getFirstName()) || isBlank(guardian.getLastName())) {
            throw new IllegalArgumentException("El nombre y el primer apellido del acudiente son obligatorios.");
        }
        if (isBlank(guardian.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento del acudiente es obligatorio.");
        }
        if (isBlank(guardian.getRelationship())) {
            throw new IllegalArgumentException("El parentesco del acudiente es obligatorio.");
        }
        if (isBlank(guardian.getPrimaryPhone())) {
            throw new IllegalArgumentException("El teléfono principal del acudiente es obligatorio.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
