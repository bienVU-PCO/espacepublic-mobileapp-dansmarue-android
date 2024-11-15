package com.accenture.dansmarue.services.models;

import com.accenture.dansmarue.mvp.models.Position;

/**
 * Created by PK on 11/04/2017.
 */

public class GetIncidentsByPositionRequest extends SiraRequest {

    private static final String SERVICE_NAME = "getIncidentsByPosition";

    private String guid;

    private Position position;

    private String searchByNumber;


    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getSearchByNumber() {return searchByNumber;}

    public void setSearchByNumber(String searchByNumber) { this.searchByNumber = searchByNumber;}


}
