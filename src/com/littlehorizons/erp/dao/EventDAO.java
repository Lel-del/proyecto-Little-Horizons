package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.SchoolEvent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for school events.
 */
public class EventDAO {

    private static final String SELECT_BASE =
            "SELECT ev.*, CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_organizador " +
            "FROM eventos ev LEFT JOIN profesores p ON p.id_profesor = ev.id_organizador ";

    public int insert(SchoolEvent event) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, " +
                "tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getEventName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setString(5, event.getLocation());
            ps.setBoolean(6, event.isHasCompetition());
            if (event.getOrganizerTeacherId() != null) {
                ps.setInt(7, event.getOrganizerTeacherId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, event.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    event.setEventId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del evento");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el evento " + event.getEventName(), ex);
        }
    }

    public boolean update(SchoolEvent event) {
        String sql = "UPDATE eventos SET nombre_evento = ?, descripcion = ?, tipo_evento = ?, fecha_evento = ?, " +
                "lugar = ?, tiene_competencia = ?, id_organizador = ?, esta_activo = ? WHERE id_evento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getEventType());
            ps.setDate(4, Date.valueOf(event.getEventDate()));
            ps.setString(5, event.getLocation());
            ps.setBoolean(6, event.isHasCompetition());
            if (event.getOrganizerTeacherId() != null) {
                ps.setInt(7, event.getOrganizerTeacherId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setBoolean(8, event.isActive());
            ps.setInt(9, event.getEventId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar el evento ID " + event.getEventId(), ex);
        }
    }

    public boolean delete(int eventId) {
        String sql = "DELETE FROM eventos WHERE id_evento = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar el evento ID " + eventId, ex);
        }
    }

    public Optional<SchoolEvent> findById(int eventId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE ev.id_evento = ?")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar el evento ID " + eventId, ex);
        }
    }

    public List<SchoolEvent> findAll() {
        return query(SELECT_BASE + "ORDER BY ev.fecha_evento DESC");
    }

    public List<SchoolEvent> findByType(String type) {
        String sql = SELECT_BASE + "WHERE ev.tipo_evento = ? ORDER BY ev.fecha_evento DESC";
        List<SchoolEvent> events = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar eventos por tipo", ex);
        }
        return events;
    }

    private List<SchoolEvent> query(String sql) {
        List<SchoolEvent> events = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar eventos", ex);
        }
        return events;
    }

    private SchoolEvent map(ResultSet rs) throws SQLException {
        SchoolEvent e = new SchoolEvent();
        e.setEventId(rs.getInt("id_evento"));
        e.setEventName(rs.getString("nombre_evento"));
        e.setDescription(rs.getString("descripcion"));
        e.setEventType(rs.getString("tipo_evento"));
        Date date = rs.getDate("fecha_evento");
        e.setEventDate(date != null ? date.toLocalDate() : null);
        e.setLocation(rs.getString("lugar"));
        e.setHasCompetition(rs.getBoolean("tiene_competencia"));
        int organizerId = rs.getInt("id_organizador");
        e.setOrganizerTeacherId(rs.wasNull() ? null : organizerId);
        e.setActive(rs.getBoolean("esta_activo"));
        e.setOrganizerName(rs.getString("nombre_organizador"));
        return e;
    }
}
