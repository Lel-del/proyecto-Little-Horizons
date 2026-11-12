package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.AcademicRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for academic grade records and average calculations.
 */
public class AcademicRecordDAO {

    private static final String SELECT_BASE =
            "SELECT r.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "m.nombre_materia, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_profesor " +
            "FROM registros_academicos r " +
            "JOIN estudiantes e ON e.id_estudiante = r.id_estudiante " +
            "JOIN materias m ON m.id_materia = r.id_materia " +
            "JOIN profesores p ON p.id_profesor = r.id_profesor ";

    public int insert(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, `año_academico`, " +
                "periodo_academico, valor_nota, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getStudentId());
            ps.setInt(2, record.getSubjectId());
            ps.setInt(3, record.getTeacherId());
            ps.setInt(4, record.getAcademicYear());
            ps.setString(5, record.getAcademicPeriod());
            ps.setBigDecimal(6, record.getGradeValue());
            ps.setString(7, record.getObservations());
            ps.setDate(8, Date.valueOf(record.getRecordDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    record.setRecordId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del registro académico");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la calificación", ex);
        }
    }

    public boolean update(AcademicRecord record) {
        String sql = "UPDATE registros_academicos SET valor_nota = ?, observaciones = ?, fecha_registro = ?, " +
                "id_profesor = ? WHERE id_registro = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, record.getGradeValue());
            ps.setString(2, record.getObservations());
            ps.setDate(3, Date.valueOf(record.getRecordDate()));
            ps.setInt(4, record.getTeacherId());
            ps.setInt(5, record.getRecordId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar la calificación ID " + record.getRecordId(), ex);
        }
    }

    public boolean delete(int recordId) {
        String sql = "DELETE FROM registros_academicos WHERE id_registro = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la calificación ID " + recordId, ex);
        }
    }

    public List<AcademicRecord> findByStudent(int studentId, int academicYear) {
        String sql = SELECT_BASE + "WHERE r.id_estudiante = ? AND r.`año_academico` = ? " +
                "ORDER BY m.nombre_materia, r.periodo_academico";
        List<AcademicRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el historial del estudiante", ex);
        }
        return records;
    }

    public List<AcademicRecord> findHistory(int studentId) {
        String sql = SELECT_BASE + "WHERE r.id_estudiante = ? ORDER BY r.`año_academico` DESC, " +
                "r.periodo_academico, m.nombre_materia";
        List<AcademicRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el historial académico completo", ex);
        }
        return records;
    }

    public List<AcademicRecord> findBySubjectAndPeriod(int subjectId, int academicYear, String period) {
        String sql = SELECT_BASE + "WHERE r.id_materia = ? AND r.`año_academico` = ? AND r.periodo_academico = ? " +
                "ORDER BY e.primer_apellido, e.primer_nombre";
        List<AcademicRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, academicYear);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar notas por materia y período", ex);
        }
        return records;
    }

    public boolean exists(int studentId, int subjectId, int academicYear, String period) {
        String sql = "SELECT 1 FROM registros_academicos WHERE id_estudiante = ? AND id_materia = ? " +
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
            throw new DatabaseException("Error al validar la existencia de la nota", ex);
        }
    }

    /**
     * Average grade of a student in one subject for the whole academic year.
     */
    public BigDecimal calculateStudentSubjectAverage(int studentId, int subjectId, int academicYear) {
        String sql = "SELECT AVG(valor_nota) FROM registros_academicos WHERE id_estudiante = ? " +
                "AND id_materia = ? AND `año_academico` = ? AND periodo_academico <> 'Final'";
        return queryAverage(sql, studentId, subjectId, academicYear);
    }

    /**
     * General average of a student during a specific academic period.
     */
    public BigDecimal calculateStudentPeriodAverage(int studentId, int academicYear, String period) {
        String sql = "SELECT AVG(valor_nota) FROM registros_academicos WHERE id_estudiante = ? " +
                "AND `año_academico` = ? AND periodo_academico = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? round(rs.getBigDecimal(1)) : null;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el promedio del período", ex);
        }
    }

    /**
     * Course average for a subject in a given academic year and period.
     */
    public BigDecimal calculateCourseSubjectAverage(int subjectId, int academicYear, String period) {
        String sql = "SELECT AVG(valor_nota) FROM registros_academicos WHERE id_materia = ? " +
                "AND `año_academico` = ? AND periodo_academico = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, academicYear);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? round(rs.getBigDecimal(1)) : null;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el promedio del curso", ex);
        }
    }

    public List<String> findLowPerformingStudents(BigDecimal threshold, int academicYear, String period) {
        String sql = "SELECT CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS estudiante, " +
                "m.nombre_materia, r.valor_nota FROM registros_academicos r " +
                "JOIN estudiantes e ON e.id_estudiante = r.id_estudiante " +
                "JOIN materias m ON m.id_materia = r.id_materia " +
                "WHERE r.`año_academico` = ? AND r.periodo_academico = ? AND r.valor_nota < ? " +
                "ORDER BY r.valor_nota";
        List<String> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            ps.setString(2, period);
            ps.setBigDecimal(3, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(String.format("%-45s | %-30s | Nota: %s",
                            rs.getString("estudiante"), rs.getString("nombre_materia"), rs.getBigDecimal("valor_nota")));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al generar el reporte de bajo rendimiento", ex);
        }
        return results;
    }

    private BigDecimal queryAverage(String sql, int studentId, int subjectId, int academicYear) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? round(rs.getBigDecimal(1)) : null;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el promedio", ex);
        }
    }

    private BigDecimal round(BigDecimal value) {
        return value == null ? null : value.setScale(1, java.math.RoundingMode.HALF_UP);
    }

    private AcademicRecord map(ResultSet rs) throws SQLException {
        AcademicRecord r = new AcademicRecord();
        r.setRecordId(rs.getInt("id_registro"));
        r.setStudentId(rs.getInt("id_estudiante"));
        r.setSubjectId(rs.getInt("id_materia"));
        r.setTeacherId(rs.getInt("id_profesor"));
        r.setAcademicYear(rs.getInt("año_academico"));
        r.setAcademicPeriod(rs.getString("periodo_academico"));
        r.setGradeValue(rs.getBigDecimal("valor_nota"));
        r.setObservations(rs.getString("observaciones"));
        Date date = rs.getDate("fecha_registro");
        r.setRecordDate(date != null ? date.toLocalDate() : null);
        r.setStudentName(rs.getString("nombre_estudiante"));
        r.setSubjectName(rs.getString("nombre_materia"));
        r.setTeacherName(rs.getString("nombre_profesor"));
        return r;
    }
}
