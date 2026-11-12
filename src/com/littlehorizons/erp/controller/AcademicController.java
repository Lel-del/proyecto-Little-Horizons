package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDAO;
import com.littlehorizons.erp.dao.AttendanceDAO;
import com.littlehorizons.erp.dao.GradeDAO;
import com.littlehorizons.erp.dao.SubjectDAO;
import com.littlehorizons.erp.dao.SubjectEnrollmentDAO;
import com.littlehorizons.erp.dao.TeacherAssignmentDAO;
import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.AppLogger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business logic controller for academic management: subjects, grades, attendance and reports.
 */
public class AcademicController {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final GradeDAO gradeDAO = new GradeDAO();
    private final AcademicRecordDAO recordDAO = new AcademicRecordDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SubjectEnrollmentDAO subjectEnrollmentDAO = new SubjectEnrollmentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final TeacherAssignmentDAO assignmentDAO = new TeacherAssignmentDAO();

    public int registerSubject(Subject subject) {
        validateSubject(subject);
        if (subjectDAO.existsCode(subject.getSubjectCode())) {
            throw new IllegalArgumentException("Ya existe una materia con el código " + subject.getSubjectCode());
        }
        int id = subjectDAO.insert(subject);
        AppLogger.getInstance().logInfo("AcademicController.registerSubject", "Materia registrada ID " + id);
        return id;
    }

    public boolean updateSubject(Subject subject) {
        validateSubject(subject);
        if (subject.getSubjectId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una materia válida para actualizar.");
        }
        return subjectDAO.update(subject);
    }

    public boolean deleteSubject(int subjectId) {
        return subjectDAO.delete(subjectId);
    }

    public Subject findSubject(int subjectId) {
        return subjectDAO.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la materia con ID " + subjectId));
    }

    public List<Subject> listSubjects() {
        return subjectDAO.findAll();
    }

    public List<Subject> listActiveSubjects() {
        return subjectDAO.findActive();
    }

    public List<Subject> listSubjectsByGrade(int gradeId) {
        return subjectDAO.findByGrade(gradeId);
    }

    public List<Grade> listGrades() {
        return gradeDAO.findAll();
    }

    public int registerGradeRecord(AcademicRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Los datos de la calificación son obligatorios.");
        }
        if (record.getGradeValue() == null) {
            throw new IllegalArgumentException("La nota es obligatoria.");
        }
        if (record.getGradeValue().compareTo(BigDecimal.ZERO) < 0
                || record.getGradeValue().compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("La nota debe estar en el rango de 0.0 a 5.0.");
        }
        if (record.getGradeValue().scale() > 1) {
            throw new IllegalArgumentException("La nota debe tener máximo un decimal (ej: 4.5).");
        }
        if (record.getAcademicYear() < 2000 || record.getAcademicYear() > 2100) {
            throw new IllegalArgumentException("El año académico debe estar entre 2000 y 2100.");
        }
        teacherDAO.findById(record.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el profesor con ID " + record.getTeacherId()));
        subjectDAO.findById(record.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("No existe la materia con ID " + record.getSubjectId()));
        if (recordDAO.exists(record.getStudentId(), record.getSubjectId(),
                record.getAcademicYear(), record.getAcademicPeriod())) {
            throw new IllegalStateException("Ya existe una nota registrada para ese estudiante, materia y período.");
        }
        if (!subjectEnrollmentDAO.existsForYear(record.getStudentId(), record.getSubjectId(), record.getAcademicYear())) {
            throw new IllegalStateException("El estudiante no está inscrito en la materia para el año indicado.");
        }
        boolean teachesSubject = assignmentDAO.findByTeacher(record.getTeacherId()).stream()
                .anyMatch(a -> a.getSubjectId() == record.getSubjectId());
        if (!teachesSubject) {
            throw new IllegalStateException("El profesor indicado no tiene asignada la materia seleccionada.");
        }
        int id = recordDAO.insert(record);
        AppLogger.getInstance().logInfo("AcademicController.registerGradeRecord", "Calificación registrada ID " + id);
        return id;
    }

    public boolean updateGradeRecord(AcademicRecord record) {
        if (record.getGradeValue() == null
                || record.getGradeValue().compareTo(BigDecimal.ZERO) < 0
                || record.getGradeValue().compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("La nota debe estar en el rango de 0.0 a 5.0.");
        }
        if (record.getGradeValue().scale() > 1) {
            throw new IllegalArgumentException("La nota debe tener máximo un decimal.");
        }
        return recordDAO.update(record);
    }

    public boolean deleteGradeRecord(int recordId) {
        return recordDAO.delete(recordId);
    }

    public List<AcademicRecord> listRecordsByStudent(int studentId, int academicYear) {
        return recordDAO.findByStudent(studentId, academicYear);
    }

    public List<AcademicRecord> listStudentHistory(int studentId) {
        return recordDAO.findHistory(studentId);
    }

    public List<AcademicRecord> listRecordsBySubjectAndPeriod(int subjectId, int academicYear, String period) {
        return recordDAO.findBySubjectAndPeriod(subjectId, academicYear, period);
    }

    /**
     * Average per subject, general period average and course average for each subject.
     */
    public Map<String, BigDecimal> calculateStudentAverages(int studentId, int academicYear, String period) {
        List<AcademicRecord> records = recordDAO.findByStudent(studentId, academicYear);
        Map<Integer, String> subjectNames = new java.util.LinkedHashMap<>();
        records.stream()
                .filter(r -> r.getAcademicPeriod().equals(period))
                .forEach(r -> subjectNames.putIfAbsent(r.getSubjectId(), r.getSubjectName()));

        Map<String, BigDecimal> averages = new java.util.LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : subjectNames.entrySet()) {
            BigDecimal subjectAverage = recordDAO.calculateStudentSubjectAverage(studentId, entry.getKey(), academicYear);
            BigDecimal courseAverage = recordDAO.calculateCourseSubjectAverage(entry.getKey(), academicYear, period);
            averages.put(entry.getValue() + " (Estudiante)",
                    subjectAverage != null ? subjectAverage : BigDecimal.ZERO);
            averages.put(entry.getValue() + " (Curso)",
                    courseAverage != null ? courseAverage : BigDecimal.ZERO);
        }
        BigDecimal general = recordDAO.calculateStudentPeriodAverage(studentId, academicYear, period);
        averages.put("Promedio general del período", general != null ? general : BigDecimal.ZERO);
        return averages;
    }

    public int registerAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Los datos de asistencia son obligatorios.");
        }
        if (attendance.getAttendanceDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede registrar asistencia para fechas futuras.");
        }
        if (attendanceDAO.exists(attendance.getStudentId(), attendance.getSubjectId(), attendance.getAttendanceDate())) {
            throw new IllegalStateException("Ya existe un registro de asistencia para ese estudiante, materia y fecha.");
        }
        int id = attendanceDAO.insert(attendance);
        AppLogger.getInstance().logInfo("AcademicController.registerAttendance", "Asistencia registrada ID " + id);
        return id;
    }

    public List<Attendance> listAttendanceByStudent(int studentId) {
        return attendanceDAO.findByStudent(studentId);
    }

    public List<Attendance> listAttendanceByDateAndSubject(LocalDate date, int subjectId) {
        return attendanceDAO.findByDateAndSubject(date, subjectId);
    }

    public List<String> generateAbsenceReport(int minAbsences, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return attendanceDAO.findStudentsWithExcessiveAbsences(minAbsences, from, to);
    }

    public List<String> generateLowPerformanceReport(BigDecimal threshold, int academicYear, String period) {
        if (threshold.compareTo(BigDecimal.ZERO) < 0 || threshold.compareTo(new BigDecimal("5.0")) > 0) {
            throw new IllegalArgumentException("El umbral de nota debe estar entre 0.0 y 5.0.");
        }
        return recordDAO.findLowPerformingStudents(threshold, academicYear, period);
    }

    public List<TeacherAssignment> listTeacherAssignments() {
        return assignmentDAO.findAll();
    }

    private void validateSubject(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Los datos de la materia son obligatorios.");
        }
        if (subject.getSubjectCode() == null || subject.getSubjectCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la materia es obligatorio.");
        }
        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la materia es obligatorio.");
        }
        if (subject.getWeeklyHours() <= 0) {
            throw new IllegalArgumentException("Las horas semanales deben ser mayores que cero.");
        }
        gradeDAO.findById(subject.getGradeId())
                .orElseThrow(() -> new IllegalArgumentException("El grado asociado a la materia no existe."));
    }
}
