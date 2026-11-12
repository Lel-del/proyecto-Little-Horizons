package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a generated electronic invoice (simulated DIAN TXT).
 */
public class Invoice {
    private int invoiceId;
    private String invoiceNumber;
    private int paymentId;
    private LocalDate issueDate;
    private String cufe;
    private String fileName;
    private String filePath;

    public Invoice() {
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return String.format("Factura: %s | Pago ID: %d | Emisión: %s | Archivo: %s",
                invoiceNumber, paymentId, issueDate, filePath);
    }
}
