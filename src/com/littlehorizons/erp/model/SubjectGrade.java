package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Grade and subject data used by the academic performance report. */
public record SubjectGrade(int recordId, BigDecimal grade, String period, Subject subject) {
    public record Subject(int id, String code, String name) {
    }
}
