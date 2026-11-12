package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/**
 * Domain entity representing a billable payment concept (tuition, uniform, etc.).
 */
public class PaymentConcept {
    private int conceptId;
    private String conceptCode;
    private String conceptName;
    private String description;
    private BigDecimal baseAmount;
    private boolean recurring;
    private boolean active;

    public PaymentConcept() {
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s - %s | Valor base: $%s | Recurrente: %s | Activo: %s",
                conceptId, conceptCode, conceptName, baseAmount,
                recurring ? "Sí" : "No", active ? "Sí" : "No");
    }
}
