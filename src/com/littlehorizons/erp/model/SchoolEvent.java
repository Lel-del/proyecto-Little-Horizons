package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a school extracurricular event.
 */
public class SchoolEvent {
    private int eventId;
    private String eventName;
    private String description;
    private String eventType;
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition;
    private Integer organizerTeacherId;
    private boolean active;
    private String organizerName;

    public SchoolEvent() {
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isHasCompetition() {
        return hasCompetition;
    }

    public void setHasCompetition(boolean hasCompetition) {
        this.hasCompetition = hasCompetition;
    }

    public Integer getOrganizerTeacherId() {
        return organizerTeacherId;
    }

    public void setOrganizerTeacherId(Integer organizerTeacherId) {
        this.organizerTeacherId = organizerTeacherId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | Tipo: %s | Fecha: %s | Lugar: %s | Competencia: %s",
                eventId, eventName, eventType, eventDate,
                location != null ? location : "-", hasCompetition ? "Sí" : "No");
    }
}
