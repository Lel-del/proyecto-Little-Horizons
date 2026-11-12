package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utility class for validated console input reading with clear error messages and retry loops.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    ConsolePrinter.printError("Ingrese un número entre " + min + " y " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                ConsolePrinter.printError("Entrada inválida. Por favor ingrese un número entero.");
            }
        }
    }

    public int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    public BigDecimal readBigDecimal(String prompt, BigDecimal min, BigDecimal max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim().replace(",", ".");
            try {
                BigDecimal value = new BigDecimal(raw);
                if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                    ConsolePrinter.printError("El valor debe estar entre " + min + " y " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException ex) {
                ConsolePrinter.printError("Entrada inválida. Ingrese un valor numérico válido.");
            }
        }
    }

    public BigDecimal readGrade(String prompt) {
        BigDecimal value = readBigDecimal(prompt, new BigDecimal("0.0"), new BigDecimal("5.0"));
        return value.setScale(1, RoundingMode.HALF_UP);
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                ConsolePrinter.printError("Este campo es obligatorio y no puede estar vacío.");
                continue;
            }
            return raw;
        }
    }

    public String readOptionalString(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        return raw.isEmpty() ? null : raw;
    }

    public String readEmail(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                if (!required) {
                    return null;
                }
                ConsolePrinter.printError("El correo electrónico es obligatorio.");
                continue;
            }
            if (!EMAIL_PATTERN.matcher(raw).matches()) {
                ConsolePrinter.printError("Formato de correo electrónico inválido (ej: nombre@dominio.com).");
                continue;
            }
            return raw;
        }
    }

    public LocalDate readDate(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                if (!required) {
                    return null;
                }
                ConsolePrinter.printError("La fecha es obligatoria.");
                continue;
            }
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException ex) {
                ConsolePrinter.printError("Fecha inválida. Use el formato YYYY-MM-DD (ej: 2026-03-15).");
            }
        }
    }

    public LocalDate readDateWithDefault(String prompt, LocalDate current) {
        while (true) {
            System.out.print(prompt + " [" + current + "]: ");
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                return current;
            }
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException ex) {
                ConsolePrinter.printError("Fecha inválida. Use el formato YYYY-MM-DD.");
            }
        }
    }

    public String readStringWithDefault(String prompt, String current) {
        System.out.print(prompt + (current != null ? " [" + current + "]" : "") + ": ");
        String raw = scanner.nextLine().trim();
        return raw.isEmpty() ? current : raw;
    }

    public BigDecimal readBigDecimalWithDefault(String prompt, BigDecimal current) {
        while (true) {
            System.out.print(prompt + " [" + current + "]: ");
            String raw = scanner.nextLine().trim().replace(",", ".");
            if (raw.isEmpty()) {
                return current;
            }
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException ex) {
                ConsolePrinter.printError("Valor numérico inválido.");
            }
        }
    }

    public LocalTime readTime(String prompt, boolean required) {
        while (true) {
            System.out.print(prompt + " (HH:MM, 24h): ");
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                if (!required) {
                    return null;
                }
                ConsolePrinter.printError("La hora es obligatoria.");
                continue;
            }
            try {
                return LocalTime.parse(raw);
            } catch (DateTimeParseException ex) {
                ConsolePrinter.printError("Hora inválida. Use formato HH:MM en 24 horas (ej: 08:30).");
            }
        }
    }

    public String readChoice(String prompt, List<String> allowedValues) {
        while (true) {
            System.out.print(prompt + " " + allowedValues + ": ");
            String raw = scanner.nextLine().trim();
            for (String allowed : allowedValues) {
                if (allowed.equalsIgnoreCase(raw)) {
                    return allowed;
                }
            }
            ConsolePrinter.printError("Opción inválida. Valores permitidos: " + allowedValues);
        }
    }

    public boolean readBooleanYN(String prompt) {
        while (true) {
            System.out.print(prompt + " (S/N): ");
            String raw = scanner.nextLine().trim().toUpperCase();
            if ("S".equals(raw) || "SI".equals(raw) || "SÍ".equals(raw) || "Y".equals(raw)) {
                return true;
            }
            if ("N".equals(raw) || "NO".equals(raw)) {
                return false;
            }
            ConsolePrinter.printError("Responda con S (Sí) o N (No).");
        }
    }

    public void waitForEnter() {
        System.out.print("\nPresione ENTER para continuar...");
        scanner.nextLine();
    }
}
