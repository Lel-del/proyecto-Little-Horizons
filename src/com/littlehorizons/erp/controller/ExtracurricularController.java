package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDAO;
import com.littlehorizons.erp.dao.ClubMembershipDAO;
import com.littlehorizons.erp.dao.EventDAO;
import com.littlehorizons.erp.dao.EventParticipantDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.util.AppLogger;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic controller for extracurricular clubs and events.
 */
public class ExtracurricularController {

    private static final List<String> VALID_EVENT_TYPES =
            Arrays.asList("Académico", "Cultural", "Deportivo", "Social", "Científico");
    private static final List<String> VALID_PARTICIPATION_ROLES =
            Arrays.asList("Participante", "Competidor", "Organizador", "Asistente");
    private static final List<String> VALID_POSITIONS =
            Arrays.asList("1er Lugar", "2do Lugar", "3er Lugar", "Participante");

    private final ClubDAO clubDAO = new ClubDAO();
    private final ClubMembershipDAO membershipDAO = new ClubMembershipDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final EventParticipantDAO participantDAO = new EventParticipantDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    public int registerClub(Club club) {
        validateClub(club);
        if (clubDAO.existsName(club.getClubName())) {
            throw new IllegalArgumentException("Ya existe un club con el nombre " + club.getClubName());
        }
        int id = clubDAO.insert(club);
        AppLogger.getInstance().logInfo("ExtracurricularController.registerClub", "Club registrado ID " + id);
        return id;
    }

    public boolean updateClub(Club club) {
        validateClub(club);
        if (club.getClubId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un club válido para actualizar.");
        }
        return clubDAO.update(club);
    }

    public boolean deleteClub(int clubId) {
        return clubDAO.delete(clubId);
    }

    public Club findClub(int clubId) {
        return clubDAO.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el club con ID " + clubId));
    }

    public List<Club> listClubs() {
        return clubDAO.findAll();
    }

    public List<Club> listActiveClubs() {
        return clubDAO.findActive();
    }

    /**
     * Enrolls a student in a club validating the maximum capacity.
     */
    public int enrollStudentInClub(int studentId, int clubId, LocalDate joinDate) {
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        Club club = clubDAO.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el club con ID " + clubId));
        if (!club.isActive()) {
            throw new IllegalStateException("El club seleccionado no está activo.");
        }
        if (membershipDAO.existsActiveMembership(studentId, clubId)) {
            throw new IllegalStateException("El estudiante ya está inscrito activamente en este club.");
        }
        int activeMembers = membershipDAO.countActiveMembers(clubId);
        if (activeMembers >= club.getMaxCapacity()) {
            throw new IllegalStateException("El club alcanzó su cupo máximo de " + club.getMaxCapacity() + " estudiantes.");
        }
        ClubMembership membership = new ClubMembership();
        membership.setStudentId(studentId);
        membership.setClubId(clubId);
        membership.setJoinDate(joinDate != null ? joinDate : LocalDate.now());
        membership.setMembershipStatus("Activa");
        int id = membershipDAO.insert(membership);
        AppLogger.getInstance().logInfo("ExtracurricularController.enrollStudentInClub",
                "Membresía " + id + " creada para estudiante " + studentId + " en club " + clubId);
        return id;
    }

    public boolean withdrawStudentFromClub(int membershipId, LocalDate withdrawalDate, String reason) {
        return membershipDAO.withdraw(membershipId, withdrawalDate != null ? withdrawalDate : LocalDate.now(), reason);
    }

    public List<ClubMembership> listMembershipsByClub(int clubId) {
        return membershipDAO.findByClub(clubId);
    }

    public List<ClubMembership> listMembershipsByStudent(int studentId) {
        return membershipDAO.findByStudent(studentId);
    }

    public List<ClubMembership> listAllMemberships() {
        return membershipDAO.findAll();
    }

    public int registerEvent(SchoolEvent event) {
        validateEvent(event);
        int id = eventDAO.insert(event);
        AppLogger.getInstance().logInfo("ExtracurricularController.registerEvent", "Evento registrado ID " + id);
        return id;
    }

    public boolean updateEvent(SchoolEvent event) {
        validateEvent(event);
        if (event.getEventId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un evento válido para actualizar.");
        }
        return eventDAO.update(event);
    }

    public boolean deleteEvent(int eventId) {
        return eventDAO.delete(eventId);
    }

    public SchoolEvent findEvent(int eventId) {
        return eventDAO.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el evento con ID " + eventId));
    }

    public List<SchoolEvent> listEvents() {
        return eventDAO.findAll();
    }

    public List<SchoolEvent> listEventsByType(String type) {
        if (!VALID_EVENT_TYPES.contains(type)) {
            throw new IllegalArgumentException("Tipo de evento inválido. Use: " + VALID_EVENT_TYPES);
        }
        return eventDAO.findByType(type);
    }

    public int registerEventParticipant(int eventId, int studentId, String role, String position, String observations) {
        SchoolEvent event = eventDAO.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el evento con ID " + eventId));
        studentDAO.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante con ID " + studentId));
        if (!VALID_PARTICIPATION_ROLES.contains(role)) {
            throw new IllegalArgumentException("Rol de participación inválido. Use: " + VALID_PARTICIPATION_ROLES);
        }
        if (position != null && !position.trim().isEmpty() && !VALID_POSITIONS.contains(position)) {
            throw new IllegalArgumentException("Posición inválida. Use: " + VALID_POSITIONS);
        }
        if (position != null && !position.trim().isEmpty() && !event.isHasCompetition()) {
            throw new IllegalStateException("El evento seleccionado no tiene competencia registrada.");
        }
        if (participantDAO.exists(eventId, studentId)) {
            throw new IllegalStateException("El estudiante ya está registrado como participante de este evento.");
        }
        EventParticipant participant = new EventParticipant();
        participant.setEventId(eventId);
        participant.setStudentId(studentId);
        participant.setParticipationRole(role);
        participant.setObtainedPosition(position);
        participant.setObservations(observations);
        int id = participantDAO.insert(participant);
        AppLogger.getInstance().logInfo("ExtracurricularController.registerEventParticipant",
                "Participación " + id + " registrada en evento " + eventId);
        return id;
    }

    public boolean updateParticipantPosition(int participationId, String position) {
        if (!VALID_POSITIONS.contains(position)) {
            throw new IllegalArgumentException("Posición inválida. Use: " + VALID_POSITIONS);
        }
        return participantDAO.updatePosition(participationId, position);
    }

    public boolean deleteParticipant(int participationId) {
        return participantDAO.delete(participationId);
    }

    public List<EventParticipant> listParticipantsByEvent(int eventId) {
        return participantDAO.findByEvent(eventId);
    }

    public List<EventParticipant> listEventsByStudent(int studentId) {
        return participantDAO.findByStudent(studentId);
    }

    private void validateClub(Club club) {
        if (club == null) {
            throw new IllegalArgumentException("Los datos del club son obligatorios.");
        }
        if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del club es obligatorio.");
        }
        if (club.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor que cero.");
        }
        if (club.getSupervisorTeacherId() != null) {
            teacherDAO.findById(club.getSupervisorTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("El profesor encargado no existe."));
        }
    }

    private void validateEvent(SchoolEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Los datos del evento son obligatorios.");
        }
        if (event.getEventName() == null || event.getEventName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (event.getEventType() == null || !VALID_EVENT_TYPES.contains(event.getEventType())) {
            throw new IllegalArgumentException("Tipo de evento inválido. Use: " + VALID_EVENT_TYPES);
        }
        if (event.getEventDate() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        }
        if (event.getOrganizerTeacherId() != null) {
            teacherDAO.findById(event.getOrganizerTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("El profesor organizador no existe."));
        }
    }
}
