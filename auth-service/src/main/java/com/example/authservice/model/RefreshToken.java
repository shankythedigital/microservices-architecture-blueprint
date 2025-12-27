
package com.example.authservice.model;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name="refresh_tokens")
public class RefreshToken extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true) 
    private String tokenHash;
    
    @Column(name ="access_token",nullable=false, columnDefinition = "TEXT") 
    private String accesstoken;
    
    @ManyToOne @JoinColumn(name="session_id") 
    private Session session;
    private LocalDateTime expiryDate;


    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public String getAccessToken(){return accesstoken;}
    public void setAccessToken(String v){this.accesstoken=v;}
    public String getTokenHash(){return tokenHash;}
    public void setTokenHash(String v){this.tokenHash=v;}
    public Session getSession(){return session;}
    public void setSession(Session v){this.session=v;}
    public LocalDateTime getExpiryDate(){return expiryDate;}
    public void setExpiryDate(LocalDateTime v){this.expiryDate=v;}
}




