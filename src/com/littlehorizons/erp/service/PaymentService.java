package com.littlehorizons.erp.service;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.dao.DatabaseException;
import com.littlehorizons.erp.dao.PaymentDAO;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transactional service that registers a payment together with its electronic invoice.
 * Either both records are committed or neither is persisted.
 */
public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final InvoiceService invoiceService = new InvoiceService();

    /**
     * Persists a payment and its invoice inside a single database transaction.
     *
     * @param payment validated payment data
     * @return the generated invoice metadata
     */
    public Invoice registerPaymentWithInvoice(Payment payment) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int paymentId = paymentDAO.insert(payment, conn);
                Invoice invoice = invoiceService.generateInvoiceForPayment(conn, paymentId);
                conn.commit();
                return invoice;
            } catch (RuntimeException ex) {
                rollback(conn);
                throw ex;
            } catch (SQLException ex) {
                rollback(conn);
                throw new DatabaseException("Error al confirmar el pago y su factura", ex);
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al abrir la conexión para registrar el pago", ex);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            AppLogger.getInstance().logError("PaymentService.rollback",
                    "No fue posible revertir la transacción del pago", rollbackEx);
        }
    }

    private void restoreAutoCommit(Connection conn, boolean originalAutoCommit) {
        try {
            conn.setAutoCommit(originalAutoCommit);
        } catch (SQLException ex) {
            AppLogger.getInstance().logWarning("PaymentService.restoreAutoCommit",
                    "No fue posible restaurar el modo autocommit: " + ex.getMessage());
        }
    }
}
