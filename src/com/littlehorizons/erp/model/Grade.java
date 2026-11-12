package com.littlehorizons.erp.model;

/**
 * Represents an academic grade level (e.g., Preescolar through Once).
 */
public class Grade {
    private int gradeId;
    private String gradeName;
    private int numericLevel;

    public Grade() {
    }

    public Grade(int gradeId, String gradeName, int numericLevel) {
        this.gradeId = gradeId;
        this.gradeName = gradeName;
        this.numericLevel = numericLevel;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public int getNumericLevel() {
        return numericLevel;
    }

    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (Nivel %d)", gradeId, gradeName, numericLevel);
    }
}
