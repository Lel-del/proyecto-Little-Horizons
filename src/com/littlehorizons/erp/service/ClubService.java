package com.littlehorizons.erp.service;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.dao.ClubDAO;
import com.littlehorizons.erp.dao.ClubMembershipDAO;
import com.littlehorizons.erp.dao.DatabaseException;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Transactional service for club enrollment, guaranteeing the capacity check and the
 * membership insert cannot be interleaved by concurrent requests.
 */
public class ClubService {

    private final ClubDAO clubDAO = new ClubDAO();
    private final ClubMembershipDAO membershipDAO = new ClubMembershipDAO();

    /**
     * Enrolls a student in a club, locking the club row during the capacity validation.
     */
    public int enrollStudent(int studentId, int clubId, LocalDate joinDate) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (!clubDAO.lockById(clubId, conn)) {
                    throw new IllegalArgumentException("No existe el club con ID " + clubId);
                }
                Club club = clubDAO.findById(clubId, conn)
                        .orElseThrow(() -> new IllegalArgumentException("No existe el club con ID " + clubId));
                if (!club.isActive()) {
                    throw new IllegalStateException("El club seleccionado no está activo.");
                }
                if (membershipDAO.existsActiveMembership(studentId, clubId, conn)) {
                    throw new IllegalStateException("El estudiante ya está inscrito activamente en este club.");
                }
                int activeMembers = membershipDAO.countActiveMembers(clubId, conn);
                if (activeMembers >= club.getMaxCapacity()) {
                    throw new IllegalStateException("El club alcanzó su cupo máximo de "
                            + club.getMaxCapacity() + " estudiantes.");
                }
                ClubMembership membership = new ClubMembership();
                membership.setStudentId(studentId);
                membership.setClubId(clubId);
                membership.setJoinDate(joinDate != null ? joinDate : LocalDate.now());
                membership.setMembershipStatus("Activa");
                int membershipId = membershipDAO.insert(membership, conn);
                conn.commit();
                return membershipId;
            } catch (RuntimeException ex) {
                rollback(conn);
                throw ex;
            } catch (SQLException ex) {
                rollback(conn);
                throw new DatabaseException("Error al confirmar la inscripción en el club", ex);
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Error al abrir la conexión para inscribir en el club", ex);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            AppLogger.getInstance().logError("ClubService.rollback",
                    "No fue posible revertir la transacción de la membresía", ex);
        }
    }

    private void restoreAutoCommit(Connection conn, boolean originalAutoCommit) {
        try {
            conn.setAutoCommit(originalAutoCommit);
        } catch (SQLException ex) {
            AppLogger.getInstance().logWarning("ClubService.restoreAutoCommit",
                    "No fue posible restaurar el modo autocommit: " + ex.getMessage());
        }
    }
}
