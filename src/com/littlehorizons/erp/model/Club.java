package com.littlehorizons.erp.model;

/**
 * Domain entity representing an extracurricular school club.
 */
public class Club {
    private int clubId;
    private String clubName;
    private String description;
    private Integer supervisorTeacherId;
    private int maxCapacity;
    private String meetingSchedule;
    private boolean active;
    private String supervisorName;
    private int activeMembersCount;

    public Club() {
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSupervisorTeacherId() {
        return supervisorTeacherId;
    }

    public void setSupervisorTeacherId(Integer supervisorTeacherId) {
        this.supervisorTeacherId = supervisorTeacherId;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public int getActiveMembersCount() {
        return activeMembersCount;
    }

    public void setActiveMembersCount(int activeMembersCount) {
        this.activeMembersCount = activeMembersCount;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | Cupos: %d/%d | Encargado: %s | Horario: %s | Activo: %s",
                clubId, clubName, activeMembersCount, maxCapacity,
                supervisorName != null ? supervisorName : "Sin asignar",
                meetingSchedule != null ? meetingSchedule : "-",
                active ? "Sí" : "No");
    }
}
