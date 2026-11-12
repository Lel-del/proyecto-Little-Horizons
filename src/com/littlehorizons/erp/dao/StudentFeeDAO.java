package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.StudentFee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for student-specific fees (sibling discounts, scholarships).
 */
public class StudentFeeDAO {

    private static final String SELECT_BASE =
            "SELECT t.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "c.nombre_concepto FROM tarifas_estudiante t " +
            "JOIN estudiantes e ON e.id_estudiante = t.id_estudiante " +
            "JOIN conceptos_pago c ON c.id_concepto = t.id_concepto ";

    public int insert(StudentFee fee) {
        String sql = "INSERT INTO tarifas_estudiante (id_estudiante, id_concepto, monto_personalizado, " +
                "porcentaje_descuento, motivo, esta_activo) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE monto_personalizado = VALUES(monto_personalizado), " +
                "porcentaje_descuento = VALUES(porcentaje_descuento), motivo = VALUES(motivo), esta_activo = VALUES(esta_activo)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fee.getStudentId());
            ps.setInt(2, fee.getConceptId());
            ps.setBigDecimal(3, fee.getCustomAmount());
            ps.setBigDecimal(4, fee.getDiscountPercentage());
            ps.setString(5, fee.getReason());
            ps.setBoolean(6, fee.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    fee.setFeeId(id);
                    return id;
                }
            }
            return 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la tarifa personalizada", ex);
        }
    }

    public boolean delete(int feeId) {
        String sql = "DELETE FROM tarifas_estudiante WHERE id_tarifa = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, feeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la tarifa personalizada", ex);
        }
    }

    public List<StudentFee> findAll() {
        String sql = SELECT_BASE + "ORDER BY e.primer_apellido, c.nombre_concepto";
        List<StudentFee> fees = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                fees.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las tarifas personalizadas", ex);
        }
        return fees;
    }

    public List<StudentFee> findByStudent(int studentId) {
        String sql = SELECT_BASE + "WHERE t.id_estudiante = ? ORDER BY c.nombre_concepto";
        List<StudentFee> fees = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fees.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar tarifas del estudiante", ex);
        }
        return fees;
    }

    /**
     * Effective amount to charge: custom fee when defined, otherwise concept base amount.
     */
    public java.math.BigDecimal findEffectiveAmount(int studentId, int conceptId, java.math.BigDecimal baseAmount) {
        String sql = "SELECT monto_personalizado FROM tarifas_estudiante WHERE id_estudiante = ? " +
                "AND id_concepto = ? AND esta_activo = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, conceptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("monto_personalizado") : baseAmount;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el monto efectivo del concepto", ex);
        }
    }

    private StudentFee map(ResultSet rs) throws SQLException {
        StudentFee f = new StudentFee();
        f.setFeeId(rs.getInt("id_tarifa"));
        f.setStudentId(rs.getInt("id_estudiante"));
        f.setConceptId(rs.getInt("id_concepto"));
        f.setCustomAmount(rs.getBigDecimal("monto_personalizado"));
        f.setDiscountPercentage(rs.getBigDecimal("porcentaje_descuento"));
        f.setReason(rs.getString("motivo"));
        f.setActive(rs.getBoolean("esta_activo"));
        f.setStudentName(rs.getString("nombre_estudiante"));
        f.setConceptName(rs.getString("nombre_concepto"));
        return f;
    }
}
