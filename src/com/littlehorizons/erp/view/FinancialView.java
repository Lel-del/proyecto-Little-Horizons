package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinancialController;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.StudentFee;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsolePrinter;
import com.littlehorizons.erp.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Console view for the financial module: concepts, payments, invoices and delinquency.
 */
public class FinancialView {

    private final InputValidator input;
    private final FinancialController controller = new FinancialController();

    public FinancialView(InputValidator input) {
        this.input = input;
    }

    public void run() {
        int option;
        do {
            ConsolePrinter.printSectionHeader("GESTIÓN FINANCIERA");
            System.out.println("1. Registrar concepto de pago");
            System.out.println("2. Listar conceptos de pago");
            System.out.println("3. Personalizar tarifa de estudiante");
            System.out.println("4. Registrar pago y generar factura electrónica");
            System.out.println("5. Historial de pagos por estudiante");
            System.out.println("6. Historial de pagos por acudiente");
            System.out.println("7. Reporte de estudiantes morosos");
            System.out.println("8. Listar facturas generadas");
            System.out.println("9. Consultar monto personalizado de un estudiante");
            System.out.println("0. Volver al menú principal");
            option = input.readInt("Seleccione una opción: ", 0, 9);
            try {
                handle(option);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                ConsolePrinter.printError(ex.getMessage());
                AppLogger.getInstance().logWarning("FinancialView", ex.getMessage());
            } catch (RuntimeException ex) {
                ConsolePrinter.printError("Error inesperado al procesar la solicitud.");
                AppLogger.getInstance().logError("FinancialView", "Error inesperado", ex);
            }
        } while (option != 0);
    }

    private void handle(int option) {
        switch (option) {
            case 1:
                registerConcept();
                break;
            case 2:
                listConcepts();
                break;
            case 3:
                registerFee();
                break;
            case 4:
                registerPayment();
                break;
            case 5:
                paymentsByStudent();
                break;
            case 6:
                paymentsByGuardian();
                break;
            case 7:
                delinquencyReport();
                break;
            case 8:
                listInvoices();
                break;
            case 9:
                consultStudentAmount();
                break;
            default:
                break;
        }
    }

    private void registerConcept() {
        ConsolePrinter.printSectionHeader("NUEVO CONCEPTO DE PAGO");
        PaymentConcept concept = new PaymentConcept();
        concept.setConceptCode(input.readNonEmptyString("Código del concepto: "));
        concept.setConceptName(input.readNonEmptyString("Nombre del concepto: "));
        concept.setDescription(input.readOptionalString("Descripción: "));
        concept.setBaseAmount(input.readBigDecimal("Monto base en pesos: ",
                new BigDecimal("1"), new BigDecimal("99999999")));
        concept.setRecurring(input.readBooleanYN("¿Es un concepto recurrente?"));
        concept.setActive(true);
        int id = controller.registerConcept(concept);
        ConsolePrinter.printSuccess("Concepto registrado con ID " + id + ".");
        input.waitForEnter();
    }

    private void listConcepts() {
        ConsolePrinter.printSectionHeader("CATÁLOGO DE CONCEPTOS DE PAGO");
        List<PaymentConcept> concepts = controller.listConcepts();
        if (concepts.isEmpty()) {
            ConsolePrinter.printWarning("No hay conceptos registrados.");
        } else {
            concepts.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void registerFee() {
        ConsolePrinter.printSectionHeader("TARIFA PERSONALIZADA");
        int studentId = input.readPositiveInt("ID del estudiante: ");
        printConceptCatalog();
        int conceptId = input.readPositiveInt("ID del concepto: ");
        BigDecimal amount = input.readBigDecimal("Monto personalizado: ",
                BigDecimal.ZERO, new BigDecimal("99999999"));
        BigDecimal discount = input.readBigDecimal("Porcentaje de descuento (0-100): ",
                BigDecimal.ZERO, new BigDecimal("100"));
        String reason = input.readNonEmptyString("Motivo (ej: Descuento por hermanos): ");
        controller.registerStudentFee(studentId, conceptId, amount, discount, reason);
        ConsolePrinter.printSuccess("Tarifa personalizada registrada correctamente.");
        input.waitForEnter();
    }

    private void registerPayment() {
        ConsolePrinter.printSectionHeader("REGISTRO DE PAGO");
        Payment payment = new Payment();
        payment.setStudentId(input.readPositiveInt("ID del estudiante: "));
        if (input.readBooleanYN("¿Registrar acudiente responsable del pago?")) {
            payment.setGuardianId(input.readPositiveInt("ID del acudiente: "));
        }
        printConceptCatalog();
        payment.setConceptId(input.readPositiveInt("ID del concepto: "));
        BigDecimal suggested = controller.resolveAmountForStudent(payment.getStudentId(), payment.getConceptId());
        ConsolePrinter.printInfo("Monto sugerido para este estudiante: $" + suggested);
        payment.setAmountPaid(input.readBigDecimal("Monto pagado: ",
                new BigDecimal("1"), new BigDecimal("99999999")));
        payment.setPaymentDate(input.readDate("Fecha de pago", false));
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }
        payment.setPaymentMethod(input.readChoice("Método de pago",
                Arrays.asList("Efectivo", "Transferencia", "Tarjeta Débito", "Tarjeta Crédito", "Cheque")));
        String suggestedReceipt = controller.suggestNextReceiptNumber();
        payment.setReceiptNumber(input.readStringWithDefault("Número de recibo", suggestedReceipt));
        payment.setAcademicYear(input.readInt("Año académico: ", 2000, 2100));
        payment.setAcademicPeriod(input.readChoice("Período", Arrays.asList("I", "II", "III", "IV", "Anual")));
        payment.setNotes(input.readOptionalString("Notas (opcional): "));
        Invoice invoice = controller.registerPayment(payment);
        ConsolePrinter.printSuccess("Pago registrado. Factura " + invoice.getInvoiceNumber() + " generada.");
        ConsolePrinter.printInfo("CUFE simulado: " + invoice.getCufe());
        ConsolePrinter.printInfo("Archivo: " + invoice.getFilePath());
        input.waitForEnter();
    }

    private void paymentsByStudent() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        ConsolePrinter.printSectionHeader("HISTORIAL DE PAGOS DEL ESTUDIANTE");
        List<Payment> payments = controller.listPaymentsByStudent(studentId);
        if (payments.isEmpty()) {
            ConsolePrinter.printWarning("El estudiante no tiene pagos registrados.");
        } else {
            payments.forEach(System.out::println);
            ConsolePrinter.printInfo("Total pagado: $" + controller.totalPaidByStudent(studentId));
        }
        input.waitForEnter();
    }

    private void paymentsByGuardian() {
        int guardianId = input.readPositiveInt("ID del acudiente: ");
        ConsolePrinter.printSectionHeader("HISTORIAL DE PAGOS DEL ACUDIENTE");
        List<Payment> payments = controller.listPaymentsByGuardian(guardianId);
        if (payments.isEmpty()) {
            ConsolePrinter.printWarning("El acudiente no tiene pagos registrados.");
        } else {
            payments.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void delinquencyReport() {
        ConsolePrinter.printSectionHeader("REPORTE DE ESTUDIANTES MOROSOS");
        printConceptCatalog();
        int conceptId = input.readPositiveInt("ID del concepto recurrente (ej: pensión): ");
        LocalDate reference = input.readDate("Fecha de referencia", false);
        List<String> report = controller.generateDelinquencyReport(conceptId, reference);
        if (report.isEmpty()) {
            ConsolePrinter.printInfo("No se encontraron estudiantes en mora con ese criterio.");
        } else {
            report.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void listInvoices() {
        ConsolePrinter.printSectionHeader("FACTURAS GENERADAS");
        List<Invoice> invoices = controller.listInvoices();
        if (invoices.isEmpty()) {
            ConsolePrinter.printWarning("No se han generado facturas.");
        } else {
            invoices.forEach(System.out::println);
        }
        input.waitForEnter();
    }

    private void printConceptCatalog() {
        ConsolePrinter.printInfo("Conceptos de pago disponibles:");
        controller.listConcepts().forEach(System.out::println);
    }

    private void consultStudentAmount() {
        int studentId = input.readPositiveInt("ID del estudiante: ");
        printConceptCatalog();
        int conceptId = input.readPositiveInt("ID del concepto: ");
        BigDecimal amount = controller.resolveAmountForStudent(studentId, conceptId);
        ConsolePrinter.printInfo("Monto efectivo para el estudiante: $" + amount);
        List<StudentFee> fees = controller.listStudentFees(studentId);
        if (!fees.isEmpty()) {
            ConsolePrinter.printInfo("Tarifas personalizadas del estudiante:");
            fees.forEach(System.out::println);
        }
        input.waitForEnter();
    }
}
