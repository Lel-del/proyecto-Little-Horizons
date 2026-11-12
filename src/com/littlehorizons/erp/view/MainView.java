package com.littlehorizons.erp.view;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.util.Scanner;

/**
 * Main console view that groups the ERP options by module.
 */
public class MainView {

    private final InputValidator input;
    private final StudentView studentView;
    private final AcademicView academicView;
    private final TeacherView teacherView;
    private final ExtracurricularView extracurricularView;
    private final FinancialView financialView;
    private final ResourceView resourceView;
    private final ReportView reportView;

    public MainView(Scanner scanner) {
        this.input = new InputValidator(scanner);
        this.studentView = new StudentView(input);
        this.academicView = new AcademicView(input);
        this.teacherView = new TeacherView(input);
        this.extracurricularView = new ExtracurricularView(input);
        this.financialView = new FinancialView(input);
        this.resourceView = new ResourceView(input);
        this.reportView = new ReportView(input);
    }

    public void start() {
        verifyDatabase();
        int option;
        do {
            ConsolePrinter.printMainBanner();
            System.out.println("1. Gestión de Estudiantes");
            System.out.println("2. Gestión Académica");
            System.out.println("3. Gestión de Profesores");
            System.out.println("4. Actividades Extracurriculares");
            System.out.println("5. Gestión Financiera");
            System.out.println("6. Gestión de Recursos");
            System.out.println("7. Reportes y Estadísticas");
            System.out.println("8. Salir");
            option = input.readInt("Seleccione una opción: ", 1, 8);
            try {
                switch (option) {
                    case 1:
                        studentView.run();
                        break;
                    case 2:
                        academicView.run();
                        break;
                    case 3:
                        teacherView.run();
                        break;
                    case 4:
                        extracurricularView.run();
                        break;
                    case 5:
                        financialView.run();
                        break;
                    case 6:
                        resourceView.run();
                        break;
                    case 7:
                        reportView.run();
                        break;
                    case 8:
                        ConsolePrinter.printInfo("Gracias por usar el ERP de Little Horizons. ¡Hasta pronto!");
                        break;
                    default:
                        break;
                }
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Ocurrió un error inesperado. El detalle quedó registrado en error_log.txt");
                AppLogger.getInstance().logError("MainView.start", "Error no controlado en el menú principal", ex);
            }
        } while (option != 8);
    }

    private void verifyDatabase() {
        ConsolePrinter.printInfo("Verificando conexión con la base de datos...");
        if (DatabaseConnection.getInstance().testConnection()) {
            ConsolePrinter.printSuccess("Conexión a MySQL establecida correctamente.");
        } else {
            ConsolePrinter.printError("No fue posible conectar con la base de datos.");
            ConsolePrinter.printWarning("Revise config/db.properties y que MySQL 8.0+ esté en ejecución.");
            AppLogger.getInstance().logError("MainView.verifyDatabase",
                    "No se pudo establecer conexión inicial con la base de datos", null);
        }
    }
}
