
package com.example.asset.entity;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;


@Entity
@Table(name = "asset_component_master")
public class AssetComponent extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;
    private String componentName;
    private String description;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    public Long getComponentId(){ return componentId; }
    public void setComponentId(Long componentId){ this.componentId = componentId; }
    public String getComponentName(){ return componentName; }
    public void setComponentName(String componentName){ this.componentName = componentName; }
    public String getDescription(){ return description; }
    public void setDescription(String description){ this.description = description; }
    public Integer getSequenceOrder(){ return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder){ this.sequenceOrder = sequenceOrder; }
    public Boolean getIsFavourite(){ return isFavourite; }
    public void setIsFavourite(Boolean isFavourite){ this.isFavourite = isFavourite; }
    public Boolean getIsMostLike(){ return isMostLike; }
    public void setIsMostLike(Boolean isMostLike){ this.isMostLike = isMostLike; }
}
