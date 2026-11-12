package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.EventParticipant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for event participants.
 */
public class EventParticipantDAO {

    private static final String SELECT_BASE =
            "SELECT pe.*, ev.nombre_evento, " +
            "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante " +
            "FROM participantes_evento pe " +
            "JOIN eventos ev ON ev.id_evento = pe.id_evento " +
            "JOIN estudiantes e ON e.id_estudiante = pe.id_estudiante ";

    public int insert(EventParticipant participant) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, " +
                "posicion_obtenida, observaciones) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, participant.getEventId());
            ps.setInt(2, participant.getStudentId());
            ps.setString(3, participant.getParticipationRole());
            ps.setString(4, participant.getObtainedPosition());
            ps.setString(5, participant.getObservations());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    participant.setParticipationId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador del participante");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar la participación en el evento", ex);
        }
    }

    public boolean updatePosition(int participationId, String position) {
        String sql = "UPDATE participantes_evento SET posicion_obtenida = ? WHERE id_participacion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, position);
            ps.setInt(2, participationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al actualizar la posición obtenida", ex);
        }
    }

    public boolean delete(int participationId) {
        String sql = "DELETE FROM participantes_evento WHERE id_participacion = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, participationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la participación", ex);
        }
    }

    public List<EventParticipant> findByEvent(int eventId) {
        String sql = SELECT_BASE + "WHERE pe.id_evento = ? ORDER BY pe.posicion_obtenida, e.primer_apellido";
        List<EventParticipant> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar participantes del evento", ex);
        }
        return list;
    }

    public List<EventParticipant> findByStudent(int studentId) {
        String sql = SELECT_BASE + "WHERE pe.id_estudiante = ? ORDER BY ev.fecha_evento DESC";
        List<EventParticipant> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar eventos del estudiante", ex);
        }
        return list;
    }

    public boolean exists(int eventId, int studentId) {
        String sql = "SELECT 1 FROM participantes_evento WHERE id_evento = ? AND id_estudiante = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la participación en el evento", ex);
        }
    }

    private EventParticipant map(ResultSet rs) throws SQLException {
        EventParticipant p = new EventParticipant();
        p.setParticipationId(rs.getInt("id_participacion"));
        p.setEventId(rs.getInt("id_evento"));
        p.setStudentId(rs.getInt("id_estudiante"));
        p.setParticipationRole(rs.getString("rol_participacion"));
        p.setObtainedPosition(rs.getString("posicion_obtenida"));
        p.setObservations(rs.getString("observaciones"));
        p.setEventName(rs.getString("nombre_evento"));
        p.setStudentName(rs.getString("nombre_estudiante"));
        return p;
    }
}
