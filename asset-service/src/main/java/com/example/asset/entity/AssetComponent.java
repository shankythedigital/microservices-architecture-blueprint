package com.example.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "asset_component_master")
public class AssetComponent extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;
    private String componentName;
    private String description;

    public Long getComponentId(){ return componentId; }
    public void setComponentId(Long componentId){ this.componentId = componentId; }
    public String getComponentName(){ return componentName; }
    public void setComponentName(String componentName){ this.componentName = componentName; }
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
}
