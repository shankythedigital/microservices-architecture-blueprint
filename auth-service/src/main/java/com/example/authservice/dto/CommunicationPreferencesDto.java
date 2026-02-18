package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lightweight DTO for communication opt-out preferences (e.g. for notification senders).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunicationPreferencesDto {
    private boolean optOutSms;
    private boolean optOutEmail;
    private boolean optOutWhatsapp;
    private boolean optOutInapp;
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
