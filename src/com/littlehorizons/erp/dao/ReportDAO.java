package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access object for cross-module statistics used by the reports module.
 */
public class ReportDAO {

    public Map<String, Long> countStudentsByStatus() {
        String sql = "SELECT estado, COUNT(*) AS total FROM estudiantes GROUP BY estado ORDER BY total DESC";
        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("estado"), rs.getLong("total"));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al contar estudiantes por estado", ex);
        }
        return result;
    }

    public List<String> countStudentsByGrade(int academicYear) {
        String sql = "SELECT g.nombre_grado, COUNT(m.id_estudiante) AS total FROM grados g " +
                "LEFT JOIN matriculas m ON m.id_grado = g.id_grado AND m.`año_academico` = ? " +
                "GROUP BY g.id_grado, g.nombre_grado, g.nivel_numerico ORDER BY g.nivel_numerico";
        List<String> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(String.format("%-12s | Estudiantes: %d",
                            rs.getString("nombre_grado"), rs.getInt("total")));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al contar estudiantes por grado", ex);
        }
        return result;
    }

    public Map<String, java.math.BigDecimal> incomeByConcept(int academicYear) {
        String sql = "SELECT c.nombre_concepto, COALESCE(SUM(p.monto_pagado), 0) AS total FROM conceptos_pago c " +
                "LEFT JOIN pagos p ON p.id_concepto = c.id_concepto AND p.`año_academico` = ? " +
                "GROUP BY c.id_concepto, c.nombre_concepto ORDER BY total DESC";
        Map<String, java.math.BigDecimal> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("nombre_concepto"), rs.getBigDecimal("total"));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular ingresos por concepto", ex);
        }
        return result;
    }

    public java.math.BigDecimal totalIncome(int academicYear) {
        String sql = "SELECT COALESCE(SUM(monto_pagado), 0) FROM pagos WHERE `año_academico` = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular el total recaudado", ex);
        }
    }

    public List<String> averageBySubject(int academicYear, String period) {
        String sql = "SELECT m.nombre_materia, g.nombre_grado, AVG(r.valor_nota) AS promedio FROM registros_academicos r " +
                "JOIN materias m ON m.id_materia = r.id_materia " +
                "JOIN grados g ON g.id_grado = m.id_grado " +
                "WHERE r.`año_academico` = ? AND r.periodo_academico = ? " +
                "GROUP BY m.id_materia, m.nombre_materia, g.nombre_grado, g.nivel_numerico " +
                "ORDER BY g.nivel_numerico, m.nombre_materia";
        List<String> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            ps.setString(2, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(String.format("%-12s | %-32s | Promedio: %s",
                            rs.getString("nombre_grado"), rs.getString("nombre_materia"),
                            rs.getBigDecimal("promedio").setScale(1, java.math.RoundingMode.HALF_UP)));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular promedios por materia", ex);
        }
        return result;
    }

    public Map<String, Long> participationByClub() {
        String sql = "SELECT c.nombre_club, COUNT(mc.id_membresia) AS total FROM clubes c " +
                "LEFT JOIN membresias_club mc ON mc.id_club = c.id_club AND mc.estado_membresia = 'Activa' " +
                "GROUP BY c.id_club, c.nombre_club ORDER BY total DESC";
        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("nombre_club"), rs.getLong("total"));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al calcular participación por club", ex);
        }
        return result;
    }

    public Map<String, Long> resourcesByStatus() {
        String sql = "SELECT estado_recurso, COUNT(*) AS total FROM recursos GROUP BY estado_recurso ORDER BY total DESC";
        Map<String, Long> result = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("estado_recurso"), rs.getLong("total"));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al contar recursos por estado", ex);
        }
        return result;
    }
}
