package com.littlehorizons.erp.model;

import java.time.LocalTime;

/**
 * Domain entity representing a teacher's subject assignment and class schedule.
 */
public class TeacherAssignment {
    private int assignmentId;
    private int teacherId;
    private int subjectId;
    private int academicYear;
    private String section;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String teacherName;
    private String subjectName;

    public TeacherAssignment() {
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Prof: %s | Materia: %s | Año: %d Sec: %s | %s %s-%s",
                assignmentId,
                teacherName != null ? teacherName : String.valueOf(teacherId),
                subjectName != null ? subjectName : String.valueOf(subjectId),
                academicYear,
                section != null ? section : "-",
                dayOfWeek != null ? dayOfWeek : "Sin día",
                startTime != null ? startTime.toString() : "--:--",
                endTime != null ? endTime.toString() : "--:--");
    }
}
