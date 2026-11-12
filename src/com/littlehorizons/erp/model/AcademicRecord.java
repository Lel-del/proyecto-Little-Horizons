package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain entity representing an academic grade record (0.0 to 5.0).
 */
public class AcademicRecord {
    private int recordId;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private String academicPeriod;
    private BigDecimal gradeValue;
    private String observations;
    private LocalDate recordDate;
    private String studentName;
    private String subjectName;
    private String teacherName;

    public AcademicRecord() {
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
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

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    public BigDecimal getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(BigDecimal gradeValue) {
        this.gradeValue = gradeValue;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Est: %s | Materia: %s | Año %d Per.%s | Nota: %s | Prof: %s",
                recordId,
                studentName != null ? studentName : String.valueOf(studentId),
                subjectName != null ? subjectName : String.valueOf(subjectId),
                academicYear, academicPeriod, gradeValue,
                teacherName != null ? teacherName : String.valueOf(teacherId));
    }
}
