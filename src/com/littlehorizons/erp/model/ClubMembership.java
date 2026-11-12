package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a student's membership in an extracurricular club.
 */
public class ClubMembership {
    private int membershipId;
    private int studentId;
    private int clubId;
    private LocalDate joinDate;
    private String membershipStatus;
    private LocalDate withdrawalDate;
    private String withdrawalReason;
    private String studentName;
    private String clubName;

    public ClubMembership() {
    }

    public int getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(int membershipId) {
        this.membershipId = membershipId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Est: %s | Club: %s | Ingreso: %s | Estado: %s",
                membershipId,
                studentName != null ? studentName : String.valueOf(studentId),
                clubName != null ? clubName : String.valueOf(clubId),
                joinDate, membershipStatus);
    }
}
