package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a student's enrollment in a specific subject.
 */
public class SubjectEnrollment {
    private int subjectEnrollmentId;
    private int studentId;
    private int subjectId;
    private int academicYear;
    private String academicPeriod;
    private String section;
    private LocalDate enrollmentDate;
    private String status;
    private String studentName;
    private String subjectName;

    public SubjectEnrollment() {
    }

    public int getSubjectEnrollmentId() {
        return subjectEnrollmentId;
    }

    public void setSubjectEnrollmentId(int subjectEnrollmentId) {
        this.subjectEnrollmentId = subjectEnrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getAcademicPeriod() {
        return academicPeriod;
    }

    public void setAcademicPeriod(String academicPeriod) {
        this.academicPeriod = academicPeriod;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Est: %s | Materia: %s | %d Per.%s | Sec: %s | %s",
                subjectEnrollmentId,
                studentName != null ? studentName : String.valueOf(studentId),
                subjectName != null ? subjectName : String.valueOf(subjectId),
                academicYear, academicPeriod, section != null ? section : "-", status);
    }
}
