package com.littlehorizons.erp;

import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.view.MainView;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Application entry point for the Little Horizons ERP console system.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        configureUtf8Console();
        AppLogger.getInstance().logInfo("Main", "Inicio de la aplicación ERP Little Horizons");
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new MainView(scanner).start();
        } catch (RuntimeException ex) {
            ConsolePrinter.printError("Error crítico de la aplicación. Consulte error_log.txt");
            AppLogger.getInstance().logError("Main.main", "Error crítico no controlado", ex);
        } finally {
            AppLogger.getInstance().logInfo("Main", "Cierre de la aplicación ERP Little Horizons");
        }
    }

    /**
     * Forces UTF-8 output so accents and box-drawing characters render consistently.
     */
    private static void configureUtf8Console() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        } catch (RuntimeException ex) {
            System.err.println("No fue posible configurar la salida UTF-8: " + ex.getMessage());
        }
    }
}
