package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for subjects.
 */
public class SubjectDAO {

    private static final String SELECT_BASE =
            "SELECT m.*, g.nombre_grado FROM materias m JOIN grados g ON g.id_grado = m.id_grado ";

    public int insert(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subject.getSubjectCode());
            ps.setString(2, subject.getSubjectName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getGradeId());
            ps.setInt(5, subject.getWeeklyHours());
            ps.setBoolean(6, subject.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    subject.setSubjectId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la materia");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la materia " + subject.getSubjectCode(), ex);
        }
    }

    public boolean update(Subject subject) {
        String sql = "UPDATE materias SET codigo_materia = ?, nombre_materia = ?, descripcion = ?, id_grado = ?, " +
                "horas_semanales = ?, esta_activa = ? WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectCode());
            ps.setString(2, subject.getSubjectName());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getGradeId());
            ps.setInt(5, subject.getWeeklyHours());
            ps.setBoolean(6, subject.isActive());
            ps.setInt(7, subject.getSubjectId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar la materia ID " + subject.getSubjectId(), ex);
        }
    }

    public boolean delete(int subjectId) {
        String sql = "DELETE FROM materias WHERE id_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la materia ID " + subjectId, ex);
        }
    }

    public Optional<Subject> findById(int subjectId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE m.id_materia = ?")) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar la materia ID " + subjectId, ex);
        }
    }

    public List<Subject> findAll() {
        return query(SELECT_BASE + "ORDER BY g.nivel_numerico, m.nombre_materia", null);
    }

    public List<Subject> findByGrade(int gradeId) {
        return query(SELECT_BASE + "WHERE m.id_grado = ? AND m.esta_activa = TRUE ORDER BY m.nombre_materia", gradeId);
    }

    public List<Subject> findActive() {
        return query(SELECT_BASE + "WHERE m.esta_activa = TRUE ORDER BY g.nivel_numerico, m.nombre_materia", null);
    }

    public boolean existsCode(String code) {
        String sql = "SELECT 1 FROM materias WHERE codigo_materia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el código de materia", ex);
        }
    }

    private List<Subject> query(String sql, Integer gradeId) {
        List<Subject> subjects = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (gradeId != null) {
                ps.setInt(1, gradeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    subjects.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las materias", ex);
        }
        return subjects;
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setSubjectId(rs.getInt("id_materia"));
        s.setSubjectCode(rs.getString("codigo_materia"));
        s.setSubjectName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setGradeId(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setActive(rs.getBoolean("esta_activa"));
        s.setGradeName(rs.getString("nombre_grado"));
        return s;
    }
}
