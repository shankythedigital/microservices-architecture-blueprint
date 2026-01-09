package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;

/**
 * UserProfileRequest DTO for updating user profile information
 * Includes: photo, social media links, demographic info, behaviors, and additional info
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileRequest {

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
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth; // YYYY-MM-DD format

    private String gender; // MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY

    private String occupation;
    private String education; // HIGH_SCHOOL, BACHELORS, MASTERS, PHD, etc.
    private String maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED, etc.

    // Behaviors & Preferences
    private String preferences; // JSON string for user preferences
    private String activityPatterns; // JSON string for activity patterns
    private String interests; // JSON array string for interests/hobbies

    // Additional Information
    private String bio; // User biography/description
    private String skills; // JSON array string for skills
    private String languages; // JSON array string for languages
    private String timezone; // User's timezone
    private String additionalInfo; // JSON string for additional information

    // Getters and Setters
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
}

