package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.SubjectGrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Calculates and ranks subject averages across all recorded grades. */
public final class AcademicPerformanceService {
    public record SubjectAverage(SubjectGrade.Subject subject, BigDecimal average) {
    }

    public record Report(List<SubjectAverage> ranking, List<SubjectAverage> topSubjects) {
    }

    public Report calculate(List<SubjectGrade> grades) {
        Map<SubjectGrade.Subject, List<SubjectGrade>> gradesBySubject = grades.stream()
                .collect(Collectors.groupingBy(SubjectGrade::subject));

        List<SubjectAverage> ranking = gradesBySubject.entrySet().stream()
                .map(entry -> new SubjectAverage(entry.getKey(), average(entry.getValue())))
                .sorted(Comparator.comparing(SubjectAverage::average).reversed()
                        .thenComparing(average -> average.subject().code()))
                .toList();

        List<SubjectAverage> topSubjects = ranking.stream()
                .findFirst()
                .map(first -> ranking.stream()
                        .takeWhile(row -> row.average().compareTo(first.average()) == 0)
                        .toList())
                .orElseGet(List::of);

        return new Report(ranking, topSubjects);
    }

    private BigDecimal average(List<SubjectGrade> grades) {
        BigDecimal sum = grades.stream()
                .map(SubjectGrade::grade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(grades.size()), 1, RoundingMode.HALF_UP);
    }
}
