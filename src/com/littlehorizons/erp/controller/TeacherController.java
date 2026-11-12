package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SubjectDAO;
import com.littlehorizons.erp.dao.TeacherAssignmentDAO;
import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.AppLogger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic controller for teachers and academic load assignments.
 */
public class TeacherController {

    private static final List<String> VALID_DAYS =
            Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado");

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherAssignmentDAO assignmentDAO = new TeacherAssignmentDAO();

    public int registerTeacher(Teacher teacher) {
        validateTeacher(teacher);
        if (teacherDAO.existsDocument(teacher.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un profesor con el documento " + teacher.getDocumentNumber());
        }
        int id = teacherDAO.insert(teacher);
        AppLogger.getInstance().logInfo("TeacherController.registerTeacher", "Profesor registrado ID " + id);
        return id;
    }

    public boolean updateTeacher(Teacher teacher) {
        validateTeacher(teacher);
        if (teacher.getTeacherId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un profesor válido para actualizar.");
        }
        return teacherDAO.update(teacher);
    }

    public boolean deleteTeacher(int teacherId) {
        return teacherDAO.delete(teacherId);
    }

    public Teacher findTeacher(int teacherId) {
        return teacherDAO.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el profesor con ID " + teacherId));
    }

    public List<Teacher> listTeachers() {
        return teacherDAO.findAll();
    }

    public List<Teacher> listActiveTeachers() {
        return teacherDAO.findActive();
    }

    public List<Teacher> searchTeachers(String term) {
        return teacherDAO.searchByName(term);
    }

    public int assignSubjectToTeacher(int teacherId, int subjectId, int academicYear, String section,
                                      String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        teacherDAO.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el profesor con ID " + teacherId));
        Subject subject = subjectDAO.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la materia con ID " + subjectId));
        if (academicYear < 2000 || academicYear > 2100) {
            throw new IllegalArgumentException("El año académico debe estar entre 2000 y 2100.");
        }
        if (dayOfWeek != null && !VALID_DAYS.contains(dayOfWeek)) {
            throw new IllegalArgumentException("Día inválido. Use: " + VALID_DAYS);
        }
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (dayOfWeek != null && startTime != null && endTime != null
                && assignmentDAO.hasScheduleConflict(teacherId, academicYear, dayOfWeek, startTime, endTime, null)) {
            throw new IllegalStateException("El profesor ya tiene una clase asignada en ese día y franja horaria.");
        }
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setTeacherId(teacherId);
        assignment.setSubjectId(subjectId);
        assignment.setAcademicYear(academicYear);
        assignment.setSection(section);
        assignment.setDayOfWeek(dayOfWeek);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        int id = assignmentDAO.insert(assignment);
        AppLogger.getInstance().logInfo("TeacherController.assignSubjectToTeacher",
                "Asignación " + id + " -> profesor " + teacherId + ", materia " + subject.getSubjectCode());
        return id;
    }

    public boolean deleteAssignment(int assignmentId) {
        return assignmentDAO.delete(assignmentId);
    }

    public List<TeacherAssignment> listAssignmentsByTeacher(int teacherId) {
        return assignmentDAO.findByTeacher(teacherId);
    }

    public List<TeacherAssignment> listAllAssignments() {
        return assignmentDAO.findAll();
    }

    private void validateTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Los datos del profesor son obligatorios.");
        }
        if (isBlank(teacher.getFirstName()) || isBlank(teacher.getLastName())) {
            throw new IllegalArgumentException("El nombre y el primer apellido del profesor son obligatorios.");
        }
        if (isBlank(teacher.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento del profesor es obligatorio.");
        }
        if (isBlank(teacher.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico del profesor es obligatorio.");
        }
        if (teacher.getHireDate() == null) {
            throw new IllegalArgumentException("La fecha de contratación es obligatoria.");
        }
        if (teacher.getHireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de contratación no puede ser futura.");
        }
        if (isBlank(teacher.getSpecialization())) {
            throw new IllegalArgumentException("La especialización del profesor es obligatoria.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
