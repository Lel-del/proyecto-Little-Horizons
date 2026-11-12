package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Resource;

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
 * Data access object for loanable school resources.
 */
public class ResourceDAO {

    private static final String SELECT_BASE =
            "SELECT r.*, c.nombre_categoria FROM recursos r " +
            "JOIN categorias_recurso c ON c.id_categoria = r.id_categoria ";

    public int insert(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, " +
                "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getResourceCode());
            ps.setString(2, resource.getResourceName());
            ps.setInt(3, resource.getCategoryId());
            ps.setString(4, resource.getDescription());
            ps.setDate(5, resource.getAcquisitionDate() != null ? Date.valueOf(resource.getAcquisitionDate()) : null);
            ps.setString(6, resource.getPhysicalLocation());
            ps.setString(7, resource.getResourceStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    resource.setResourceId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del recurso");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el recurso " + resource.getResourceCode(), ex);
        }
    }

    public boolean update(Resource resource) {
        String sql = "UPDATE recursos SET codigo_recurso = ?, nombre_recurso = ?, id_categoria = ?, descripcion = ?, " +
                "fecha_adquisicion = ?, ubicacion_fisica = ?, estado_recurso = ? WHERE id_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource.getResourceCode());
            ps.setString(2, resource.getResourceName());
            ps.setInt(3, resource.getCategoryId());
            ps.setString(4, resource.getDescription());
            ps.setDate(5, resource.getAcquisitionDate() != null ? Date.valueOf(resource.getAcquisitionDate()) : null);
            ps.setString(6, resource.getPhysicalLocation());
            ps.setString(7, resource.getResourceStatus());
            ps.setInt(8, resource.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el recurso ID " + resource.getResourceId(), ex);
        }
    }

    public boolean updateStatus(int resourceId, String status) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return updateStatus(resourceId, status, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el estado del recurso", ex);
        }
    }

    /**
     * Updates the resource status using an externally managed connection.
     */
    public boolean updateStatus(int resourceId, String status, Connection conn) {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el estado del recurso", ex);
        }
    }

    public boolean delete(int resourceId) {
        String sql = "DELETE FROM recursos WHERE id_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el recurso ID " + resourceId, ex);
        }
    }

    public Optional<Resource> findById(int resourceId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return findById(resourceId, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el recurso ID " + resourceId, ex);
        }
    }

    /**
     * Finds a resource using an externally managed connection.
     */
    public Optional<Resource> findById(int resourceId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE r.id_recurso = ?")) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el recurso ID " + resourceId, ex);
        }
    }

    /**
     * Locks a resource row for the duration of a transaction, preventing concurrent loans.
     */
    public Optional<Resource> findByIdForUpdate(int resourceId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE r.id_recurso = ? FOR UPDATE")) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al bloquear el recurso ID " + resourceId + " para préstamo", ex);
        }
    }

    public List<Resource> findAll() {
        return query(SELECT_BASE + "ORDER BY r.nombre_recurso", null);
    }

    public List<Resource> findByStatus(String status) {
        return query(SELECT_BASE + "WHERE r.estado_recurso = ? ORDER BY r.nombre_recurso", status);
    }

    public List<Resource> findByCategory(int categoryId) {
        List<Resource> resources = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE r.id_categoria = ? ORDER BY r.nombre_recurso";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resources.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar recursos por categoría", ex);
        }
        return resources;
    }

    public boolean existsCode(String code) {
        String sql = "SELECT 1 FROM recursos WHERE codigo_recurso = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el código del recurso", ex);
        }
    }

    private List<Resource> query(String sql, String param) {
        List<Resource> resources = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resources.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los recursos", ex);
        }
        return resources;
    }

    private Resource map(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setResourceId(rs.getInt("id_recurso"));
        r.setResourceCode(rs.getString("codigo_recurso"));
        r.setResourceName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        Date date = rs.getDate("fecha_adquisicion");
        r.setAcquisitionDate(date != null ? date.toLocalDate() : null);
        r.setPhysicalLocation(rs.getString("ubicacion_fisica"));
        r.setResourceStatus(rs.getString("estado_recurso"));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }
}
