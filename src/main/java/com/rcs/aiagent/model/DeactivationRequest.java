package com.rcs.aiagent.model;

import java.util.List;

public class DeactivationRequest {

    private String email;
    private List<String> systems;
    private String action;

    public DeactivationRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getSystems() {
        return systems;
    }

    public void setSystems(List<String> systems) {
        this.systems = systems;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}