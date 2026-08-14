package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tenants",
        indices = {
                @Index("phoneNumber"),
                @Index("nidNumber"),
                @Index("status")
        }
)
public class Tenant {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String fullName;
    private String fatherName;
    private String motherName;

    private String phoneNumber;
    private String alternativePhone;
    private String email;

    private String dateOfBirth;
    private String occupation;

    private String nidNumber;
    private String passportNumber;

    private String presentAddress;
    private String permanentAddress;

    private String emergencyContactName;
    private String emergencyContactPhone;

    private int familyMemberCount;

    private String photoPath;
    private String notes;

    private String status; // ACTIVE, INACTIVE, ARCHIVED

    private long createdAt;
    private long updatedAt;

    public Tenant() {
        this.status = "ACTIVE";
    }

    @androidx.room.Ignore
    public Tenant(
            String fullName,
            String fatherName,
            String motherName,
            String phoneNumber,
            String alternativePhone,
            String email,
            String dateOfBirth,
            String occupation,
            String nidNumber,
            String passportNumber,
            String presentAddress,
            String permanentAddress,
            String emergencyContactName,
            String emergencyContactPhone,
            int familyMemberCount,
            String photoPath,
            String notes,
            String status,
            long createdAt,
            long updatedAt
    ) {
        this.fullName = fullName;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.phoneNumber = phoneNumber;
        this.alternativePhone = alternativePhone;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.occupation = occupation;
        this.nidNumber = nidNumber;
        this.passportNumber = passportNumber;
        this.presentAddress = presentAddress;
        this.permanentAddress = permanentAddress;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.familyMemberCount = familyMemberCount;
        this.photoPath = photoPath;
        this.notes = notes;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAlternativePhone() {
        return alternativePhone;
    }

    public void setAlternativePhone(String alternativePhone) {
        this.alternativePhone = alternativePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getNidNumber() {
        return nidNumber;
    }

    public void setNidNumber(String nidNumber) {
        this.nidNumber = nidNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getPresentAddress() {
        return presentAddress;
    }

    public void setPresentAddress(String presentAddress) {
        this.presentAddress = presentAddress;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public int getFamilyMemberCount() {
        return familyMemberCount;
    }

    public void setFamilyMemberCount(int familyMemberCount) {
        this.familyMemberCount = familyMemberCount;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status != null ? status : "ACTIVE";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}