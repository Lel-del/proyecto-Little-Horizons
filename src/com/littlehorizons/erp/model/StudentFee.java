package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/**
 * Domain entity representing a student-specific fee with discounts or scholarships.
 */
public class StudentFee {
    private int feeId;
    private int studentId;
    private int conceptId;
    private BigDecimal customAmount;
    private BigDecimal discountPercentage;
    private String reason;
    private boolean active;
    private String studentName;
    private String conceptName;

    public StudentFee() {
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public BigDecimal getCustomAmount() {
        return customAmount;
    }

    public void setCustomAmount(BigDecimal customAmount) {
        this.customAmount = customAmount;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Est: %s | Concepto: %s | Valor: $%s | Descuento: %s%% | Motivo: %s",
                feeId,
                studentName != null ? studentName : String.valueOf(studentId),
                conceptName != null ? conceptName : String.valueOf(conceptId),
                customAmount, discountPercentage,
                reason != null ? reason : "-");
    }
}
