package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for academic grade levels.
 */
public class GradeDAO {

    public List<Grade> findAll() {
        String sql = "SELECT id_grado, nombre_grado, nivel_numerico FROM grados ORDER BY nivel_numerico";
        List<Grade> grades = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                grades.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los grados académicos", ex);
        }
        return grades;
    }

    public java.util.Optional<Grade> findById(int gradeId) {
        String sql = "SELECT id_grado, nombre_grado, nivel_numerico FROM grados WHERE id_grado = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? java.util.Optional.of(map(rs)) : java.util.Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al buscar el grado con ID " + gradeId, ex);
        }
    }

    public Grade findByName(String name) {
        String sql = "SELECT id_grado, nombre_grado, nivel_numerico FROM grados WHERE nombre_grado = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al buscar el grado por nombre", ex);
        }
    }

    private Grade map(ResultSet rs) throws SQLException {
        return new Grade(rs.getInt("id_grado"), rs.getString("nombre_grado"), rs.getInt("nivel_numerico"));
    }
}
