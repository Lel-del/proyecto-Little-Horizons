package com.littlehorizons.erp.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton logger responsible for recording application errors and key events to error_log.txt.
 */
public final class AppLogger {

    private static final String LOG_FILE_PATH = "error_log.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static AppLogger instance;

    private AppLogger() {
    }

    public static synchronized AppLogger getInstance() {
        if (instance == null) {
            instance = new AppLogger();
        }
        return instance;
    }

    public synchronized void logInfo(String context, String message) {
        writeEntry("INFO", context, message, null);
    }

    public synchronized void logWarning(String context, String message) {
        writeEntry("WARN", context, message, null);
    }

    public synchronized void logError(String context, String message, Throwable throwable) {
        writeEntry("ERROR", context, message, throwable);
    }

    private void writeEntry(String level, String context, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        Path logPath = Paths.get(LOG_FILE_PATH);
        try (BufferedWriter bw = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
             PrintWriter out = new PrintWriter(bw)) {
            out.printf("[%s] [%s] [%s] %s%n", timestamp, level, context, message);
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                out.println(sw.toString());
            }
        } catch (IOException ioEx) {
            System.err.println("Critical logging failure: unable to write to " + LOG_FILE_PATH + " - " + ioEx.getMessage());
        }
    }
}
