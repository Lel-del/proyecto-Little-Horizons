package com.littlehorizons.erp.util;

/**
 * Utility class for standardized console formatting, banners, and status messages.
 */
public final class ConsolePrinter {

    private ConsolePrinter() {
    }

    public static void printMainBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   ERP Colegio Little Horizons               ║");
        System.out.println("║   Sistema de Gestión Educativa              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    public static void printSectionHeader(String title) {
        System.out.println();
        System.out.println("================================================");
        System.out.println("  " + title);
        System.out.println("================================================");
    }

    public static void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("[AVISO] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void printDivider() {
        System.out.println("------------------------------------------------");
    }
}
