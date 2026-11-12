package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Club;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for extracurricular clubs.
 */
public class ClubDAO {

    private static final String SELECT_BASE =
            "SELECT c.*, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_profesor, " +
            "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS total_miembros " +
            "FROM clubes c LEFT JOIN profesores p ON p.id_profesor = c.id_profesor_supervisor ";

    public int insert(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, " +
                "horario_reunion, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getDescription());
            if (club.getSupervisorTeacherId() != null) {
                ps.setInt(3, club.getSupervisorTeacherId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getMeetingSchedule());
            ps.setBoolean(6, club.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    club.setClubId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del club");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el club " + club.getClubName(), ex);
        }
    }

    public boolean update(Club club) {
        String sql = "UPDATE clubes SET nombre_club = ?, descripcion = ?, id_profesor_supervisor = ?, " +
                "capacidad_maxima = ?, horario_reunion = ?, esta_activo = ? WHERE id_club = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getDescription());
            if (club.getSupervisorTeacherId() != null) {
                ps.setInt(3, club.getSupervisorTeacherId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, club.getMaxCapacity());
            ps.setString(5, club.getMeetingSchedule());
            ps.setBoolean(6, club.isActive());
            ps.setInt(7, club.getClubId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el club ID " + club.getClubId(), ex);
        }
    }

    public boolean delete(int clubId) {
        String sql = "DELETE FROM clubes WHERE id_club = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el club ID " + clubId, ex);
        }
    }

    public Optional<Club> findById(int clubId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return findById(clubId, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el club ID " + clubId, ex);
        }
    }

    /**
     * Finds a club using an externally managed connection.
     */
    public Optional<Club> findById(int clubId, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE c.id_club = ?")) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el club ID " + clubId, ex);
        }
    }

    /**
     * Locks the club row so capacity checks and inserts cannot interleave with other enrollments.
     *
     * @return true when the club exists
     */
    public boolean lockById(int clubId, Connection conn) {
        String sql = "SELECT 1 FROM clubes WHERE id_club = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al bloquear el club ID " + clubId, ex);
        }
    }

    public List<Club> findAll() {
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY c.nombre_club");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clubs.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los clubes", ex);
        }
        return clubs;
    }

    public List<Club> findActive() {
        List<Club> clubs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE c.esta_activo = TRUE ORDER BY c.nombre_club");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clubs.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar los clubes activos", ex);
        }
        return clubs;
    }

    public boolean existsName(String name) {
        String sql = "SELECT 1 FROM clubes WHERE nombre_club = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar el nombre del club", ex);
        }
    }

    private Club map(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setClubId(rs.getInt("id_club"));
        c.setClubName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        int supervisorId = rs.getInt("id_profesor_supervisor");
        c.setSupervisorTeacherId(rs.wasNull() ? null : supervisorId);
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setSupervisorName(rs.getString("nombre_profesor"));
        c.setActiveMembersCount(rs.getInt("total_miembros"));
        return c;
    }
}
