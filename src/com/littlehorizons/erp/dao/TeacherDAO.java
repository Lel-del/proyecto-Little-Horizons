package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Teacher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for teachers.
 */
public class TeacherDAO {

    private static final String FULL_NAME =
            "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido)";

    public int insert(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, teacher);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    teacher.setTeacherId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del profesor");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el profesor " + teacher.getDocumentNumber(), ex);
        }
    }

    public boolean update(Teacher teacher) {
        String sql = "UPDATE profesores SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, " +
                "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, correo_electronico = ?, telefono = ?, " +
                "especializacion = ?, fecha_contratacion = ?, estado = ? WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, teacher);
            ps.setInt(12, teacher.getTeacherId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el profesor ID " + teacher.getTeacherId(), ex);
        }
    }

    public boolean delete(int teacherId) {
        String sql = "DELETE FROM profesores WHERE id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el profesor ID " + teacherId, ex);
        }
    }

    public Optional<Teacher> findById(int teacherId) {
        String sql = "SELECT p.* FROM profesores p WHERE p.id_profesor = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el profesor ID " + teacherId, ex);
        }
    }

    public List<Teacher> findAll() {
        return query("SELECT p.* FROM profesores p ORDER BY p.primer_apellido", null);
    }

    public List<Teacher> findActive() {
        return query("SELECT p.* FROM profesores p WHERE p.estado = 'Activo' ORDER BY p.primer_apellido", null);
    }

    public List<Teacher> searchByName(String term) {
        String sql = "SELECT p.* FROM profesores p WHERE " + FULL_NAME + " LIKE ? ORDER BY p.primer_apellido";
        List<Teacher> teachers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teachers.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al buscar profesores por nombre", ex);
        }
        return teachers;
    }

    public boolean existsDocument(String documentNumber) {
        String sql = "SELECT 1 FROM profesores WHERE numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el documento del profesor", ex);
        }
    }

    private List<Teacher> query(String sql, String param) {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teachers.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar profesores", ex);
        }
        return teachers;
    }

    private void bind(PreparedStatement ps, Teacher t) throws SQLException {
        ps.setString(1, t.getDocumentType());
        ps.setString(2, t.getDocumentNumber());
        ps.setString(3, t.getFirstName());
        ps.setString(4, t.getMiddleName());
        ps.setString(5, t.getLastName());
        ps.setString(6, t.getSecondLastName());
        ps.setString(7, t.getEmail());
        ps.setString(8, t.getPhone());
        ps.setString(9, t.getSpecialization());
        ps.setDate(10, Date.valueOf(t.getHireDate()));
        ps.setString(11, t.getStatus());
    }

    private Teacher map(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setTeacherId(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setMiddleName(rs.getString("segundo_nombre"));
        t.setLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        Date hireDate = rs.getDate("fecha_contratacion");
        t.setHireDate(hireDate != null ? hireDate.toLocalDate() : null);
        t.setStatus(rs.getString("estado"));
        return t;
    }
}
