package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_document")
public class AssetDocument extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne @JoinColumn(name="asset_id")
    private AssetMaster asset;

    @ManyToOne @JoinColumn(name="component_id")
    private AssetComponent component;

    private String docType;
    private String filePath;
    private LocalDateTime uploadedDate = LocalDateTime.now();
    private String userId;
    private String username;

    public Long getDocumentId(){ return documentId; }
    public void setDocumentId(Long documentId){ this.documentId = documentId; }
    public AssetMaster getAsset(){ return asset; }
    public void setAsset(AssetMaster asset){ this.asset = asset; }
    public AssetComponent getComponent(){ return component; }
    public void setComponent(AssetComponent component){ this.component = component; }
    public String getDocType(){ return docType; }
    public void setDocType(String docType){ this.docType = docType; }
    public String getFilePath(){ return filePath; }
    public void setFilePath(String filePath){ this.filePath = filePath; }
    public LocalDateTime getUploadedDate(){ return uploadedDate; }
    public void setUploadedDate(LocalDateTime uploadedDate){ this.uploadedDate = uploadedDate; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
}
