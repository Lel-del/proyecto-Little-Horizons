package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Guardian;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for guardians and their relation with students.
 */
public class GuardianDAO {

    public int insert(Guardian guardian) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, " +
                "correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, guardian);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    guardian.setGuardianId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del acudiente");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el acudiente " + guardian.getDocumentNumber(), ex);
        }
    }

    public boolean update(Guardian guardian) {
        String sql = "UPDATE acudientes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, " +
                "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, parentesco = ?, " +
                "telefono_principal = ?, telefono_secundario = ?, correo_electronico = ?, direccion = ?, " +
                "ocupacion = ?, es_responsable_financiero = ? WHERE id_acudiente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, guardian);
            ps.setInt(14, guardian.getGuardianId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el acudiente ID " + guardian.getGuardianId(), ex);
        }
    }

    public boolean delete(int guardianId) {
        String sql = "DELETE FROM acudientes WHERE id_acudiente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el acudiente ID " + guardianId, ex);
        }
    }

    public Optional<Guardian> findById(int guardianId) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el acudiente ID " + guardianId, ex);
        }
    }

    public List<Guardian> findAll() {
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, primer_nombre";
        List<Guardian> guardians = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                guardians.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los acudientes", ex);
        }
        return guardians;
    }

    public List<Guardian> findByStudent(int studentId) {
        String sql = "SELECT a.* FROM acudientes a " +
                "JOIN estudiante_acudiente ea ON ea.id_acudiente = a.id_acudiente " +
                "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC, a.primer_apellido";
        List<Guardian> guardians = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guardians.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar acudientes del estudiante ID " + studentId, ex);
        }
        return guardians;
    }

    public boolean linkToStudent(int studentId, int guardianId, boolean principal, boolean emergencyContact) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            ps.setBoolean(3, principal);
            ps.setBoolean(4, emergencyContact);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al asociar el acudiente con el estudiante", ex);
        }
    }

    public boolean unlinkFromStudent(int studentId, int guardianId) {
        String sql = "DELETE FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al desasociar el acudiente del estudiante", ex);
        }
    }

    public boolean existsDocument(String documentNumber) {
        String sql = "SELECT 1 FROM acudientes WHERE numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el documento del acudiente", ex);
        }
    }

    private void bind(PreparedStatement ps, Guardian g) throws SQLException {
        ps.setString(1, g.getDocumentType());
        ps.setString(2, g.getDocumentNumber());
        ps.setString(3, g.getFirstName());
        ps.setString(4, g.getMiddleName());
        ps.setString(5, g.getLastName());
        ps.setString(6, g.getSecondLastName());
        ps.setString(7, g.getRelationship());
        ps.setString(8, g.getPrimaryPhone());
        ps.setString(9, g.getSecondaryPhone());
        ps.setString(10, g.getEmail());
        ps.setString(11, g.getAddress());
        ps.setString(12, g.getOccupation());
        ps.setBoolean(13, g.isFinancialResponsible());
    }

    private Guardian map(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setGuardianId(rs.getInt("id_acudiente"));
        g.setDocumentType(rs.getString("tipo_documento"));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("primer_nombre"));
        g.setMiddleName(rs.getString("segundo_nombre"));
        g.setLastName(rs.getString("primer_apellido"));
        g.setSecondLastName(rs.getString("segundo_apellido"));
        g.setRelationship(rs.getString("parentesco"));
        g.setPrimaryPhone(rs.getString("telefono_principal"));
        g.setSecondaryPhone(rs.getString("telefono_secundario"));
        g.setEmail(rs.getString("correo_electronico"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("ocupacion"));
        g.setFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        return g;
    }
}
