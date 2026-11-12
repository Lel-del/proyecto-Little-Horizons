package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Console view for library and laboratory resource management.
 */
public class ResourceView {

    private final InputValidator input;
    private final ResourceController controller = new ResourceController();

    public ResourceView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("GESTIÓN DE RECURSOS");
            System.out.println("1. Registrar recurso");
            System.out.println("2. Listar recursos");
            System.out.println("3. Listar recursos por estado");
            System.out.println("4. Registrar categoría de recurso");
            System.out.println("5. Prestar recurso a estudiante");
            System.out.println("6. Prestar recurso a profesor");
            System.out.println("7. Registrar devolución");
            System.out.println("8. Reporte de préstamos vencidos");
            System.out.println("9. Listar préstamos activos");
            System.out.println("10. Cambiar estado de un recurso");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 10);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("ResourceView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("ResourceView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                registerResource();
                break;
            case 2:
                listResources();
                break;
            case 3:
                listResourcesByStatus();
                break;
            case 4:
                registerCategory();
                break;
            case 5:
                loanToStudent();
                break;
            case 6:
                loanToTeacher();
                break;
            case 7:
                registerReturn();
                break;
            case 8:
                overdueReport();
                break;
            case 9:
                listActiveLoans();
                break;
            case 10:
                changeStatus();
                break;
            default:
                break;
        }
    }

    private void registerResource() {
        ConsolePrinter.printSectionHeader("NUEVO RECURSO");
        List<ResourceCategory> categories = controller.listCategories();
        if (categories.isEmpty()) {
            ConsolePrinter.printWarning("Debe registrar al menos una categoría antes de crear recursos.");
            input.waitForEnter();
            return;
        }
        categories.forEach(System.out::println);
        Resource resource = new Resource();
        resource.setResourceCode(input.readNonEmptyString("Código único del recurso: "));
        resource.setResourceName(input.readNonEmptyString("Nombre del recurso: "));
        resource.setCategoryId(input.readPositiveInt("ID de la categoría: "));
        resource.setDescription(input.readOptionalString("Descripción: "));
        resource.setAcquisitionDate(input.readDate("Fecha de adquisición", false));
        resource.setPhysicalLocation(input.readOptionalString("Ubicación física: "));
        resource.setResourceStatus(input.readChoice("Estado inicial",
                Arrays.asList("Disponible", "Prestado", "En Mantenimiento", "Dado de Baja")));
        int id = controller.registerResource(resource);
        ConsolePrinter.printSuccess("Recurso registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listResources() {
        ConsolePrinter.printSectionHeader("INVENTARIO DE RECURSOS");
        List<Resource> resources = controller.listResources();
        if (resources.isEmpty()) {
            ConsolePrinter.printWarning("No hay recursos registrados.");
        } else {
            resources.forEach(System.out::println);
            ConsolePrinter.printInfo("Total: " + resources.size() + " recursos.");
        }
        input.waitForEnter();
    }

    private void listResourcesByStatus() {
        String status = input.readChoice("Estado",
                Arrays.asList("Disponible", "Prestado", "En Mantenimiento", "Dado de Baja"));
        List<Resource> resources = controller.listResourcesByStatus(status);
        ConsolePrinter.printSectionHeader("RECURSOS EN ESTADO " + status.toUpperCase());
        if (resources.isEmpty()) {
            ConsolePrinter.printWarning("No hay recursos en ese estado.");
        } else {
            resources.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void registerCategory() {
        ConsolePrinter.printSectionHeader("NUEVA CATEGORÍA DE RECURSO");
        ResourceCategory category = new ResourceCategory();
        category.setCategoryName(input.readNonEmptyString("Nombre de la categoría: "));
        category.setDescription(input.readOptionalString("Descripción: "));
        int id = controller.registerCategory(category);
        ConsolePrinter.printSuccess("Categoría registrada con ID " + id + ".");
        input.waitForEnter();
    }

    private void loanToStudent() {
        ConsolePrinter.printSectionHeader("PRÉSTAMO A ESTUDIANTE");
        listAvailableResources();
        int resourceId = input.readPositiveInt("ID del recurso: ");
        int studentId = input.readPositiveInt("ID del estudiante: ");
        LocalDate loanDate = input.readDate("Fecha de préstamo", false);
        if (loanDate == null) {
            loanDate = LocalDate.now();
        }
        LocalDate expected = input.readDate("Fecha de devolución esperada", true);
        String observations = input.readOptionalString("Observaciones (opcional): ");
        int id = controller.loanToStudent(resourceId, studentId, loanDate, expected, observations);
        ConsolePrinter.printSuccess("Préstamo registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void loanToTeacher() {
        ConsolePrinter.printSectionHeader("PRÉSTAMO A PROFESOR");
        listAvailableResources();
        int resourceId = input.readPositiveInt("ID del recurso: ");
        int teacherId = input.readPositiveInt("ID del profesor: ");
        LocalDate loanDate = input.readDate("Fecha de préstamo", false);
        if (loanDate == null) {
            loanDate = LocalDate.now();
        }
        LocalDate expected = input.readDate("Fecha de devolución esperada", true);
        String observations = input.readOptionalString("Observaciones (opcional): ");
        int id = controller.loanToTeacher(resourceId, teacherId, loanDate, expected, observations);
        ConsolePrinter.printSuccess("Préstamo registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void registerReturn() {
        ConsolePrinter.printSectionHeader("REGISTRO DE DEVOLUCIÓN");
        List<ResourceLoan> activeLoans = controller.listActiveLoans();
        if (activeLoans.isEmpty()) {
            ConsolePrinter.printWarning("No hay préstamos activos pendientes de devolución.");
            input.waitForEnter();
            return;
        }
        activeLoans.forEach(System.out::println);
        String reference = input.readNonEmptyString("Referencia del préstamo a devolver (ej: E-2 o P-2): ");
        ResourceLoan selected = activeLoans.stream()
                .filter(l -> l.getReference().equalsIgnoreCase(reference))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un préstamo activo con la referencia " + reference));
        LocalDate returnDate = input.readDate("Fecha real de devolución", false);
        controller.registerReturn(selected, returnDate);
        ConsolePrinter.printSuccess("Devolución registrada. El recurso vuelve a estar disponible.");
        input.waitForEnter();
    }

    private void overdueReport() {
        ConsolePrinter.printSectionHeader("REPORTE DE PRÉSTAMOS VENCIDOS");
        LocalDate reference = input.readDate("Fecha de referencia", false);
        List<ResourceLoan> overdue = controller.generateOverdueReport(reference);
        if (overdue.isEmpty()) {
            ConsolePrinter.printInfo("No hay préstamos vencidos a la fecha indicada.");
        } else {
            overdue.forEach(System.out::println);
            ConsolePrinter.printInfo("Total de préstamos vencidos: " + overdue.size());
        }
        input.waitForEnter();
    }

    private void listActiveLoans() {
        ConsolePrinter.printSectionHeader("PRÉSTAMOS ACTIVOS");
        List<ResourceLoan> loans = controller.listActiveLoans();
        if (loans.isEmpty()) {
            ConsolePrinter.printWarning("No hay préstamos activos.");
        } else {
            loans.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void changeStatus() {
        int resourceId = input.readPositiveInt("ID del recurso: ");
        String status = input.readChoice("Nuevo estado",
                Arrays.asList("Disponible", "Prestado", "En Mantenimiento", "Dado de Baja"));
        controller.changeResourceStatus(resourceId, status);
        ConsolePrinter.printSuccess("Estado del recurso actualizado.");
        input.waitForEnter();
    }

    private void listAvailableResources() {
        List<Resource> available = controller.listResourcesByStatus("Disponible");
        if (available.isEmpty()) {
            ConsolePrinter.printWarning("No hay recursos disponibles para prestar.");
        } else {
            available.forEach(System.out::println);
        }
    }
}
