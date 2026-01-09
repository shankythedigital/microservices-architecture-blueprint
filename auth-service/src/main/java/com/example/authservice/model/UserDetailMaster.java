package com.example.authservice.model;

import com.example.common.converter.JpaAttributeEncryptor;
import com.example.common.util.HmacUtil;
import jakarta.persistence.*;

import com.example.common.jpa.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_detail_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username_hash"}),
        @UniqueConstraint(columnNames = {"email_hash"}),
        @UniqueConstraint(columnNames = {"mobile_hash"})
    }
)
public class UserDetailMaster extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Encrypted + HMAC username
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", nullable = false, length = 2048)
    private String username;

    @Column(name = "username_hash", nullable = false, unique = true, length = 512)
    private String usernameHash;

    // Encrypted + HMAC email
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "email_enc", length = 2048)
    private String email;

    @Column(name = "email_hash", unique = true, length = 512)
    private String emailHash;

    // Encrypted + HMAC mobile
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    @Column(name = "mobile_hash", unique = true, length = 512)
    private String mobileHash;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "employee_id_enc", length = 1024)
    private String employeeId;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "login_retry")
    private Integer loginRetry = 0;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    // Address fields
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "pincode_enc", length = 1024)
    private String pincode;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "city_enc", length = 1024)
    private String city;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "state_enc", length = 1024)
    private String state;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "country_enc", length = 1024)
    private String country;

    // Terms & Conditions acceptance
    @Column(name = "accept_tc")
    private Boolean acceptTc = false;

    // Country code for mobile validation
    @Column(name = "country_code", length = 10)
    private String countryCode;

    // ============================================================
    // 📸 PROFILE PHOTO
    // ============================================================
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    // ============================================================
    // 🔗 SOCIAL MEDIA LINKS
    // ============================================================
    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "twitter_url", length = 500)
    private String twitterUrl;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    // ============================================================
    // 👤 DEMOGRAPHIC INFORMATION
    // ============================================================
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "date_of_birth_enc", length = 2048)
    private String dateOfBirth; // Stored as encrypted string (YYYY-MM-DD format)

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "gender_enc", length = 2048)
    private String gender; // MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "occupation_enc", length = 2048)
    private String occupation;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "education_enc", length = 2048)
    private String education; // HIGH_SCHOOL, BACHELORS, MASTERS, PHD, etc.

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "marital_status_enc", length = 2048)
    private String maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED, etc.

    // ============================================================
    // 🎯 BEHAVIORS & PREFERENCES
    // ============================================================
    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences; // JSON string for user preferences (interests, notification preferences, etc.)

    @Column(name = "activity_patterns", columnDefinition = "TEXT")
    private String activityPatterns; // JSON string for activity patterns (login frequency, preferred times, etc.)

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests; // JSON array string for user interests/hobbies

    // ============================================================
    // 📝 ADDITIONAL INFORMATION
    // ============================================================
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio; // User biography/description

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // JSON array string for user skills

    @Column(name = "languages", columnDefinition = "TEXT")
    private String languages; // JSON array string for languages spoken (e.g., ["English", "Spanish"])

    @Column(name = "timezone", length = 100)
    private String timezone; // User's timezone (e.g., "America/New_York", "Asia/Kolkata")

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo; // JSON string for any other additional information

    // ✅ Auto-compute HMAC values before insert/update
    @PrePersist
    @PreUpdate
    public void computeHashes() {
        if (this.username != null) this.usernameHash = HmacUtil.hmacHex(this.username);
        if (this.email != null) this.emailHash = HmacUtil.hmacHex(this.email);
        if (this.mobile != null) this.mobileHash = HmacUtil.hmacHex(this.mobile);
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUsernameHash() { return usernameHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailHash() { return emailHash; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getMobileHash() { return mobileHash; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDateTime loginDate) { this.loginDate = loginDate; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public Integer getLoginRetry() { return loginRetry; }
    public void setLoginRetry(Integer loginRetry) { this.loginRetry = loginRetry; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getAcceptTc() { return acceptTc; }
    public void setAcceptTc(Boolean acceptTc) { this.acceptTc = acceptTc; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    // Profile Photo
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    // Social Media Links
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

    // Demographic Information
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

    // Behaviors & Preferences
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public String getActivityPatterns() { return activityPatterns; }
    public void setActivityPatterns(String activityPatterns) { this.activityPatterns = activityPatterns; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    // Additional Information
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

