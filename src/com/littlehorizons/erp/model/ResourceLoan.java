package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a resource loan (either to a student or a teacher).
 */
public class ResourceLoan {
    private int loanId;
    private int resourceId;
    private Integer studentId;
    private Integer teacherId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String loanStatus;
    private String observations;
    private String resourceName;
    private String borrowerName;
    private String borrowerType;

    public ResourceLoan() {
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(String loanStatus) {
        this.loanStatus = loanStatus;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerType() {
        return borrowerType;
    }

    public void setBorrowerType(String borrowerType) {
        this.borrowerType = borrowerType;
    }

    /**
     * Unique reference across the two loan tables, since loan identifiers only
     * repeat within each table. Students use E-N and teachers use P-N.
     */
    public String getReference() {
        String prefix = "Profesor".equalsIgnoreCase(borrowerType) ? "P-" : "E-";
        return prefix + loanId;
    }

    @Override
    public String toString() {
        return String.format("Ref: %-6s | Recurso: %s | %s: %s | Préstamo: %s | Devolución esperada: %s | Estado: %s",
                getReference(),
                resourceName != null ? resourceName : String.valueOf(resourceId),
                borrowerType, borrowerName != null ? borrowerName : "-",
                loanDate, expectedReturnDate, loanStatus);
    }
}
