package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Payment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for payments and delinquency reports.
 */
public class PaymentDAO {

    private static final String SELECT_BASE =
            "SELECT p.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "CONCAT_WS(' ', a.primer_nombre, a.segundo_nombre, a.primer_apellido, a.segundo_apellido) AS nombre_acudiente, " +
            "COALESCE(a.tipo_documento, e.tipo_documento) AS tipo_documento_cliente, " +
            "COALESCE(a.numero_documento, e.numero_documento) AS documento_cliente, " +
            "COALESCE(a.direccion, e.direccion) AS direccion_cliente, " +
            "c.nombre_concepto FROM pagos p " +
            "JOIN estudiantes e ON e.id_estudiante = p.id_estudiante " +
            "LEFT JOIN acudientes a ON a.id_acudiente = p.id_acudiente " +
            "JOIN conceptos_pago c ON c.id_concepto = p.id_concepto ";

    public int insert(Payment payment) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return insert(payment, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el pago", ex);
        }
    }

    /**
     * Inserts a payment using an externally managed connection so callers can enlist it in a transaction.
     */
    public int insert(Payment payment, Connection conn) {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, " +
                "metodo_pago, numero_recibo, `año_academico`, periodo_academico, notas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getStudentId());
            if (payment.getGuardianId() != null) {
                ps.setInt(2, payment.getGuardianId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, payment.getConceptId());
            ps.setBigDecimal(4, payment.getAmountPaid());
            ps.setDate(5, Date.valueOf(payment.getPaymentDate()));
            ps.setString(6, payment.getPaymentMethod());
            ps.setString(7, payment.getReceiptNumber());
            if (payment.getAcademicYear() != null) {
                ps.setInt(8, payment.getAcademicYear());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, payment.getAcademicPeriod());
            ps.setString(10, payment.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    payment.setPaymentId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del pago");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el pago", ex);
        }
    }

    public boolean delete(int paymentId) {
        String sql = "DELETE FROM pagos WHERE id_pago = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el pago", ex);
        }
    }

    public Optional<Payment> findById(int paymentId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return findById(paymentId, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el pago ID " + paymentId, ex);
        }
    }

    /**
     * Finds a payment using an externally managed connection (transaction-aware).
     */
    public Optional<Payment> findById(int paymentId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id_pago = ?")) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el pago ID " + paymentId, ex);
        }
    }

    public List<Payment> findByStudent(int studentId) {
        return query(SELECT_BASE + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC", studentId);
    }

    public List<Payment> findByGuardian(int guardianId) {
        return query(SELECT_BASE + "WHERE p.id_acudiente = ? ORDER BY p.fecha_pago DESC", guardianId);
    }

    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY p.fecha_pago DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los pagos", ex);
        }
        return payments;
    }

    /**
     * Students whose latest pension payment is older than the reference date minus one month.
     */
    public List<String> findDelinquentStudents(java.time.LocalDate referenceDate, int conceptId) {
        String sql = "SELECT CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS estudiante, " +
                "MAX(p.fecha_pago) AS ultimo_pago, " +
                "DATEDIFF(?, MAX(p.fecha_pago)) AS dias_mora " +
                "FROM estudiantes e JOIN pagos p ON p.id_estudiante = e.id_estudiante " +
                "WHERE p.id_concepto = ? AND e.estado = 'Activo' " +
                "GROUP BY e.id_estudiante, estudiante " +
                "HAVING MAX(p.fecha_pago) < DATE_SUB(?, INTERVAL 1 MONTH) ORDER BY ultimo_pago";
        List<String> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(referenceDate));
            ps.setInt(2, conceptId);
            ps.setDate(3, Date.valueOf(referenceDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(String.format("%-45s | Último pago: %s | Días de mora: %d",
                            rs.getString("estudiante"), rs.getDate("ultimo_pago"), rs.getInt("dias_mora")));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al generar el reporte de morosos", ex);
        }
        return results;
    }

    public boolean existsReceipt(String receiptNumber) {
        String sql = "SELECT 1 FROM pagos WHERE numero_recibo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el número de recibo", ex);
        }
    }

    public java.math.BigDecimal totalCollectedByStudent(int studentId) {
        String sql = "SELECT COALESCE(SUM(monto_pagado), 0) FROM pagos WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el total pagado", ex);
        }
    }

    private List<Payment> query(String sql, int param) {
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar pagos", ex);
        }
        return payments;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("id_pago"));
        p.setStudentId(rs.getInt("id_estudiante"));
        int guardianId = rs.getInt("id_acudiente");
        p.setGuardianId(rs.wasNull() ? null : guardianId);
        p.setConceptId(rs.getInt("id_concepto"));
        p.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        Date date = rs.getDate("fecha_pago");
        p.setPaymentDate(date != null ? date.toLocalDate() : null);
        p.setPaymentMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        int year = rs.getInt("año_academico");
        p.setAcademicYear(rs.wasNull() ? null : year);
        p.setAcademicPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));
        p.setStudentName(rs.getString("nombre_estudiante"));
        p.setGuardianName(rs.getString("nombre_acudiente"));
        p.setClientDocumentType(rs.getString("tipo_documento_cliente"));
        p.setClientDocument(rs.getString("documento_cliente"));
        p.setClientAddress(rs.getString("direccion_cliente"));
        p.setConceptName(rs.getString("nombre_concepto"));
        return p;
    }
}
