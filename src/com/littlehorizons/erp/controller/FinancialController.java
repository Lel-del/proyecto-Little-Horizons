package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDAO;
import com.littlehorizons.erp.dao.InvoiceDAO;
import com.littlehorizons.erp.dao.PaymentConceptDAO;
import com.littlehorizons.erp.dao.PaymentDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.dao.StudentFeeDAO;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentFee;
import com.littlehorizons.erp.service.PaymentService;
import com.littlehorizons.erp.util.AppLogger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic controller for payments, fee customization and electronic invoicing.
 */
public class FinancialController {

    private static final List<String> VALID_PAYMENT_METHODS =
            Arrays.asList("Efectivo", "Transferencia", "Tarjeta Débito", "Tarjeta Crédito", "Cheque");
    private static final List<String> VALID_ACADEMIC_PERIODS = Arrays.asList("I", "II", "III", "IV", "Anual");

    private final PaymentConceptDAO conceptDAO = new PaymentConceptDAO();
    private final StudentFeeDAO feeDAO = new StudentFeeDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final GuardianDAO guardianDAO = new GuardianDAO();
    private final PaymentService paymentService = new PaymentService();

    public int registerConcept(PaymentConcept concept) {
        validateConcept(concept);
        int id = conceptDAO.insert(concept);
        AppLogger.getInstance().logInfo("FinancialController.registerConcept", "Concepto de pago registrado ID " + id);
        return id;
    }

    public boolean updateConcept(PaymentConcept concept) {
        validateConcept(concept);
        if (concept.getConceptId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un concepto válido para actualizar.");
        }
        return conceptDAO.update(concept);
    }

    public boolean deleteConcept(int conceptId) {
        return conceptDAO.delete(conceptId);
    }

    public PaymentConcept findConcept(int conceptId) {
        return conceptDAO.findById(conceptId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el concepto de pago con ID " + conceptId));
    }

    public List<PaymentConcept> listConcepts() {
        return conceptDAO.findAll();
    }

    public List<PaymentConcept> listActiveConcepts() {
        return conceptDAO.findActive();
    }

    /**
     * Defines a personalized amount for a student (sibling discount, scholarship, etc.).
     */
    public int registerStudentFee(int studentId, int conceptId, BigDecimal customAmount,
                                  BigDecimal discountPercentage, String reason) {
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        conceptDAO.findById(conceptId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el concepto de pago con ID " + conceptId));
        if (customAmount == null || customAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto personalizado no puede ser negativo.");
        }
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100.");
        }
        StudentFee fee = new StudentFee();
        fee.setStudentId(studentId);
        fee.setConceptId(conceptId);
        fee.setCustomAmount(customAmount);
        fee.setDiscountPercentage(discountPercentage);
        fee.setReason(reason);
        fee.setActive(true);
        return feeDAO.insert(fee);
    }

    public List<StudentFee> listStudentFees(int studentId) {
        return feeDAO.findByStudent(studentId);
    }

    public List<StudentFee> listAllStudentFees() {
        return feeDAO.findAll();
    }

    public BigDecimal resolveAmountForStudent(int studentId, int conceptId) {
        PaymentConcept concept = findConcept(conceptId);
        return feeDAO.findEffectiveAmount(studentId, conceptId, concept.getBaseAmount());
    }

    /**
     * Registers a payment and returns the generated electronic invoice.
     */
    public Invoice registerPayment(Payment payment) {
        validatePayment(payment);
        studentDAO.findById(payment.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + payment.getStudentId()));
        if (payment.getGuardianId() != null) {
            guardianDAO.findById(payment.getGuardianId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el acudiente con ID " + payment.getGuardianId()));
        }
        conceptDAO.findById(payment.getConceptId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el concepto de pago seleccionado."));
        if (payment.getReceiptNumber() != null && paymentDAO.existsReceipt(payment.getReceiptNumber())) {
            throw new IllegalStateException("El número de recibo " + payment.getReceiptNumber() + " ya existe.");
        }
        Invoice invoice = paymentService.registerPaymentWithInvoice(payment);
        AppLogger.getInstance().logInfo("FinancialController.registerPayment",
                "Pago registrado ID " + invoice.getPaymentId() + " por $" + payment.getAmountPaid());
        return invoice;
    }

    public List<Payment> listPaymentsByStudent(int studentId) {
        return paymentDAO.findByStudent(studentId);
    }

    public List<Payment> listPaymentsByGuardian(int guardianId) {
        return paymentDAO.findByGuardian(guardianId);
    }

    public List<Payment> listAllPayments() {
        return paymentDAO.findAll();
    }

    public List<Invoice> listInvoices() {
        return invoiceDAO.findAll();
    }

    public Invoice findInvoiceByPayment(int paymentId) {
        return invoiceDAO.findByPayment(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("El pago indicado no tiene factura generada."));
    }

    /**
     * Students with pending pension payments older than one month.
     */
    public List<String> generateDelinquencyReport(int pensionConceptId, LocalDate referenceDate) {
        LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now();
        return paymentDAO.findDelinquentStudents(reference, pensionConceptId);
    }

    public BigDecimal totalPaidByStudent(int studentId) {
        return paymentDAO.totalCollectedByStudent(studentId);
    }

    public String suggestNextReceiptNumber() {
        return "REC-" + LocalDate.now().getYear() + "-" + String.format("%04d", System.currentTimeMillis() % 10000);
    }

    private void validateConcept(PaymentConcept concept) {
        if (concept == null) {
            throw new IllegalArgumentException("Los datos del concepto de pago son obligatorios.");
        }
        if (concept.getConceptCode() == null || concept.getConceptCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del concepto es obligatorio.");
        }
        if (concept.getConceptName() == null || concept.getConceptName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del concepto es obligatorio.");
        }
        if (concept.getBaseAmount() == null || concept.getBaseAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto base debe ser mayor que cero.");
        }
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Los datos del pago son obligatorios.");
        }
        if (payment.getAmountPaid() == null || payment.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto pagado debe ser mayor que cero.");
        }
        if (payment.getPaymentDate() == null) {
            throw new IllegalArgumentException("La fecha de pago es obligatoria.");
        }
        if (payment.getPaymentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de pago no puede ser futura.");
        }
        if (!VALID_PAYMENT_METHODS.contains(payment.getPaymentMethod())) {
            throw new IllegalArgumentException("Método de pago inválido. Use: " + VALID_PAYMENT_METHODS);
        }
        if (payment.getAcademicPeriod() != null && !personalPeriodValid(payment.getAcademicPeriod())) {
            throw new IllegalArgumentException("Período académico inválido. Use: " + VALID_ACADEMIC_PERIODS);
        }
        if (payment.getReceiptNumber() == null || payment.getReceiptNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de recibo es obligatorio.");
        }
    }

    private boolean personalPeriodValid(String period) {
        return VALID_ACADEMIC_PERIODS.contains(period);
    }
}
