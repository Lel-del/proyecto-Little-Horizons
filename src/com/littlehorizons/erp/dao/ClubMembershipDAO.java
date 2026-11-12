package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.ClubMembership;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for club memberships.
 */
public class ClubMembershipDAO {

    private static final String SELECT_BASE =
            "SELECT mc.*, CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, " +
            "c.nombre_club FROM membresias_club mc " +
            "JOIN estudiantes e ON e.id_estudiante = mc.id_estudiante " +
            "JOIN clubes c ON c.id_club = mc.id_club ";

    public int insert(ClubMembership membership) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return insert(membership, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al inscribir al estudiante en el club", ex);
        }
    }

    /**
     * Inserts a membership using an externally managed connection (transaction-aware).
     */
    public int insert(ClubMembership membership, Connection conn) {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, membership.getStudentId());
            ps.setInt(2, membership.getClubId());
            ps.setDate(3, Date.valueOf(membership.getJoinDate()));
            ps.setString(4, membership.getMembershipStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    membership.setMembershipId(id);
                    return id;
                }
            }
            throw new DatabaseException("No se generó el identificador de la membresía");
        } catch (SQLException ex) {
            throw new DatabaseException("Error al inscribir al estudiante en el club", ex);
        }
    }

    public boolean withdraw(int membershipId, java.time.LocalDate withdrawalDate, String reason) {
        String sql = "UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = ?, motivo_retiro = ? " +
                "WHERE id_membresia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(withdrawalDate));
            ps.setString(2, reason);
            ps.setInt(3, membershipId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al registrar el retiro de la membresía", ex);
        }
    }

    public boolean delete(int membershipId) {
        String sql = "DELETE FROM membresias_club WHERE id_membresia = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, membershipId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DatabaseException("Error al eliminar la membresía", ex);
        }
    }

    public List<ClubMembership> findByClub(int clubId) {
        return query(SELECT_BASE + "WHERE mc.id_club = ? ORDER BY e.primer_apellido", clubId);
    }

    public List<ClubMembership> findByStudent(int studentId) {
        return query(SELECT_BASE + "WHERE mc.id_estudiante = ? ORDER BY c.nombre_club", studentId);
    }

    public List<ClubMembership> findAll() {
        List<ClubMembership> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY c.nombre_club, e.primer_apellido");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al listar las membresías", ex);
        }
        return list;
    }

    public boolean existsActiveMembership(int studentId, int clubId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return existsActiveMembership(studentId, clubId, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la membresía activa", ex);
        }
    }

    /**
     * Checks an active membership using an externally managed connection.
     */
    public boolean existsActiveMembership(int studentId, int clubId, Connection conn) {
        String sql = "SELECT 1 FROM membresias_club WHERE id_estudiante = ? AND id_club = ? AND estado_membresia = 'Activa'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al validar la membresía activa", ex);
        }
    }

    public int countActiveMembers(int clubId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return countActiveMembers(clubId, conn);
        } catch (SQLException ex) {
            throw new DatabaseException("Error al contar miembros del club", ex);
        }
    }

    /**
     * Counts active members using an externally managed connection.
     */
    public int countActiveMembers(int clubId, Connection conn) {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al contar miembros del club", ex);
        }
    }

    private List<ClubMembership> query(String sql, int param) {
        List<ClubMembership> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al consultar membresías", ex);
        }
        return list;
    }

    private ClubMembership map(ResultSet rs) throws SQLException {
        ClubMembership m = new ClubMembership();
        m.setMembershipId(rs.getInt("id_membresia"));
        m.setStudentId(rs.getInt("id_estudiante"));
        m.setClubId(rs.getInt("id_club"));
        Date joinDate = rs.getDate("fecha_ingreso");
        m.setJoinDate(joinDate != null ? joinDate.toLocalDate() : null);
        m.setMembershipStatus(rs.getString("estado_membresia"));
        Date withdrawal = rs.getDate("fecha_retiro");
        m.setWithdrawalDate(withdrawal != null ? withdrawal.toLocalDate() : null);
        m.setWithdrawalReason(rs.getString("motivo_retiro"));
        m.setStudentName(rs.getString("nombre_estudiante"));
        m.setClubName(rs.getString("nombre_club"));
        return m;
    }
}
