package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Student;

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
 * Data access object for students.
 */
public class StudentDAO {

    private static final String FULL_NAME =
            "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido)";

    public int insert(Student student) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, " +
                "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, " +
                "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, student);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    student.setStudentId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del estudiante");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el estudiante " + student.getDocumentNumber(), ex);
        }
    }

    public boolean update(Student student) {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, " +
                "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, " +
                "direccion = ?, telefono = ?, correo_electronico = ?, tipo_sangre = ?, alergias = ?, " +
                "condiciones_medicas = ?, fecha_ingreso = ?, estado = ? WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, student);
            ps.setInt(17, student.getStudentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el estudiante ID " + student.getStudentId(), ex);
        }
    }

    public boolean delete(int studentId) {
        String sql = "DELETE FROM estudiantes WHERE id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el estudiante ID " + studentId, ex);
        }
    }

    public Optional<Student> findById(int studentId) {
        String sql = "SELECT e.* FROM estudiantes e WHERE e.id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el estudiante ID " + studentId, ex);
        }
    }

    public Optional<Student> findByDocument(String documentNumber) {
        String sql = "SELECT e.* FROM estudiantes e WHERE e.numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el estudiante con documento " + documentNumber, ex);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT e.* FROM estudiantes e ORDER BY e.primer_apellido, e.primer_nombre";
        return queryList(sql, null);
    }

    public List<Student> findByStatus(String status) {
        String sql = "SELECT e.* FROM estudiantes e WHERE e.estado = ? ORDER BY e.primer_apellido";
        return queryList(sql, status);
    }

    public List<Student> searchByName(String term) {
        String sql = "SELECT e.* FROM estudiantes e WHERE " + FULL_NAME + " LIKE ? ORDER BY e.primer_apellido";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al buscar estudiantes por nombre", ex);
        }
        return students;
    }

    public List<Student> findByGuardian(int guardianId) {
        String sql = "SELECT e.* FROM estudiantes e " +
                "JOIN estudiante_acudiente ea ON ea.id_estudiante = e.id_estudiante " +
                "WHERE ea.id_acudiente = ? ORDER BY e.primer_apellido";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar estudiantes del acudiente ID " + guardianId, ex);
        }
        return students;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM estudiantes WHERE estado = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al contar estudiantes por estado", ex);
        }
    }

    public boolean existsDocument(String documentNumber) {
        String sql = "SELECT 1 FROM estudiantes WHERE numero_documento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el documento del estudiante", ex);
        }
    }

    private List<Student> queryList(String sql, String param) {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar estudiantes", ex);
        }
        return students;
    }

    private void bind(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getDocumentType());
        ps.setString(2, s.getDocumentNumber());
        ps.setString(3, s.getFirstName());
        ps.setString(4, s.getMiddleName());
        ps.setString(5, s.getLastName());
        ps.setString(6, s.getSecondLastName());
        ps.setDate(7, Date.valueOf(s.getBirthDate()));
        ps.setString(8, s.getGender());
        ps.setString(9, s.getAddress());
        ps.setString(10, s.getPhone());
        ps.setString(11, s.getEmail());
        ps.setString(12, s.getBloodType());
        ps.setString(13, s.getAllergies());
        ps.setString(14, s.getMedicalConditions());
        ps.setDate(15, Date.valueOf(s.getEntryDate()));
        ps.setString(16, s.getStatus());
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("id_estudiante"));
        s.setDocumentType(rs.getString("tipo_documento"));
        s.setDocumentNumber(rs.getString("numero_documento"));
        s.setFirstName(rs.getString("primer_nombre"));
        s.setMiddleName(rs.getString("segundo_nombre"));
        s.setLastName(rs.getString("primer_apellido"));
        s.setSecondLastName(rs.getString("segundo_apellido"));
        Date birth = rs.getDate("fecha_nacimiento");
        s.setBirthDate(birth != null ? birth.toLocalDate() : null);
        s.setGender(rs.getString("genero"));
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        Date entry = rs.getDate("fecha_ingreso");
        s.setEntryDate(entry != null ? entry.toLocalDate() : null);
        s.setStatus(rs.getString("estado"));
        return s;
    }
}
