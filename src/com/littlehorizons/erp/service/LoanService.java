package com.littlehorizons.erp.service;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.dao.DatabaseException;
import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.dao.ResourceLoanDAO;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Transactional service for resource loans and returns.
 * The loan row and the resource status are committed atomically.
 */
public class LoanService {

    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final ResourceLoanDAO loanDAO = new ResourceLoanDAO();

    /**
     * Registers a loan after locking the resource row and re-validating its availability.
     */
    public int registerLoan(ResourceLoan loan, ResourceLoanDAO.BorrowerType borrowerType) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Resource resource = resourceDAO.findByIdForUpdate(loan.getResourceId(), conn)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No existe el recurso con ID " + loan.getResourceId()));
                if (!"Disponible".equalsIgnoreCase(resource.getResourceStatus())) {
                    throw new IllegalStateException("El recurso no está disponible. Estado actual: "
                            + resource.getResourceStatus());
                }
                if (loanDAO.hasActiveLoanForResource(loan.getResourceId(), conn)) {
                    throw new IllegalStateException("El recurso ya tiene un préstamo activo registrado.");
                }
                int loanId = loanDAO.insert(loan, borrowerType, conn);
                resourceDAO.updateStatus(loan.getResourceId(), "Prestado", conn);
                conn.commit();
                return loanId;
            } catch (RuntimeException ex) {
                rollback(conn);
                throw ex;
            } catch (SQLException ex) {
                rollback(conn);
                throw new DatabaseException("Error al confirmar el préstamo del recurso", ex);
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al abrir la conexión para registrar el préstamo", ex);
        }
    }

    /**
     * Registers a return and frees the resource inside the same transaction.
     */
    public boolean registerReturn(ResourceLoan loan, ResourceLoanDAO.BorrowerType borrowerType, LocalDate returnDate) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                boolean updated = loanDAO.registerReturn(loan.getLoanId(), borrowerType, returnDate, conn);
                if (updated) {
                    resourceDAO.updateStatus(loan.getResourceId(), "Disponible", conn);
                }
                conn.commit();
                return updated;
            } catch (RuntimeException ex) {
                rollback(conn);
                throw ex;
            } catch (SQLException ex) {
                rollback(conn);
                throw new DatabaseException("Error al confirmar la devolución del recurso", ex);
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al abrir la conexión para registrar la devolución", ex);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            AppLogger.getInstance().logError("LoanService.rollback",
                    "No fue posible revertir la transacción del préstamo", ex);
        }
    }

    private void restoreAutoCommit(Connection conn, boolean originalAutoCommit) {
        try {
            conn.setAutoCommit(originalAutoCommit);
        } catch (SQLException ex) {
            AppLogger.getInstance().logWarning("LoanService.restoreAutoCommit",
                    "No fue posible restaurar el modo autocommit: " + ex.getMessage());
        }
    }
}
