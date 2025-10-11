
package com.example.authservice.model;

import jakarta.persistence.*;

@Entity
@Table(name="credentials")
public class Credential extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable=false)
    private String type; // RSA, WEBAUTHN, MPIN, etc.

    @Column(nullable=false, length=4096)
    private String publicKey; // PEM for RSA, base64 for WebAuthn, or hashed mpin

    @Column(nullable=false, unique=true)
    private String credentialId; // for WebAuthn credential IDs, RSA key fingerprint, or mpin-id

    @Column(name="metadata", length=1024)
    private String metadata; // optional device info, etc.

    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public User getUser(){ return user; }
    public void setUser(User user){ this.user = user; }

    public String getType(){ return type; }
    public void setType(String type){ this.type = type; }

    public String getPublicKey(){ return publicKey; }
    public void setPublicKey(String publicKey){ this.publicKey = publicKey; }

    public String getCredentialId(){ return credentialId; }
    public void setCredentialId(String credentialId){ this.credentialId = credentialId; }

    public String getMetadata(){ return metadata; }
    public void setMetadata(String metadata){ this.metadata = metadata; }
}



