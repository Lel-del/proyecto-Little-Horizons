package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.SubjectEnrollment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for student subject enrollments.
 */
public class SubjectEnrollmentDAO {

    private static final String SELECT_BASE =
            "SELECT i.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "m.nombre_materia FROM inscripciones_materia i " +
            "JOIN estudiantes e ON e.id_estudiante = i.id_estudiante " +
            "JOIN materias m ON m.id_materia = i.id_materia ";

    public int insert(SubjectEnrollment enrollment) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, `año_academico`, " +
                "periodo_academico, seccion, fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudentId());
            ps.setInt(2, enrollment.getSubjectId());
            ps.setInt(3, enrollment.getAcademicYear());
            ps.setString(4, enrollment.getAcademicPeriod());
            ps.setString(5, enrollment.getSection());
            ps.setDate(6, Date.valueOf(enrollment.getEnrollmentDate()));
            ps.setString(7, enrollment.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    enrollment.setSubjectEnrollmentId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la inscripción");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al inscribir la materia", ex);
        }
    }

    public boolean delete(int enrollmentId) {
        String sql = "DELETE FROM inscripciones_materia WHERE id_inscripcion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la inscripción", ex);
        }
    }

    public List<SubjectEnrollment> findByStudent(int studentId, int academicYear) {
        String sql = SELECT_BASE + "WHERE i.id_estudiante = ? AND i.`año_academico` = ? ORDER BY m.nombre_materia";
        List<SubjectEnrollment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las inscripciones del estudiante", ex);
        }
        return list;
    }

    public boolean existsForYear(int studentId, int subjectId, int academicYear) {
        String sql = "SELECT 1 FROM inscripciones_materia WHERE id_estudiante = ? AND id_materia = ? " +
                "AND `año_academico` = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la inscripción anual de la materia", ex);
        }
    }

    public boolean exists(int studentId, int subjectId, int academicYear, String period) {
        String sql = "SELECT 1 FROM inscripciones_materia WHERE id_estudiante = ? AND id_materia = ? " +
                "AND `año_academico` = ? AND periodo_academico = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYear);
            ps.setString(4, period);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la inscripción de materia", ex);
        }
    }

    private SubjectEnrollment map(ResultSet rs) throws SQLException {
        SubjectEnrollment i = new SubjectEnrollment();
        i.setSubjectEnrollmentId(rs.getInt("id_inscripcion"));
        i.setStudentId(rs.getInt("id_estudiante"));
        i.setSubjectId(rs.getInt("id_materia"));
        i.setAcademicYear(rs.getInt("año_academico"));
        i.setAcademicPeriod(rs.getString("periodo_academico"));
        i.setSection(rs.getString("seccion"));
        Date date = rs.getDate("fecha_inscripcion");
        i.setEnrollmentDate(date != null ? date.toLocalDate() : null);
        i.setStatus(rs.getString("estado"));
        i.setStudentName(rs.getString("nombre_estudiante"));
        i.setSubjectName(rs.getString("nombre_materia"));
        return i;
    }
}
