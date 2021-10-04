package com.accenture.dansmarue.services.models;

public class IsEmailAgentRequest extends SiraRequest{

    public static final String SERVICE_NAME = "isMailAgent";

    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }
}
