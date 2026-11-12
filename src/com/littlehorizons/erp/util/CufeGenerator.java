package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

/**
 * Generates simulated DIAN CUFE hashes and ASCII QR blocks for electronic invoices.
 */
public final class CufeGenerator {

    private CufeGenerator() {
    }

    public static String generateCufe(String invoiceNumber, String schoolNit, LocalDate issueDate, BigDecimal total) {
        String seed = invoiceNumber + "|" + schoolNit + "|" + issueDate + "|" + total.toPlainString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            AppLogger.getInstance().logError("CufeGenerator", "SHA-256 algorithm unavailable", ex);
            return Integer.toHexString(seed.hashCode()).toUpperCase();
        }
    }

    public static String generateSimulatedQrBlock(String invoiceNumber, String schoolNit, String cufe, BigDecimal total) {
        String shortCufe = cufe.length() > 24 ? cufe.substring(0, 24) + "..." : cufe;
        return new StringBuilder()
                .append("  +--------------------------------------+\n")
                .append("  |  [##  # ## #]  QR DIAN SIMULADO      |\n")
                .append("  |  [# ##  #  #]  Fac: ").append(padRight(invoiceNumber, 17)).append("|\n")
                .append("  |  [### # ## #]  NIT: ").append(padRight(schoolNit, 17)).append("|\n")
                .append("  |  [#  ###  ##]  Tot: $").append(padRight(total.toPlainString(), 16)).append("|\n")
                .append("  |  [## # ### #]  CUFE: ").append(padRight(shortCufe, 16)).append("|\n")
                .append("  +--------------------------------------+")
                .toString();
    }

    private static String padRight(String value, int width) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return String.format("%-" + width + "s", value);
    }
}
