package com.votify.frontend.dto;

import java.util.List;

public class ParticipantRequest {
    private String teamName;
    private String email;
    private String phone;
    private String description;
    private String logo;
    private List<String> members;
    private String ownerEmail;

    public ParticipantRequest() {
    }

    public ParticipantRequest(String teamName, String email, String phone, String description, String logo, List<String> members, String ownerEmail) {
        this.teamName = teamName;
        this.email = email;
        this.phone = phone;
        this.description = description;
        this.logo = logo;
        this.members = members;
        this.ownerEmail = ownerEmail;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
