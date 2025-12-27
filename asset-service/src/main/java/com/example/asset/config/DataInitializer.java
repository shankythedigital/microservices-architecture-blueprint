
package com.example.asset.config;

import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.service.EntityTypeService;
import com.example.asset.service.StatusService;
import com.example.asset.service.ComplianceRuleService;
import com.example.asset.service.ComplianceMasterDataService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ✅ DataInitializer
 * Seeds base reference data for the Asset microservice if the database is empty.
 * Creates default categories, subcategories, makes, models, outlets, components, entity types, and statuses.
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductCategoryRepository catRepo;
    private final ProductSubCategoryRepository subRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final PurchaseOutletRepository outletRepo;
    private final AssetComponentRepository compRepo;
    private final EntityTypeService entityTypeService;
    private final StatusService statusService;
    private final ComplianceRuleService complianceRuleService;
    private final ComplianceMasterDataService complianceMasterDataService;

    public DataInitializer(ProductCategoryRepository catRepo,
                           ProductSubCategoryRepository subRepo,
                           ProductMakeRepository makeRepo,
                           ProductModelRepository modelRepo,
                           PurchaseOutletRepository outletRepo,
                           AssetComponentRepository compRepo,
                           EntityTypeService entityTypeService,
                           StatusService statusService,
                           ComplianceRuleService complianceRuleService,
                           ComplianceMasterDataService complianceMasterDataService) {
        this.catRepo = catRepo;
        this.subRepo = subRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.outletRepo = outletRepo;
        this.compRepo = compRepo;
        this.entityTypeService = entityTypeService;
        this.statusService = statusService;
        this.complianceRuleService = complianceRuleService;
        this.complianceMasterDataService = complianceMasterDataService;
    }

    // ============================================================
    // 🧩 Initialize Data on Startup
    // ============================================================
    @PostConstruct
    public void init() {
        try {
            log.info("🚀 Initializing Asset Service Reference Data...");

            // ------------------------------------------------------------------------
            // 0️⃣ Entity Types (Must be initialized first)
            // ------------------------------------------------------------------------
            entityTypeService.initializeEntityTypes();
            log.info("✅ Entity Types initialized");

            // ------------------------------------------------------------------------
            // 0️⃣ Status Values (Must be initialized early)
            // ------------------------------------------------------------------------
            statusService.initializeStatuses();
            log.info("✅ Status Values initialized");

            // ------------------------------------------------------------------------
            // 0️⃣ Compliance Master Data (Must be initialized first)
            // ------------------------------------------------------------------------
            complianceMasterDataService.initializeMasterData("system");
            log.info("✅ Compliance Master Data initialized");

            // ------------------------------------------------------------------------
            // 0️⃣ Compliance Rules (Must be initialized after master data)
            // ------------------------------------------------------------------------
            complianceRuleService.initializeDefaultRules("system");
            log.info("✅ Compliance Rules initialized");

            // ------------------------------------------------------------------------
            // 1️⃣ Product Categories
            // ------------------------------------------------------------------------
            if (catRepo.count() == 0) {
                ProductCategory electronics = new ProductCategory("Electronics");
                electronics.setCreatedBy("system");
                electronics.setUpdatedBy("system");

                ProductCategory appliances = new ProductCategory("Home Appliances");
                appliances.setCreatedBy("system");
                appliances.setUpdatedBy("system");

                ProductCategory smartHome = new ProductCategory("Smart Home Devices");
                smartHome.setCreatedBy("system");
                smartHome.setUpdatedBy("system");

                catRepo.saveAll(List.of(electronics, appliances, smartHome));
                log.info("✅ Seeded Product Categories");
            }

            // Reload categories to ensure IDs are populated
            List<ProductCategory> categories = catRepo.findAll();
            ProductCategory electronics = categories.stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase("Electronics"))
                    .findFirst().orElse(null);
            ProductCategory appliances = categories.stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase("Home Appliances"))
                    .findFirst().orElse(null);

            // ------------------------------------------------------------------------
            // 2️⃣ Product SubCategories
            // ------------------------------------------------------------------------
            if (subRepo.count() == 0 && electronics != null && appliances != null) {
                ProductSubCategory phones = new ProductSubCategory("Smartphones", electronics);
                phones.setCreatedBy("system");
                phones.setUpdatedBy("system");

                ProductSubCategory tvs = new ProductSubCategory("Smart TVs", electronics);
                tvs.setCreatedBy("system");
                tvs.setUpdatedBy("system");

                ProductSubCategory fridge = new ProductSubCategory("Refrigerators", appliances);
                fridge.setCreatedBy("system");
                fridge.setUpdatedBy("system");

                ProductSubCategory washing = new ProductSubCategory("Washing Machines", appliances);
                washing.setCreatedBy("system");
                washing.setUpdatedBy("system");

                subRepo.saveAll(List.of(phones, tvs, fridge, washing));
                log.info("✅ Seeded Product SubCategories");
            }

            // ------------------------------------------------------------------------
            // 3️⃣ Product Makes
            // ------------------------------------------------------------------------
            if (makeRepo.count() == 0) {
                List<ProductSubCategory> subs = subRepo.findAll();

                ProductMake samsung = new ProductMake("Samsung", findSub(subs, "Smart TVs"));
                samsung.setCreatedBy("system");
                samsung.setUpdatedBy("system");

                ProductMake lg = new ProductMake("LG", findSub(subs, "Refrigerators"));
                lg.setCreatedBy("system");
                lg.setUpdatedBy("system");

                ProductMake apple = new ProductMake("Apple", findSub(subs, "Smartphones"));
                apple.setCreatedBy("system");
                apple.setUpdatedBy("system");

                makeRepo.saveAll(List.of(samsung, lg, apple));
                log.info("✅ Seeded Product Makes");
            }

            // ------------------------------------------------------------------------
            // 4️⃣ Product Models
            // ------------------------------------------------------------------------
            if (modelRepo.count() == 0) {
                List<ProductMake> makes = makeRepo.findAll();

                ProductModel iphone15 = new ProductModel("iPhone 15 Pro", findMake(makes, "Apple"));
                iphone15.setCreatedBy("system");
                iphone15.setUpdatedBy("system");

                ProductModel samsungQLED = new ProductModel("Samsung QLED 65", findMake(makes, "Samsung"));
                samsungQLED.setCreatedBy("system");
                samsungQLED.setUpdatedBy("system");

                ProductModel lgInstaView = new ProductModel("LG InstaView 260L", findMake(makes, "LG"));
                lgInstaView.setCreatedBy("system");
                lgInstaView.setUpdatedBy("system");

                modelRepo.saveAll(List.of(iphone15, samsungQLED, lgInstaView));
                log.info("✅ Seeded Product Models");
            }

            // ------------------------------------------------------------------------
            // 5️⃣ Purchase Outlets
            // ------------------------------------------------------------------------
            if (outletRepo.count() == 0) {
                PurchaseOutlet amazon = new PurchaseOutlet("Amazon", "Online Portal", "support@amazon.in");
                amazon.setCreatedBy("system");
                amazon.setUpdatedBy("system");

                PurchaseOutlet croma = new PurchaseOutlet("Croma", "Khar West Store", "022-11112222");
                croma.setCreatedBy("system");
                croma.setUpdatedBy("system");

                PurchaseOutlet reliance = new PurchaseOutlet("Reliance Digital", "Andheri East", "022-44443333");
                reliance.setCreatedBy("system");
                reliance.setUpdatedBy("system");

                outletRepo.saveAll(List.of(amazon, croma, reliance));
                log.info("✅ Seeded Purchase Outlets");
            }

            // ------------------------------------------------------------------------
            // 6️⃣ Asset Components
            // ------------------------------------------------------------------------
            if (compRepo.count() == 0) {
                AssetComponent battery = new AssetComponent();
                battery.setComponentName("Battery Pack");
                battery.setDescription("Device rechargeable battery unit");
                battery.setCreatedBy("system");
                battery.setUpdatedBy("system");

                AssetComponent charger = new AssetComponent();
                charger.setComponentName("Charger");
                charger.setDescription("Device adapter or charging cable");
                charger.setCreatedBy("system");
                charger.setUpdatedBy("system");

                AssetComponent remote = new AssetComponent();
                remote.setComponentName("Remote Control");
                remote.setDescription("TV or AC remote controller");
                remote.setCreatedBy("system");
                remote.setUpdatedBy("system");

                compRepo.saveAll(List.of(battery, charger, remote));
                log.info("✅ Seeded Asset Components");
            }

            log.info("🎉 Data initialization completed successfully.");

        } catch (Exception e) {
            log.error("❌ Failed during Data Initialization: {}", e.getMessage(), e);
        }
    }

    // ============================================================
    // 🔧 Helper Methods
    // ============================================================

    private ProductSubCategory findSub(List<ProductSubCategory> subs, String name) {
        return subs.stream()
                .filter(s -> s.getSubCategoryName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private ProductMake findMake(List<ProductMake> makes, String name) {
        return makes.stream()
                .filter(m -> m.getMakeName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

