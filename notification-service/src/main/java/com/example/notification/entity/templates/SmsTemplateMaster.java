package com.example.notification.entity.templates;

import com.example.notification.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sms_template_master")
public class SmsTemplateMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", unique = true, nullable = false)
    private String templateCode;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "JSON")
    private String placeholders;

    private Boolean active;
    private String projectType;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getPlaceholders() { return placeholders; }
    public void setPlaceholders(String placeholders) { this.placeholders = placeholders; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
}
