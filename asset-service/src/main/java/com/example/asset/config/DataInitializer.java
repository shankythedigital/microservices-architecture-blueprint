package com.example.asset.config;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class DataInitializer {

    private final ProductCategoryRepository catRepo;
    private final ProductSubCategoryRepository subRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final PurchaseOutletRepository outletRepo;
    private final AssetComponentRepository compRepo;

    public DataInitializer(ProductCategoryRepository catRepo,
                           ProductSubCategoryRepository subRepo,
                           ProductMakeRepository makeRepo,
                           ProductModelRepository modelRepo,
                           PurchaseOutletRepository outletRepo,
                           AssetComponentRepository compRepo) {
        this.catRepo = catRepo;
        this.subRepo = subRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.outletRepo = outletRepo;
        this.compRepo = compRepo;
    }

    @PostConstruct
    public void init() {
        if (catRepo.count() == 0) {
            ProductCategory white = new ProductCategory("White Goods / Major Home Appliances");
            ProductCategory consumer = new ProductCategory("Consumer Electronics");
            ProductCategory smartHome = new ProductCategory("Smart Home & Premium Appliances");
            catRepo.saveAll(List.of(white, consumer, smartHome));

            ProductSubCategory tv = new ProductSubCategory("Television", consumer);
            ProductSubCategory fridge = new ProductSubCategory("Refrigerator", white);
            ProductSubCategory smartphone = new ProductSubCategory("Smartphone", consumer);
            subRepo.saveAll(List.of(tv, fridge, smartphone));

            ProductMake samsung = new ProductMake("Samsung", tv);
            ProductMake lg = new ProductMake("LG", fridge);
            ProductMake apple = new ProductMake("Apple", smartphone);
            ProductMake oneplus = new ProductMake("OnePlus", smartphone);
            makeRepo.saveAll(List.of(samsung, lg, apple, oneplus));

            ProductModel s95 = new ProductModel("S95F OLED 65", samsung);
            ProductModel glTouch = new ProductModel("GL-Touch 260L", lg);
            ProductModel iphone = new ProductModel("iPhone 15 Pro", apple);
            ProductModel oneplus12 = new ProductModel("OnePlus 12", oneplus);
            modelRepo.saveAll(List.of(s95, glTouch, iphone, oneplus12));

            PurchaseOutlet amazon = new PurchaseOutlet("Amazon", "Online portal", "support@amazon.in", "ONLINE");
            PurchaseOutlet croma = new PurchaseOutlet("Croma", "Khar West Store", "022-11112222", "OFFLINE");
            outletRepo.saveAll(List.of(amazon, croma));

            AssetComponent battery = new AssetComponent();
            battery.setComponentName("Battery Pack");
            battery.setDescription("Device battery");

            AssetComponent charger = new AssetComponent();
            charger.setComponentName("Charger");
            charger.setDescription("Charger / Adapter");

            compRepo.saveAll(List.of(battery, charger));
        }
    }
}

