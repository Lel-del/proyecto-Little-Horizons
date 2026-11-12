package com.littlehorizons.erp.model;

/**
 * Domain entity representing an academic year grade enrollment for a student.
 */
public class Enrollment {
    private int enrollmentId;
    private int studentId;
    private int gradeId;
    private int academicYear;
    private String enrollmentStatus;
    private java.time.LocalDate enrollmentDate;
    private java.time.LocalDate withdrawalDate;
    private String observations;
    private String studentName;
    private String gradeName;

    public Enrollment() {
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public java.time.LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(java.time.LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public java.time.LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(java.time.LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    @Override
    public String toString() {
        return String.format("Matrícula #%d | Estudiante: %s (ID %d) | Grado: %s | Año: %d | Estado: %s",
                enrollmentId, studentName != null ? studentName : "-", studentId,
                gradeName != null ? gradeName : String.valueOf(gradeId), academicYear, enrollmentStatus);
    }
}
