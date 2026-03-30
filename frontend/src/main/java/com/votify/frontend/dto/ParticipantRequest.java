package com.votify.frontend.dto;

import java.util.List;

public class ParticipantRequest {
    private String teamName;
    private String email;
    private String address;
    private String phone;
    private List<String> members;

    public ParticipantRequest() {
    }

    public ParticipantRequest(String teamName, String email, String address, String phone, List<String> members) {
        this.teamName = teamName;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.members = members;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
