package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * UserProfileResponse DTO for returning user profile information
 * Includes all profile fields: photo, social media, demographic, behaviors, and additional info
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long userId;
    private String username;
    private String email;
    private String mobile;
    private String employeeId;
    private String firstName;
    private String lastName;

    // Address Information
    private String pincode;
    private String city;
    private String state;
    private String country;
    private String countryCode;
    private String address1;
    private String address2;
    private String address3;

    // Profile Photo
    private String profilePhotoUrl;

    // Social Media Links
    private String linkedinUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String githubUrl;
    private String websiteUrl;

    // Demographic Information
    private String dateOfBirth;
    private String gender;
    private String occupation;
    private String education;
    private String maritalStatus;

    // Behaviors & Preferences
    private String preferences;
    private String activityPatterns;
    private String interests;

    // Additional Information
    private String bio;
    private String skills;
    private String languages;
    private String timezone;
    private String additionalInfo;

    // Account Information
    private LocalDateTime lastLoginDate;
    private Boolean accountLocked;
    private Boolean acceptTc;

    // Communication opt-out (true = user opted out of that channel)
    private Boolean optOutSms;
    private Boolean optOutEmail;
    private Boolean optOutWhatsapp;
    private Boolean optOutInapp;
    private Boolean optOutPush;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }
    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }
    public String getAddress3() { return address3; }
    public void setAddress3(String address3) { this.address3 = address3; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }
    public String getTwitterUrl() { return twitterUrl; }
    public void setTwitterUrl(String twitterUrl) { this.twitterUrl = twitterUrl; }
    public String getInstagramUrl() { return instagramUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    public String getActivityPatterns() { return activityPatterns; }
    public void setActivityPatterns(String activityPatterns) { this.activityPatterns = activityPatterns; }
    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    public Boolean getAcceptTc() { return acceptTc; }
    public void setAcceptTc(Boolean acceptTc) { this.acceptTc = acceptTc; }
    public Boolean getOptOutSms() { return optOutSms; }
    public void setOptOutSms(Boolean optOutSms) { this.optOutSms = optOutSms; }
    public Boolean getOptOutEmail() { return optOutEmail; }
    public void setOptOutEmail(Boolean optOutEmail) { this.optOutEmail = optOutEmail; }
    public Boolean getOptOutWhatsapp() { return optOutWhatsapp; }
    public void setOptOutWhatsapp(Boolean optOutWhatsapp) { this.optOutWhatsapp = optOutWhatsapp; }
    public Boolean getOptOutInapp() { return optOutInapp; }
    public void setOptOutInapp(Boolean optOutInapp) { this.optOutInapp = optOutInapp; }
    public Boolean getOptOutPush() { return optOutPush; }
    public void setOptOutPush(Boolean optOutPush) { this.optOutPush = optOutPush; }
}

