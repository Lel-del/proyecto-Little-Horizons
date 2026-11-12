package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.PaymentConcept;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for payment concepts.
 */
public class PaymentConceptDAO {

    public int insert(PaymentConcept concept) {
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, " +
                "es_recurrente, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, concept.getConceptCode());
            ps.setString(2, concept.getConceptName());
            ps.setString(3, concept.getDescription());
            ps.setBigDecimal(4, concept.getBaseAmount());
            ps.setBoolean(5, concept.isRecurring());
            ps.setBoolean(6, concept.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    concept.setConceptId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del concepto de pago");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el concepto de pago", ex);
        }
    }

    public boolean update(PaymentConcept concept) {
        String sql = "UPDATE conceptos_pago SET codigo_concepto = ?, nombre_concepto = ?, descripcion = ?, " +
                "monto_base = ?, es_recurrente = ?, esta_activo = ? WHERE id_concepto = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, concept.getConceptCode());
            ps.setString(2, concept.getConceptName());
            ps.setString(3, concept.getDescription());
            ps.setBigDecimal(4, concept.getBaseAmount());
            ps.setBoolean(5, concept.isRecurring());
            ps.setBoolean(6, concept.isActive());
            ps.setInt(7, concept.getConceptId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el concepto de pago", ex);
        }
    }

    public boolean delete(int conceptId) {
        String sql = "DELETE FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conceptId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el concepto de pago", ex);
        }
    }

    public Optional<PaymentConcept> findById(int conceptId) {
        String sql = "SELECT * FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conceptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el concepto de pago", ex);
        }
    }

    public List<PaymentConcept> findAll() {
        String sql = "SELECT * FROM conceptos_pago ORDER BY nombre_concepto";
        List<PaymentConcept> concepts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                concepts.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los conceptos de pago", ex);
        }
        return concepts;
    }

    public List<PaymentConcept> findActive() {
        String sql = "SELECT * FROM conceptos_pago WHERE esta_activo = TRUE ORDER BY nombre_concepto";
        List<PaymentConcept> concepts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                concepts.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los conceptos activos", ex);
        }
        return concepts;
    }

    private PaymentConcept map(ResultSet rs) throws SQLException {
        PaymentConcept c = new PaymentConcept();
        c.setConceptId(rs.getInt("id_concepto"));
        c.setConceptCode(rs.getString("codigo_concepto"));
        c.setConceptName(rs.getString("nombre_concepto"));
        c.setDescription(rs.getString("descripcion"));
        c.setBaseAmount(rs.getBigDecimal("monto_base"));
        c.setRecurring(rs.getBoolean("es_recurrente"));
        c.setActive(rs.getBoolean("esta_activo"));
        return c;
    }
}
