package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain entity representing a payment made by a student or guardian.
 */
public class Payment {
    private int paymentId;
    private int studentId;
    private Integer guardianId;
    private int conceptId;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;
    private Integer academicYear;
    private String academicPeriod;
    private String notes;
    private String studentName;
    private String guardianName;
    private String conceptName;

    public Payment() {
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Integer getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(Integer guardianId) {
        this.guardianId = guardianId;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public String getAcademicPeriod() {
        return academicPeriod;
    }

    public void setAcademicPeriod(String academicPeriod) {
        this.academicPeriod = academicPeriod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    @Override
    public String toString() {
        return String.format("Recibo: %s | Est: %s | Concepto: %s | Monto: $%s | Fecha: %s | Método: %s",
                receiptNumber,
                studentName != null ? studentName : String.valueOf(studentId),
                conceptName != null ? conceptName : String.valueOf(conceptId),
                amountPaid, paymentDate, paymentMethod);
    }
}
