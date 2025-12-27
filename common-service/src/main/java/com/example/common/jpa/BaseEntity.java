package com.example.common.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "active")
    private Boolean active = true;

    public String getCreatedBy(){return createdBy;}
    public void setCreatedBy(String v){this.createdBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public String getUpdatedBy(){return updatedBy;}
    public void setUpdatedBy(String v){this.updatedBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(LocalDateTime v){this.updatedAt=v;}
    public Boolean getActive(){return active;}
    public void setActive(Boolean v){this.active=v;}
}
