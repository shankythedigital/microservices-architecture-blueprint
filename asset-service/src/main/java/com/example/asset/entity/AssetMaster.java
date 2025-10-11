package com.example.asset.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;

@Entity
@Table(name = "asset_master")
public class AssetMaster extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    private String assetNameUdv;

    @ManyToOne @JoinColumn(name="category_id")
    private ProductCategory category;

    @ManyToOne @JoinColumn(name="sub_category_id")
    private ProductSubCategory subCategory;

    @ManyToOne @JoinColumn(name="make_id")
    private ProductMake make;

    @ManyToOne @JoinColumn(name="model_id")
    private ProductModel model;

    private String makeUdv;
    private String modelUdv;
    private String purchaseMode;
    @ManyToOne @JoinColumn(name="purchase_outlet_id")
    private PurchaseOutlet purchaseOutlet;
    private String purchaseOutletUdv;
    private String purchaseOutletAddressUdv;
    private Date purchaseDate;
    private String assetStatus;
    private Date soldOnDate;
    private String salesChannelName;
    private Date createdDate;

    @ManyToMany
    @JoinTable(
        name = "asset_component_link",               // ✅ clearer join table name
        joinColumns = @JoinColumn(name = "asset_id"),
        inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    private Set<AssetComponent> components = new HashSet<>();


    public Long getAssetId(){ return assetId; }
    public void setAssetId(Long assetId){ this.assetId = assetId; }

    public String getAssetNameUdv(){ return assetNameUdv; }
    public void setAssetNameUdv(String assetNameUdv){ this.assetNameUdv = assetNameUdv; }

    public ProductCategory getCategory(){ return category; }
    public void setCategory(ProductCategory category){ this.category = category; }

    public ProductSubCategory getSubCategory(){ return subCategory; }
    public void setSubCategory(ProductSubCategory subCategory){ this.subCategory = subCategory; }

    public ProductMake getMake(){ return make; }
    public void setMake(ProductMake make){ this.make = make; }

    public ProductModel getModel(){ return model; }
    public void setModel(ProductModel model){ this.model = model; }

    public String getMakeUdv(){ return makeUdv; }
    public void setMakeUdv(String makeUdv){ this.makeUdv = makeUdv; }

    public String getModelUdv(){ return modelUdv; }
    public void setModelUdv(String modelUdv){ this.modelUdv = modelUdv; }

    public String getPurchaseMode(){ return purchaseMode; }
    public void setPurchaseMode(String purchaseMode){ this.purchaseMode = purchaseMode; }

    public PurchaseOutlet getPurchaseOutlet(){ return purchaseOutlet; }
    public void setPurchaseOutlet(PurchaseOutlet purchaseOutlet){ this.purchaseOutlet = purchaseOutlet; }

    public String getPurchaseOutletUdv(){ return purchaseOutletUdv; }
    public void setPurchaseOutletUdv(String purchaseOutletUdv){ this.purchaseOutletUdv = purchaseOutletUdv; }

    public String getPurchaseOutletAddressUdv(){ return purchaseOutletAddressUdv; }
    public void setPurchaseOutletAddressUdv(String purchaseOutletAddressUdv){ this.purchaseOutletAddressUdv = purchaseOutletAddressUdv; }

    public Date getPurchaseDate(){ return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate){ this.purchaseDate = purchaseDate; }

    public String getAssetStatus(){ return assetStatus; }
    public void setAssetStatus(String assetStatus){ this.assetStatus = assetStatus; }

    public Date getSoldOnDate(){ return soldOnDate; }
    public void setSoldOnDate(Date soldOnDate){ this.soldOnDate = soldOnDate; }

    public String getSalesChannelName(){ return salesChannelName; }
    public void setSalesChannelName(String salesChannelName){ this.salesChannelName = salesChannelName; }

    public Date getCreatedDate(){ return createdDate; }
    public void setCreatedDate(Date createdDate){ this.createdDate = createdDate; }

    public Set<AssetComponent> getComponents(){ return components; }
    public void setComponents(Set<AssetComponent> components){ this.components = components; }
}
