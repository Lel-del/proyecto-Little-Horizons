package com.littlehorizons.erp.model;

/**
 * Domain entity representing a student's participation in a school event.
 */
public class EventParticipant {
    private int participationId;
    private int eventId;
    private int studentId;
    private String participationRole;
    private String obtainedPosition;
    private String observations;
    private String eventName;
    private String studentName;

    public EventParticipant() {
    }

    public int getParticipationId() {
        return participationId;
    }

    public void setParticipationId(int participationId) {
        this.participationId = participationId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getParticipationRole() {
        return participationRole;
    }

    public void setParticipationRole(String participationRole) {
        this.participationRole = participationRole;
    }

    public String getObtainedPosition() {
        return obtainedPosition;
    }

    public void setObtainedPosition(String obtainedPosition) {
        this.obtainedPosition = obtainedPosition;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Evento: %s | Est: %s | Rol: %s | Posición: %s",
                participationId,
                eventName != null ? eventName : String.valueOf(eventId),
                studentName != null ? studentName : String.valueOf(studentId),
                participationRole,
                obtainedPosition != null ? obtainedPosition : "Participante");
    }
}
