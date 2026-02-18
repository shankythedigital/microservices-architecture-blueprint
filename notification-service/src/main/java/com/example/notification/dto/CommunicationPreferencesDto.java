package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mirrors auth-service communication opt-out response.
 * Used when checking user preferences before sending notifications.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunicationPreferencesDto {

    @JsonProperty("optOutSms")
    private boolean optOutSms;
    @JsonProperty("optOutEmail")
    private boolean optOutEmail;
    @JsonProperty("optOutWhatsapp")
    private boolean optOutWhatsapp;
    @JsonProperty("optOutInapp")
    private boolean optOutInapp;
    @JsonProperty("optOutPush")
    private boolean optOutPush;

    public boolean isOptOutSms() { return optOutSms; }
    public void setOptOutSms(boolean optOutSms) { this.optOutSms = optOutSms; }
    public boolean isOptOutEmail() { return optOutEmail; }
    public void setOptOutEmail(boolean optOutEmail) { this.optOutEmail = optOutEmail; }
    public boolean isOptOutWhatsapp() { return optOutWhatsapp; }
    public void setOptOutWhatsapp(boolean optOutWhatsapp) { this.optOutWhatsapp = optOutWhatsapp; }
    public boolean isOptOutInapp() { return optOutInapp; }
    public void setOptOutInapp(boolean optOutInapp) { this.optOutInapp = optOutInapp; }
    public boolean isOptOutPush() { return optOutPush; }
    public void setOptOutPush(boolean optOutPush) { this.optOutPush = optOutPush; }
}
