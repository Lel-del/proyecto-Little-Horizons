package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.TeacherAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for teacher subject assignments and class schedules.
 */
public class TeacherAssignmentDAO {

    private static final String SELECT_BASE =
            "SELECT a.*, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_profesor, " +
            "m.nombre_materia FROM asignaciones_profesor a " +
            "JOIN profesores p ON p.id_profesor = a.id_profesor " +
            "JOIN materias m ON m.id_materia = a.id_materia ";

    public int insert(TeacherAssignment assignment) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, `año_academico`, seccion, " +
                "dia_semana, hora_inicio, hora_fin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, assignment.getTeacherId());
            ps.setInt(2, assignment.getSubjectId());
            ps.setInt(3, assignment.getAcademicYear());
            ps.setString(4, assignment.getSection());
            ps.setString(5, assignment.getDayOfWeek());
            ps.setTime(6, assignment.getStartTime() != null ? Time.valueOf(assignment.getStartTime()) : null);
            ps.setTime(7, assignment.getEndTime() != null ? Time.valueOf(assignment.getEndTime()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    assignment.setAssignmentId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la asignación");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la asignación académica", ex);
        }
    }

    public boolean delete(int assignmentId) {
        String sql = "DELETE FROM asignaciones_profesor WHERE id_asignacion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la asignación ID " + assignmentId, ex);
        }
    }

    public List<TeacherAssignment> findByTeacher(int teacherId) {
        return query(SELECT_BASE + "WHERE a.id_profesor = ? ORDER BY a.dia_semana, a.hora_inicio", teacherId);
    }

    public List<TeacherAssignment> findAll() {
        return query(SELECT_BASE + "ORDER BY p.primer_apellido, a.dia_semana, a.hora_inicio", null);
    }

    public List<TeacherAssignment> findBySubject(int subjectId) {
        return query(SELECT_BASE + "WHERE a.id_materia = ? ORDER BY a.dia_semana", subjectId);
    }

    /**
     * Detects a schedule conflict for the same teacher, day and academic year.
     */
    public boolean hasScheduleConflict(int teacherId, int academicYear, String dayOfWeek,
                                       java.time.LocalTime start, java.time.LocalTime end, Integer excludeAssignmentId) {
        String sql = "SELECT COUNT(*) FROM asignaciones_profesor WHERE id_profesor = ? AND `año_academico` = ? " +
                "AND dia_semana = ? AND hora_inicio < ? AND hora_fin > ? " +
                (excludeAssignmentId != null ? "AND id_asignacion <> ?" : "");
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, academicYear);
            ps.setString(3, dayOfWeek);
            ps.setTime(4, Time.valueOf(end));
            ps.setTime(5, Time.valueOf(start));
            if (excludeAssignmentId != null) {
                ps.setInt(6, excludeAssignmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar conflictos de horario", ex);
        }
    }

    private List<TeacherAssignment> query(String sql, Integer param) {
        List<TeacherAssignment> assignments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar asignaciones académicas", ex);
        }
        return assignments;
    }

    private TeacherAssignment map(ResultSet rs) throws SQLException {
        TeacherAssignment a = new TeacherAssignment();
        a.setAssignmentId(rs.getInt("id_asignacion"));
        a.setTeacherId(rs.getInt("id_profesor"));
        a.setSubjectId(rs.getInt("id_materia"));
        a.setAcademicYear(rs.getInt("año_academico"));
        a.setSection(rs.getString("seccion"));
        a.setDayOfWeek(rs.getString("dia_semana"));
        Time start = rs.getTime("hora_inicio");
        a.setStartTime(start != null ? start.toLocalTime() : null);
        Time end = rs.getTime("hora_fin");
        a.setEndTime(end != null ? end.toLocalTime() : null);
        a.setTeacherName(rs.getString("nombre_profesor"));
        a.setSubjectName(rs.getString("nombre_materia"));
        return a;
    }
}
