package com.accenture.dansmarue.services.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by d4v1d on 04/05/2017.
 */

public class SiraSimpleResponse extends SiraResponse {

    private Answer answer;

    private String request;

    @SerializedName("online")
    private boolean isOnline;

    @SerializedName("message_information")
    private String messageInformation;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public String getMessageInformation() {
        return messageInformation;
    }

    public void setMessageInformation(String messageInformation) {
        this.messageInformation = messageInformation;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public class Answer {

        private String status;

        private boolean isAgent;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isAgent() {
            return isAgent;
        }
    }

}
