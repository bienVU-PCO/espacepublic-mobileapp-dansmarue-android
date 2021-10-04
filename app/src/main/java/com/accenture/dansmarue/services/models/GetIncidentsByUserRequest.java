package com.accenture.dansmarue.services.models;

/**
 * Created by PK on 11/05/2017.
 */

public class GetIncidentsByUserRequest extends SiraRequest {

    private static final String SERVICE_NAME = "getIncidentsByUser";

    private String email;
    private String filterIncidentStatus;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFilterIncidentStatus(String filterState) {
        this.filterIncidentStatus = filterState;
    }

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }
}
