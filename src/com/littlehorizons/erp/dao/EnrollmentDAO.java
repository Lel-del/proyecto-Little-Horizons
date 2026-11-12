package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Enrollment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for yearly grade enrollments.
 */
public class EnrollmentDAO {

    private static final String SELECT_BASE =
            "SELECT m.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "g.nombre_grado FROM matriculas m " +
            "JOIN estudiantes e ON e.id_estudiante = m.id_estudiante " +
            "JOIN grados g ON g.id_grado = m.id_grado ";

    public int insert(Enrollment enrollment) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, `año_academico`, estado_matricula, " +
                "fecha_matricula, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getGradeId());
            ps.setInt(3, enrollment.getAcademicYear());
            ps.setString(4, enrollment.getEnrollmentStatus());
            ps.setDate(5, Date.valueOf(enrollment.getEnrollmentDate()));
            ps.setString(6, enrollment.getObservations());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    enrollment.setEnrollmentId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la matrícula");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la matrícula", ex);
        }
    }

    public boolean updateStatus(int enrollmentId, String status, java.time.LocalDate withdrawalDate) {
        String sql = "UPDATE matriculas SET estado_matricula = ?, fecha_retiro = ? WHERE id_matricula = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setDate(2, withdrawalDate != null ? Date.valueOf(withdrawalDate) : null);
            ps.setInt(3, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el estado de la matrícula", ex);
        }
    }

    public boolean delete(int enrollmentId) {
        String sql = "DELETE FROM matriculas WHERE id_matricula = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la matrícula", ex);
        }
    }

    public List<Enrollment> findByStudent(int studentId) {
        return query(SELECT_BASE + "WHERE m.id_estudiante = ? ORDER BY m.`año_academico` DESC", studentId);
    }

    public List<Enrollment> findByAcademicYear(int academicYear) {
        return query(SELECT_BASE + "WHERE m.`año_academico` = ? ORDER BY g.nivel_numerico", academicYear);
    }

    public List<Enrollment> findAll() {
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY m.`año_academico` DESC, g.nivel_numerico");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                enrollments.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las matrículas", ex);
        }
        return enrollments;
    }

    public boolean existsForYear(int studentId, int academicYear) {
        String sql = "SELECT 1 FROM matriculas WHERE id_estudiante = ? AND `año_academico` = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la matrícula anual", ex);
        }
    }

    private List<Enrollment> query(String sql, int param) {
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar matrículas", ex);
        }
        return enrollments;
    }

    private Enrollment map(ResultSet rs) throws SQLException {
        Enrollment m = new Enrollment();
        m.setEnrollmentId(rs.getInt("id_matricula"));
        m.setStudentId(rs.getInt("id_estudiante"));
        m.setGradeId(rs.getInt("id_grado"));
        m.setAcademicYear(rs.getInt("año_academico"));
        m.setEnrollmentStatus(rs.getString("estado_matricula"));
        Date enrollmentDate = rs.getDate("fecha_matricula");
        m.setEnrollmentDate(enrollmentDate != null ? enrollmentDate.toLocalDate() : null);
        Date withdrawalDate = rs.getDate("fecha_retiro");
        m.setWithdrawalDate(withdrawalDate != null ? withdrawalDate.toLocalDate() : null);
        m.setObservations(rs.getString("observaciones"));
        m.setStudentName(rs.getString("nombre_estudiante"));
        m.setGradeName(rs.getString("nombre_grado"));
        return m;
    }
}
