package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Attendance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for daily attendance records and absence reports.
 */
public class AttendanceDAO {

    private static final String SELECT_BASE =
            "SELECT a.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "m.nombre_materia FROM asistencias a " +
            "JOIN estudiantes e ON e.id_estudiante = a.id_estudiante " +
            "JOIN materias m ON m.id_materia = a.id_materia ";

    public int insert(Attendance attendance) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, " +
                "observaciones, registrado_por) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attendance.getStudentId());
            ps.setInt(2, attendance.getSubjectId());
            ps.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            ps.setString(4, attendance.getAttendanceStatus());
            ps.setString(5, attendance.getObservations());
            if (attendance.getRecordedByTeacherId() != null) {
                ps.setInt(6, attendance.getRecordedByTeacherId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    attendance.setAttendanceId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la asistencia");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la asistencia", ex);
        }
    }

    public boolean delete(int attendanceId) {
        String sql = "DELETE FROM asistencias WHERE id_asistencia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la asistencia", ex);
        }
    }

    public List<Attendance> findByStudent(int studentId) {
        String sql = SELECT_BASE + "WHERE a.id_estudiante = ? ORDER BY a.fecha_asistencia DESC";
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar la asistencia del estudiante", ex);
        }
        return list;
    }

    public List<Attendance> findByDateAndSubject(java.time.LocalDate date, int subjectId) {
        String sql = SELECT_BASE + "WHERE a.fecha_asistencia = ? AND a.id_materia = ? ORDER BY e.primer_apellido";
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar la asistencia del día", ex);
        }
        return list;
    }

    /**
     * Students with more than the given number of absences within a date range.
     */
    public List<String> findStudentsWithExcessiveAbsences(int minAbsences, java.time.LocalDate from, java.time.LocalDate to) {
        String sql = "SELECT CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS estudiante, " +
                "COUNT(*) AS total_ausencias FROM asistencias a " +
                "JOIN estudiantes e ON e.id_estudiante = a.id_estudiante " +
                "WHERE a.estado_asistencia = 'Ausente' AND a.fecha_asistencia BETWEEN ? AND ? " +
                "GROUP BY e.id_estudiante, estudiante HAVING COUNT(*) > ? ORDER BY total_ausencias DESC";
        List<String> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ps.setInt(3, minAbsences);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(String.format("%-45s | Ausencias: %d",
                            rs.getString("estudiante"), rs.getInt("total_ausencias")));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al generar el reporte de inasistencias", ex);
        }
        return results;
    }

    public boolean exists(int studentId, int subjectId, java.time.LocalDate date) {
        String sql = "SELECT 1 FROM asistencias WHERE id_estudiante = ? AND id_materia = ? AND fecha_asistencia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la asistencia", ex);
        }
    }

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttendanceId(rs.getInt("id_asistencia"));
        a.setStudentId(rs.getInt("id_estudiante"));
        a.setSubjectId(rs.getInt("id_materia"));
        Date date = rs.getDate("fecha_asistencia");
        a.setAttendanceDate(date != null ? date.toLocalDate() : null);
        a.setAttendanceStatus(rs.getString("estado_asistencia"));
        a.setObservations(rs.getString("observaciones"));
        int teacherId = rs.getInt("registrado_por");
        a.setRecordedByTeacherId(rs.wasNull() ? null : teacherId);
        a.setStudentName(rs.getString("nombre_estudiante"));
        a.setSubjectName(rs.getString("nombre_materia"));
        return a;
    }
}
