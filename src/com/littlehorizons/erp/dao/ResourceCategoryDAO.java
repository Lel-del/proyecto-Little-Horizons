package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.ResourceCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for resource categories.
 */
public class ResourceCategoryDAO {

    public int insert(ResourceCategory category) {
        String sql = "INSERT INTO categorias_recurso (nombre_categoria, descripcion) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    category.setCategoryId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la categoría");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la categoría de recurso", ex);
        }
    }

    public List<ResourceCategory> findAll() {
        String sql = "SELECT * FROM categorias_recurso ORDER BY nombre_categoria";
        List<ResourceCategory> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ResourceCategory c = new ResourceCategory();
                c.setCategoryId(rs.getInt("id_categoria"));
                c.setCategoryName(rs.getString("nombre_categoria"));
                c.setDescription(rs.getString("descripcion"));
                categories.add(c);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las categorías de recursos", ex);
        }
        return categories;
    }
}
