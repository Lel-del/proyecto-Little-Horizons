package com.littlehorizons.erp.service;

import com.littlehorizons.erp.util.AppLogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility service that exports report content to TXT files under the reports directory.
 */
public final class ReportExportService {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ReportExportService() {
    }

    public static Path exportReport(String reportName, String title, List<String> lines) {
        Path directory = Paths.get("reports");
        String fileName = reportName + "_" + LocalDateTime.now().format(FILE_STAMP) + ".txt";
        Path filePath = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write("===============================================================\n");
                writer.write("  " + title + "\n");
                writer.write("  ERP Colegio Internacional Little Horizons\n");
                writer.write("  Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n");
                writer.write("===============================================================\n\n");
                if (lines.isEmpty()) {
                    writer.write("No hay datos disponibles para este reporte.\n");
                } else {
                    for (String line : lines) {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    }
                }
                writer.write("\n===============================================================\n");
            }
        } catch (IOException ex) {
            AppLogger.getInstance().logError("ReportExportService.exportReport",
                    "No fue posible exportar el reporte " + reportName, ex);
            throw new IllegalStateException("No fue posible exportar el reporte: " + ex.getMessage(), ex);
        }
        AppLogger.getInstance().logInfo("ReportExportService.exportReport", "Reporte generado en " + filePath.toAbsolutePath());
        return filePath.toAbsolutePath();
    }
}
