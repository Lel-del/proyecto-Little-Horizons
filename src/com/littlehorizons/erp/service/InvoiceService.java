package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.InvoiceDAO;
import com.littlehorizons.erp.dao.PaymentDAO;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.CufeGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates simulated DIAN electronic invoices as formatted TXT files.
 */
public class InvoiceService {

    public static final String SCHOOL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String SCHOOL_NIT = "901.456.789-3";
    public static final String SCHOOL_ADDRESS = "Calle 127 #54-20, Bogotá D.C., Colombia";
    public static final String SCHOOL_PHONE = "+57 (601) 745-8899";
    public static final String SCHOOL_EMAIL = "facturacion@littlehorizons.edu.co";
    public static final String RESOLUTION =
            "Resolución DIAN simulada No. 18764 de 2025 - Autorización de facturación electrónica 000001 a 999999";

    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter STAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    /**
     * Generates the invoice TXT for a payment and persists its metadata.
     *
     * @return the generated invoice metadata
     */
    public Invoice generateInvoiceForPayment(int paymentId) {
        Payment payment = paymentDAO.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el pago con ID " + paymentId));
        String invoiceNumber = invoiceDAO.nextInvoiceNumber();
        LocalDate issueDate = LocalDate.now();
        BigDecimal subtotal = payment.getAmountPaid().setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotal.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva);

        String cufe = CufeGenerator.generateCufe(invoiceNumber, SCHOOL_NIT, issueDate, total);
        String fileName = "factura_" + invoiceNumber.replace("FAC-", "") + ".txt";
        Path invoiceDirectory = Paths.get("invoices");
        Path filePath = invoiceDirectory.resolve(fileName);

        try {
            Files.createDirectories(invoiceDirectory);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(buildInvoiceContent(payment, invoiceNumber, issueDate, subtotal, iva, total, cufe));
            }
        } catch (IOException ex) {
            AppLogger.getInstance().logError("InvoiceService.generateInvoiceForPayment",
                    "No fue posible escribir la factura " + filePath, ex);
            throw new IllegalStateException("No fue posible generar el archivo de la factura: " + ex.getMessage(), ex);
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPaymentId(paymentId);
        invoice.setIssueDate(issueDate);
        invoice.setCufe(cufe);
        invoice.setFileName(fileName);
        invoice.setFilePath(filePath.toAbsolutePath().toString());
        invoiceDAO.insert(invoice);
        AppLogger.getInstance().logInfo("InvoiceService.generateInvoiceForPayment",
                "Factura " + invoiceNumber + " generada en " + filePath.toAbsolutePath());
        return invoice;
    }

    private String buildInvoiceContent(Payment payment, String invoiceNumber, LocalDate issueDate,
                                       BigDecimal subtotal, BigDecimal iva, BigDecimal total, String cufe) {
        StringBuilder sb = new StringBuilder();
        String separator = "================================================================================";
        sb.append(separator).append('\n');
        sb.append("                    FACTURA ELECTRÓNICA DE VENTA (SIMULADA)\n");
        sb.append(separator).append('\n');
        sb.append(String.format("Razón social : %s%n", SCHOOL_NAME));
        sb.append(String.format("NIT          : %s%n", SCHOOL_NIT));
        sb.append(String.format("Dirección    : %s%n", SCHOOL_ADDRESS));
        sb.append(String.format("Teléfono     : %s%n", SCHOOL_PHONE));
        sb.append(String.format("Correo       : %s%n", SCHOOL_EMAIL));
        sb.append(String.format("%s%n", RESOLUTION));
        sb.append(separator).append('\n');
        sb.append("INFORMACIÓN DEL CLIENTE\n");
        sb.append(separator).append('\n');
        sb.append(String.format("Acudiente responsable : %s%n",
                payment.getGuardianName() != null ? payment.getGuardianName() : "No registrado"));
        sb.append(String.format("Estudiante            : %s%n",
                payment.getStudentName() != null ? payment.getStudentName() : "No registrado"));
        sb.append(String.format("Documento / Dirección : Ver registro de acudiente asociado al estudiante ID %d%n",
                payment.getStudentId()));
        sb.append(separator).append('\n');
        sb.append("DETALLE DE LA FACTURA\n");
        sb.append(separator).append('\n');
        sb.append(String.format("Número de factura : %s%n", invoiceNumber));
        sb.append(String.format("Fecha de emisión  : %s%n", issueDate.format(DATE_FORMAT)));
        sb.append(String.format("Recibo de pago    : %s%n",
                payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "N/A"));
        sb.append(String.format("Método de pago    : %s%n", payment.getPaymentMethod()));
        sb.append('\n');
        sb.append(String.format("%-45s %8s %15s %15s%n", "DESCRIPCIÓN", "CANT.", "VALOR UNIT.", "SUBTOTAL"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-45s %8d %15s %15s%n",
                payment.getConceptName() != null ? payment.getConceptName() : "Concepto de pago",
                1,
                formatMoney(subtotal),
                formatMoney(subtotal)));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%58s %15s%n", "SUBTOTAL:", formatMoney(subtotal)));
        sb.append(String.format("%58s %15s%n", "IVA (19%):", formatMoney(iva)));
        sb.append(String.format("%58s %15s%n", "TOTAL:", formatMoney(total)));
        sb.append(separator).append('\n');
        sb.append("CUMPLIMIENTO DIAN (SIMULADO)\n");
        sb.append(separator).append('\n');
        sb.append(String.format("CUFE: %s%n", cufe));
        sb.append('\n');
        sb.append(CufeGenerator.generateSimulatedQrBlock(invoiceNumber, SCHOOL_NIT, cufe, total)).append('\n');
        sb.append('\n');
        sb.append(String.format("Documento generado el %s%n", LocalDate.now().atStartOfDay().format(STAMP_FORMAT)));
        sb.append("Este documento es una simulación académica y no tiene validez fiscal ante la DIAN.\n");
        sb.append(separator).append('\n');
        return sb.toString();
    }

    private String formatMoney(BigDecimal value) {
        return String.format("$%,.2f", value);
    }
}
