package com.example.notification.config;

import com.example.notification.entity.templates.*;
import com.example.notification.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer {

    private final NotificationTemplateRepository notificationRepo;
    private final SmsTemplateRepository smsRepo;
    private final WhatsappTemplateRepository waRepo;
    private final InappTemplateRepository inappRepo;

    public DataInitializer(NotificationTemplateRepository notificationRepo,
                           SmsTemplateRepository smsRepo,
                           WhatsappTemplateRepository waRepo,
                           InappTemplateRepository inappRepo) {
        this.notificationRepo = notificationRepo;
        this.smsRepo = smsRepo;
        this.waRepo = waRepo;
        this.inappRepo = inappRepo;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        if (isEmpty()) {
            System.out.println("🚀 Seeding Notification Templates (ASSET_MGMT + ECOM)...");

            // ------------------- SMS Templates -------------------
            smsRepo.saveAll(List.of(
                sms("ASSET_ASSIGN_SMS", "Asset Assignment", "Asset {{assetId}} has been assigned to you.",
                        "{\"assetId\":\"Asset Identifier\"}", "ASSET_MGMT"),
                sms("ASSET_RETURN_SMS", "Asset Return", "Return logged for asset {{assetId}}.",
                        "{\"assetId\":\"Asset Identifier\"}", "ASSET_MGMT"),
                sms("ASSET_MAINT_SMS", "Maintenance Alert",
                        "Maintenance scheduled for asset {{assetId}} on {{date}}.",
                        "{\"assetId\":\"Asset Identifier\",\"date\":\"Maintenance Date\"}", "ASSET_MGMT"),
                sms("ASSET_ERROR_SMS", "Asset Error", "Asset {{assetId}} error: {{errorCode}}.",
                        "{\"assetId\":\"Asset Identifier\",\"errorCode\":\"Error Code\"}", "ASSET_MGMT"),
                sms("OTP_SMS", "OTP Verification", "Your OTP is {{otp}}. Do not share it with anyone.",
                        "{\"otp\":\"One-Time Password\"}", "ECOM"),
                sms("ORDER_CONFIRM_SMS", "Order Confirmation", "Your order {{orderId}} has been confirmed.",
                        "{\"orderId\":\"Order ID\"}", "ECOM"),
                sms("SHIPMENT_SMS", "Shipment Update", "Your order {{orderId}} has been shipped.",
                        "{\"orderId\":\"Order ID\"}", "ECOM"),
                sms("DELIVERY_SMS", "Delivery Notification", "Your order {{orderId}} has been delivered.",
                        "{\"orderId\":\"Order ID\"}", "ECOM"),
                sms("ERROR_SMS", "Error Alert", "System error occurred: {{errorCode}}",
                        "{\"errorCode\":\"Error Code\"}", "ECOM")
            ));

            // ------------------- WhatsApp Templates -------------------
            waRepo.saveAll(List.of(
                wa("ASSET_ASSIGN_WA", "Asset Assignment", "Asset Assigned",
                        "📌 Asset {{assetId}} has been assigned to you, {{name}}.",
                        "{\"assetId\":\"Asset Identifier\",\"name\":\"Employee Name\"}", "ASSET_MGMT"),
                wa("ASSET_RETURN_WA", "Asset Return", "Asset Returned",
                        "↩️ Asset {{assetId}} returned successfully by {{name}}.",
                        "{\"assetId\":\"Asset Identifier\",\"name\":\"Employee Name\"}", "ASSET_MGMT"),
                wa("ASSET_MAINT_WA", "Maintenance Alert", "Asset Maintenance Scheduled",
                        "⚙️ Asset {{assetId}} scheduled for maintenance on {{date}}.",
                        "{\"assetId\":\"Asset Identifier\",\"date\":\"Maintenance Date\"}", "ASSET_MGMT"),
                wa("ASSET_ERROR_WA", "Asset Error", "Asset Error Notification",
                        "⚠️ Asset {{assetId}} error {{errorCode}} at {{timestamp}}.",
                        "{\"assetId\":\"Asset Identifier\",\"errorCode\":\"Error Code\",\"timestamp\":\"Error Time\"}", "ASSET_MGMT"),
                wa("WELCOME_WA", "Welcome WhatsApp", "Welcome",
                        "👋 Hi {{name}}, welcome to Our Store!",
                        "{\"name\":\"Customer Name\"}", "ECOM"),
                wa("ORDER_CONFIRM_WA", "Order Confirmation", "Order Confirmed",
                        "✅ Order {{orderId}} confirmed for {{name}}.",
                        "{\"orderId\":\"Order ID\",\"name\":\"Customer Name\"}", "ECOM"),
                wa("SHIPMENT_WA", "Shipment Update", "Order Shipped",
                        "📦 Order {{orderId}} has been shipped. Track here: {{trackingLink}}",
                        "{\"orderId\":\"Order ID\",\"trackingLink\":\"Tracking URL\"}", "ECOM"),
                wa("DELIVERY_WA", "Delivery Notification", "Order Delivered",
                        "🎉 Order {{orderId}} delivered successfully.",
                        "{\"orderId\":\"Order ID\"}", "ECOM"),
                wa("ALERT_WA", "System Alert", "System Alert",
                        "⚠️ Alert: {{alertMessage}}",
                        "{\"alertMessage\":\"Alert Details\"}", "ECOM"),
                wa("OTP_WA", "OTP Verification", "OTP Verification",
                        "Your OTP is {{otp}}. Do not share it with anyone.",
                        "{\"otp\":\"One-Time Password\"}", "ECOM")
            ));

            // ------------------- Notification (Email) Templates -------------------
            notificationRepo.saveAll(List.of(
                email("ASSET_ASSIGN_EMAIL", "Asset Assignment", "Asset Assigned: {{assetId}}",
                        "Hello {{name}}, asset {{assetId}} has been assigned to you.",
                        "{\"name\":\"Employee Name\",\"assetId\":\"Asset Identifier\"}", "ASSET_MGMT"),
                email("ASSET_RETURN_EMAIL", "Asset Return", "Asset {{assetId}} Returned",
                        "Hi {{name}}, your return for asset {{assetId}} has been logged.",
                        "{\"name\":\"Employee Name\",\"assetId\":\"Asset Identifier\"}", "ASSET_MGMT"),
                email("ASSET_MAINT_EMAIL", "Maintenance Alert", "Maintenance Scheduled for Asset {{assetId}}",
                        "Asset {{assetId}} is scheduled for maintenance on {{date}}.",
                        "{\"assetId\":\"Asset Identifier\",\"date\":\"Maintenance Date\"}", "ASSET_MGMT"),
                email("ASSET_ERROR_EMAIL", "Asset System Error", "Asset Error: {{errorCode}}",
                        "Asset {{assetId}} encountered error {{errorCode}} at {{timestamp}}.",
                        "{\"assetId\":\"Asset Identifier\",\"errorCode\":\"Error Code\",\"timestamp\":\"Error Time\"}", "ASSET_MGMT"),
                email("WELCOME_EMAIL", "Welcome Email", "Welcome to Our Store",
                        "Hello {{name}}, thank you for registering with us! Enjoy shopping 🎉",
                        "{\"name\":\"Customer Name\"}", "ECOM"),
                email("ORDER_CONFIRM_EMAIL", "Order Confirmation", "Order #{{orderId}} Confirmed",
                        "Hi {{name}}, your order {{orderId}} has been successfully confirmed.",
                        "{\"name\":\"Customer Name\",\"orderId\":\"Order ID\"}", "ECOM"),
                email("SHIPMENT_EMAIL", "Shipment Notification", "Your Order #{{orderId}} is Shipped",
                        "Hi {{name}}, your order {{orderId}} has been shipped. Track it here: {{trackingLink}}",
                        "{\"name\":\"Customer Name\",\"orderId\":\"Order ID\",\"trackingLink\":\"Tracking URL\"}", "ECOM"),
                email("DELIVERY_EMAIL", "Delivery Notification", "Your Order #{{orderId}} Delivered",
                        "Hi {{name}}, your order {{orderId}} has been delivered. We hope you enjoy your purchase 😊",
                        "{\"name\":\"Customer Name\",\"orderId\":\"Order ID\"}", "ECOM"),
                email("PASSWORD_RESET_EMAIL", "Password Reset", "Reset Your Password",
                        "Hello {{name}}, we received a request to reset your password. Click here: {{resetLink}}",
                        "{\"name\":\"Customer Name\",\"resetLink\":\"Password Reset Link\"}", "ECOM"),
                email("ERROR_EMAIL", "System Error Notification", "Error Code: {{errorCode}}",
                        "Dear Admin, error {{errorCode}} occurred at {{timestamp}}. Details: {{details}}",
                        "{\"errorCode\":\"Error Code\",\"timestamp\":\"Error Time\",\"details\":\"Error Details\"}", "ECOM"),
                email("OTP_EMAIL", "OTP Verification", "OTP Verification",
                        "Your OTP is {{otp}}. Do not share it with anyone.",
                        "{\"otp\":\"One-Time Password\"}", "ECOM")
            ));

            // ------------------- InApp Templates -------------------
            inappRepo.saveAll(List.of(
                inapp("ASSET_ASSIGN_INAPP", "Asset Assignment", "Asset Assigned",
                        "📌 Asset {{assetId}} has been assigned to you, {{name}}.",
                        "{\"assetId\":\"Asset Identifier\",\"name\":\"Employee Name\"}", "ASSET_MGMT"),
                inapp("ASSET_RETURN_INAPP", "Asset Return", "Asset Returned",
                        "↩️ Asset {{assetId}} returned successfully by {{name}}.",
                        "{\"assetId\":\"Asset Identifier\",\"name\":\"Employee Name\"}", "ASSET_MGMT"),
                inapp("ASSET_MAINT_INAPP", "Maintenance Alert", "Maintenance Scheduled",
                        "⚙️ Asset {{assetId}} is scheduled for maintenance on {{date}}.",
                        "{\"assetId\":\"Asset Identifier\",\"date\":\"Maintenance Date\"}", "ASSET_MGMT"),
                inapp("ASSET_ERROR_INAPP", "Asset Error", "Asset Error Notification",
                        "⚠️ Asset {{assetId}} error {{errorCode}} at {{timestamp}}.",
                        "{\"assetId\":\"Asset Identifier\",\"errorCode\":\"Error Code\",\"timestamp\":\"Error Time\"}", "ASSET_MGMT"),
                inapp("WELCOME_INAPP", "Welcome Notification", "Welcome to Our Store",
                        "👋 Hi {{name}}, thanks for registering! Enjoy shopping 🎉",
                        "{\"name\":\"Customer Name\"}", "ECOM"),
                inapp("ORDER_CONFIRM_INAPP", "Order Confirmation", "Order Confirmed",
                        "✅ Your order {{orderId}} has been confirmed.",
                        "{\"orderId\":\"Order ID\"}", "ECOM"),
                inapp("SHIPMENT_INAPP", "Shipment Notification", "Order Shipped",
                        "📦 Your order {{orderId}} has been shipped. Track here: {{trackingLink}}",
                        "{\"orderId\":\"Order ID\",\"trackingLink\":\"Tracking URL\"}", "ECOM"),
                inapp("DELIVERY_INAPP", "Delivery Notification", "Order Delivered",
                        "🎉 Your order {{orderId}} has been delivered successfully.",
                        "{\"orderId\":\"Order ID\"}", "ECOM"),
                inapp("PASSWORD_RESET_INAPP", "Password Reset", "Password Reset Requested",
                        "Hello {{name}}, a password reset was requested. Reset it here: {{resetLink}}",
                        "{\"name\":\"Customer Name\",\"resetLink\":\"Password Reset Link\"}", "ECOM"),
                inapp("ERROR_INAPP", "System Error Notification", "System Error",
                        "⚠️ Error {{errorCode}} occurred at {{timestamp}}. Details: {{details}}",
                        "{\"errorCode\":\"Error Code\",\"timestamp\":\"Error Time\",\"details\":\"Error Details\"}", "ECOM"),
                inapp("OTP_INAPP", "OTP Verification", "OTP Verification",
                        "Your OTP is {{otp}}. Do not share it with anyone.",
                        "{\"otp\":\"One-Time Password\"}", "ECOM")
            ));


            System.out.println("🚀 Seeding Notification Templates for ASSET_MGMT (14 Modules + 4 Channels)...");

            // =====================================================================
            // 1. ASSET CONTROLLER
            // =====================================================================
            notificationRepo.saveAll(List.of(
                email("ASSET_CREATED_EMAIL", "Asset Created", "New Asset: {{assetName}}",
                        "✅ Asset {{assetName}} created successfully by {{username}}.",
                        "{\"assetName\":\"Asset Name\",\"username\":\"Created By\"}", "ASSET_MGMT"),
                email("ASSET_UPDATED_EMAIL", "Asset Updated", "Asset Updated: {{assetName}}",
                        "✏️ Asset {{assetName}} updated successfully by {{username}}.",
                        "{\"assetName\":\"Asset Name\",\"username\":\"Updated By\"}", "ASSET_MGMT"),
                email("ASSET_DELETED_EMAIL", "Asset Deleted", "Asset Deleted: {{assetName}}",
                        "🗑️ Asset {{assetName}} deleted by {{username}}.",
                        "{\"assetName\":\"Asset Name\",\"username\":\"Deleted By\"}", "ASSET_MGMT")
            ));

            smsRepo.saveAll(List.of(
                sms("ASSET_CREATED_SMS", "Asset Created", "Asset {{assetName}} created successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT"),
                sms("ASSET_UPDATED_SMS", "Asset Updated", "Asset {{assetName}} updated successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT"),
                sms("ASSET_DELETED_SMS", "Asset Deleted", "Asset {{assetName}} deleted successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT")
            ));

            waRepo.saveAll(List.of(
                wa("ASSET_CREATED_WA", "Asset Created", "Asset Created",
                        "✅ Asset {{assetName}} created successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT"),
                wa("ASSET_UPDATED_WA", "Asset Updated", "Asset Updated",
                        "✏️ Asset {{assetName}} updated successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT"),
                wa("ASSET_DELETED_WA", "Asset Deleted", "Asset Deleted",
                        "🗑️ Asset {{assetName}} deleted successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT")
            ));

            inappRepo.saveAll(List.of(
                inapp("ASSET_CREATED_INAPP", "Asset Created", "Asset Created",
                        "✅ Asset {{assetName}} created successfully by {{username}}.",
                        "{\"assetName\":\"Asset Name\",\"username\":\"Created By\"}", "ASSET_MGMT"),
                inapp("ASSET_UPDATED_INAPP", "Asset Updated", "Asset Updated",
                        "✏️ Asset {{assetName}} updated successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT"),
                inapp("ASSET_DELETED_INAPP", "Asset Deleted", "Asset Deleted",
                        "🗑️ Asset {{assetName}} deleted successfully.",
                        "{\"assetName\":\"Asset Name\"}", "ASSET_MGMT")
            ));

            seedAssetMgmtModuleTemplates();

            System.out.println("✅ All Asset Management Templates Seeded Successfully!");

            System.out.println("✅ Template seeding completed successfully!");
        } else {
            System.out.println("ℹ️ Templates already exist, skipping seeding.");
        }
    }

    private void seedAssetMgmtModuleTemplates() {
        // 2. CATEGORY
        notificationRepo.saveAll(List.of(
            email("CATEGORY_CREATED_EMAIL", "Category Created", "Category Created: {{categoryName}}",
                    "📁 Category {{categoryName}} created successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            email("CATEGORY_UPDATED_EMAIL", "Category Updated", "Category Updated: {{categoryName}}",
                    "✏️ Category {{categoryName}} updated successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            email("CATEGORY_DELETED_EMAIL", "Category Deleted", "Category Deleted: {{categoryName}}",
                    "🗑️ Category {{categoryName}} deleted successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("CATEGORY_CREATED_SMS", "Category Created", "Category Created: {{categoryName}}",
                    "📁 Category {{categoryName}} created successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            smsWithSubject("CATEGORY_UPDATED_SMS", "Category Updated", "Category Updated: {{categoryName}}",
                    "✏️ Category {{categoryName}} updated successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            smsWithSubject("CATEGORY_DELETED_SMS", "Category Deleted", "Category Deleted: {{categoryName}}",
                    "🗑️ Category {{categoryName}} deleted successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("CATEGORY_CREATED_WA", "Category Created", "Category Created: {{categoryName}}",
                    "📁 Category {{categoryName}} created successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            wa("CATEGORY_UPDATED_WA", "Category Updated", "Category Updated: {{categoryName}}",
                    "✏️ Category {{categoryName}} updated successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            wa("CATEGORY_DELETED_WA", "Category Deleted", "Category Deleted: {{categoryName}}",
                    "🗑️ Category {{categoryName}} deleted successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("CATEGORY_CREATED_INAPP", "Category Created", "Category Created: {{categoryName}}",
                    "📁 Category {{categoryName}} created successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            inapp("CATEGORY_UPDATED_INAPP", "Category Updated", "Category Updated: {{categoryName}}",
                    "✏️ Category {{categoryName}} updated successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT"),
            inapp("CATEGORY_DELETED_INAPP", "Category Deleted", "Category Deleted: {{categoryName}}",
                    "🗑️ Category {{categoryName}} deleted successfully.",
                    "{\"categoryName\":\"Category Name\"}", "ASSET_MGMT")
        ));

        // 3. SUBCATEGORY
        notificationRepo.saveAll(List.of(
            email("SUBCATEGORY_CREATED_EMAIL", "SubCategory Created", "SubCategory Created: {{subCategoryName}}",
                    "📦 SubCategory {{subCategoryName}} created successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            email("SUBCATEGORY_UPDATED_EMAIL", "SubCategory Updated", "SubCategory Updated: {{subCategoryName}}",
                    "✏️ SubCategory {{subCategoryName}} updated successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            email("SUBCATEGORY_DELETED_EMAIL", "SubCategory Deleted", "SubCategory Deleted: {{subCategoryName}}",
                    "🗑️ SubCategory {{subCategoryName}} deleted successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("SUBCATEGORY_CREATED_SMS", "SubCategory Created", "SubCategory Created: {{subCategoryName}}",
                    "📦 SubCategory {{subCategoryName}} created successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            smsWithSubject("SUBCATEGORY_UPDATED_SMS", "SubCategory Updated", "SubCategory Updated: {{subCategoryName}}",
                    "✏️ SubCategory {{subCategoryName}} updated successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            smsWithSubject("SUBCATEGORY_DELETED_SMS", "SubCategory Deleted", "SubCategory Deleted: {{subCategoryName}}",
                    "🗑️ SubCategory {{subCategoryName}} deleted successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("SUBCATEGORY_CREATED_WA", "SubCategory Created", "SubCategory Created: {{subCategoryName}}",
                    "📦 SubCategory {{subCategoryName}} created successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            wa("SUBCATEGORY_UPDATED_WA", "SubCategory Updated", "SubCategory Updated: {{subCategoryName}}",
                    "✏️ SubCategory {{subCategoryName}} updated successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            wa("SUBCATEGORY_DELETED_WA", "SubCategory Deleted", "SubCategory Deleted: {{subCategoryName}}",
                    "🗑️ SubCategory {{subCategoryName}} deleted successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("SUBCATEGORY_CREATED_INAPP", "SubCategory Created", "SubCategory Created: {{subCategoryName}}",
                    "📦 SubCategory {{subCategoryName}} created successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            inapp("SUBCATEGORY_UPDATED_INAPP", "SubCategory Updated", "SubCategory Updated: {{subCategoryName}}",
                    "✏️ SubCategory {{subCategoryName}} updated successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT"),
            inapp("SUBCATEGORY_DELETED_INAPP", "SubCategory Deleted", "SubCategory Deleted: {{subCategoryName}}",
                    "🗑️ SubCategory {{subCategoryName}} deleted successfully.",
                    "{\"subCategoryName\":\"SubCategory Name\"}", "ASSET_MGMT")
        ));

        // 4. COMPONENT
        notificationRepo.saveAll(List.of(
            email("COMPONENT_CREATED_EMAIL", "Component Created", "Component Created: {{componentName}}",
                    "🧩 Component {{componentName}} created successfully by {{username}}.",
                    "{\"componentName\":\"Component Name\",\"username\":\"Created By\"}", "ASSET_MGMT"),
            email("COMPONENT_UPDATED_EMAIL", "Component Updated", "Component Updated: {{componentName}}",
                    "✏️ Component {{componentName}} updated successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT"),
            email("COMPONENT_DELETED_EMAIL", "Component Deleted", "Component Deleted: {{componentName}}",
                    "🗑️ Component {{componentName}} deleted successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("COMPONENT_CREATED_SMS", "Component Created", "Component Created: {{componentName}}",
                    "🧩 Component {{componentName}} created successfully by {{username}}.",
                    "{\"componentName\":\"Component Name\",\"username\":\"Created By\"}", "ASSET_MGMT"),
            smsWithSubject("COMPONENT_UPDATED_SMS", "Component Updated", "Component Updated: {{componentName}}",
                    "✏️ Component {{componentName}} updated successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT"),
            smsWithSubject("COMPONENT_DELETED_SMS", "Component Deleted", "Component Deleted: {{componentName}}",
                    "🗑️ Component {{componentName}} deleted successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("COMPONENT_CREATED_WA", "Component Created", "Component Created: {{componentName}}",
                    "🧩 Component {{componentName}} created successfully by {{username}}.",
                    "{\"componentName\":\"Component Name\",\"username\":\"Created By\"}", "ASSET_MGMT"),
            wa("COMPONENT_UPDATED_WA", "Component Updated", "Component Updated: {{componentName}}",
                    "✏️ Component {{componentName}} updated successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT"),
            wa("COMPONENT_DELETED_WA", "Component Deleted", "Component Deleted: {{componentName}}",
                    "🗑️ Component {{componentName}} deleted successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("COMPONENT_CREATED_INAPP", "Component Created", "Component Created: {{componentName}}",
                    "🧩 Component {{componentName}} created successfully by {{username}}.",
                    "{\"componentName\":\"Component Name\",\"username\":\"Created By\"}", "ASSET_MGMT"),
            inapp("COMPONENT_UPDATED_INAPP", "Component Updated", "Component Updated: {{componentName}}",
                    "✏️ Component {{componentName}} updated successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT"),
            inapp("COMPONENT_DELETED_INAPP", "Component Deleted", "Component Deleted: {{componentName}}",
                    "🗑️ Component {{componentName}} deleted successfully.",
                    "{\"componentName\":\"Component Name\"}", "ASSET_MGMT")
        ));

        // 5–14 and bulk upload — continued in next block
        seedAssetMgmtModules5To14AndBulkUpload();
    }

    private void seedAssetMgmtModules5To14AndBulkUpload() {
        // 5. MAKE
        notificationRepo.saveAll(List.of(
            email("MAKE_CREATED_EMAIL", "Make Created", "Make Created: {{makeName}}",
                    "🏭 Make {{makeName}} created successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            email("MAKE_UPDATED_EMAIL", "Make Updated", "Make Updated: {{makeName}}",
                    "✏️ Make {{makeName}} updated successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            email("MAKE_DELETED_EMAIL", "Make Deleted", "Make Deleted: {{makeName}}",
                    "🗑️ Make {{makeName}} deleted successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("MAKE_CREATED_SMS", "Make Created", "Make Created: {{makeName}}",
                    "🏭 Make {{makeName}} created successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            smsWithSubject("MAKE_UPDATED_SMS", "Make Updated", "Make Updated: {{makeName}}",
                    "✏️ Make {{makeName}} updated successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            smsWithSubject("MAKE_DELETED_SMS", "Make Deleted", "Make Deleted: {{makeName}}",
                    "🗑️ Make {{makeName}} deleted successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("MAKE_CREATED_WA", "Make Created", "Make Created: {{makeName}}",
                    "🏭 Make {{makeName}} created successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            wa("MAKE_UPDATED_WA", "Make Updated", "Make Updated: {{makeName}}",
                    "✏️ Make {{makeName}} updated successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            wa("MAKE_DELETED_WA", "Make Deleted", "Make Deleted: {{makeName}}",
                    "🗑️ Make {{makeName}} deleted successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("MAKE_CREATED_INAPP", "Make Created", "Make Created: {{makeName}}",
                    "🏭 Make {{makeName}} created successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            inapp("MAKE_UPDATED_INAPP", "Make Updated", "Make Updated: {{makeName}}",
                    "✏️ Make {{makeName}} updated successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT"),
            inapp("MAKE_DELETED_INAPP", "Make Deleted", "Make Deleted: {{makeName}}",
                    "🗑️ Make {{makeName}} deleted successfully.", "{\"makeName\":\"Make Name\"}", "ASSET_MGMT")
        ));

        // 6. MODEL
        notificationRepo.saveAll(List.of(
            email("MODEL_CREATED_EMAIL", "Model Created", "Model Created: {{modelName}}",
                    "🧱 Model {{modelName}} created successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            email("MODEL_UPDATED_EMAIL", "Model Updated", "Model Updated: {{modelName}}",
                    "✏️ Model {{modelName}} updated successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            email("MODEL_DELETED_EMAIL", "Model Deleted", "Model Deleted: {{modelName}}",
                    "🗑️ Model {{modelName}} deleted successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("MODEL_CREATED_SMS", "Model Created", "Model Created: {{modelName}}",
                    "🧱 Model {{modelName}} created successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            smsWithSubject("MODEL_UPDATED_SMS", "Model Updated", "Model Updated: {{modelName}}",
                    "✏️ Model {{modelName}} updated successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            smsWithSubject("MODEL_DELETED_SMS", "Model Deleted", "Model Deleted: {{modelName}}",
                    "🗑️ Model {{modelName}} deleted successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("MODEL_CREATED_WA", "Model Created", "Model Created: {{modelName}}",
                    "🧱 Model {{modelName}} created successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            wa("MODEL_UPDATED_WA", "Model Updated", "Model Updated: {{modelName}}",
                    "✏️ Model {{modelName}} updated successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            wa("MODEL_DELETED_WA", "Model Deleted", "Model Deleted: {{modelName}}",
                    "🗑️ Model {{modelName}} deleted successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("MODEL_CREATED_INAPP", "Model Created", "Model Created: {{modelName}}",
                    "🧱 Model {{modelName}} created successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            inapp("MODEL_UPDATED_INAPP", "Model Updated", "Model Updated: {{modelName}}",
                    "✏️ Model {{modelName}} updated successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT"),
            inapp("MODEL_DELETED_INAPP", "Model Deleted", "Model Deleted: {{modelName}}",
                    "🗑️ Model {{modelName}} deleted successfully.", "{\"modelName\":\"Model Name\"}", "ASSET_MGMT")
        ));

        // 7. VENDOR
        notificationRepo.saveAll(List.of(
            email("VENDOR_CREATED_EMAIL", "Vendor Created", "Vendor Created: {{vendorName}}",
                    "🏢 Vendor {{vendorName}} registered successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            email("VENDOR_UPDATED_EMAIL", "Vendor Updated", "Vendor Updated: {{vendorName}}",
                    "✏️ Vendor {{vendorName}} updated successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            email("VENDOR_DELETED_EMAIL", "Vendor Deleted", "Vendor Deleted: {{vendorName}}",
                    "🗑️ Vendor {{vendorName}} deleted successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("VENDOR_CREATED_SMS", "Vendor Created", "Vendor Created: {{vendorName}}",
                    "🏢 Vendor {{vendorName}} registered successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            smsWithSubject("VENDOR_UPDATED_SMS", "Vendor Updated", "Vendor Updated: {{vendorName}}",
                    "✏️ Vendor {{vendorName}} updated successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            smsWithSubject("VENDOR_DELETED_SMS", "Vendor Deleted", "Vendor Deleted: {{vendorName}}",
                    "🗑️ Vendor {{vendorName}} deleted successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("VENDOR_CREATED_WA", "Vendor Created", "Vendor Created: {{vendorName}}",
                    "🏢 Vendor {{vendorName}} registered successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            wa("VENDOR_UPDATED_WA", "Vendor Updated", "Vendor Updated: {{vendorName}}",
                    "✏️ Vendor {{vendorName}} updated successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            wa("VENDOR_DELETED_WA", "Vendor Deleted", "Vendor Deleted: {{vendorName}}",
                    "🗑️ Vendor {{vendorName}} deleted successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("VENDOR_CREATED_INAPP", "Vendor Created", "Vendor Created: {{vendorName}}",
                    "🏢 Vendor {{vendorName}} registered successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            inapp("VENDOR_UPDATED_INAPP", "Vendor Updated", "Vendor Updated: {{vendorName}}",
                    "✏️ Vendor {{vendorName}} updated successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT"),
            inapp("VENDOR_DELETED_INAPP", "Vendor Deleted", "Vendor Deleted: {{vendorName}}",
                    "🗑️ Vendor {{vendorName}} deleted successfully.", "{\"vendorName\":\"Vendor Name\"}", "ASSET_MGMT")
        ));

        // 8. OUTLET
        notificationRepo.saveAll(List.of(
            email("OUTLET_CREATED_EMAIL", "Outlet Created", "Outlet Created: {{outletName}}",
                    "🏬 Outlet {{outletName}} created successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            email("OUTLET_UPDATED_EMAIL", "Outlet Updated", "Outlet Updated: {{outletName}}",
                    "✏️ Outlet {{outletName}} updated successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            email("OUTLET_DELETED_EMAIL", "Outlet Deleted", "Outlet Deleted: {{outletName}}",
                    "🗑️ Outlet {{outletName}} deleted successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("OUTLET_CREATED_SMS", "Outlet Created", "Outlet Created: {{outletName}}",
                    "🏬 Outlet {{outletName}} created successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            smsWithSubject("OUTLET_UPDATED_SMS", "Outlet Updated", "Outlet Updated: {{outletName}}",
                    "✏️ Outlet {{outletName}} updated successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            smsWithSubject("OUTLET_DELETED_SMS", "Outlet Deleted", "Outlet Deleted: {{outletName}}",
                    "🗑️ Outlet {{outletName}} deleted successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("OUTLET_CREATED_WA", "Outlet Created", "Outlet Created: {{outletName}}",
                    "🏬 Outlet {{outletName}} created successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            wa("OUTLET_UPDATED_WA", "Outlet Updated", "Outlet Updated: {{outletName}}",
                    "✏️ Outlet {{outletName}} updated successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            wa("OUTLET_DELETED_WA", "Outlet Deleted", "Outlet Deleted: {{outletName}}",
                    "🗑️ Outlet {{outletName}} deleted successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("OUTLET_CREATED_INAPP", "Outlet Created", "Outlet Created: {{outletName}}",
                    "🏬 Outlet {{outletName}} created successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            inapp("OUTLET_UPDATED_INAPP", "Outlet Updated", "Outlet Updated: {{outletName}}",
                    "✏️ Outlet {{outletName}} updated successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT"),
            inapp("OUTLET_DELETED_INAPP", "Outlet Deleted", "Outlet Deleted: {{outletName}}",
                    "🗑️ Outlet {{outletName}} deleted successfully.", "{\"outletName\":\"Outlet Name\"}", "ASSET_MGMT")
        ));

        // 9. AMC
        notificationRepo.saveAll(List.of(
            email("AMC_CREATED_EMAIL", "AMC Created", "AMC Created for Asset {{assetId}}",
                    "📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            email("AMC_UPDATED_EMAIL", "AMC Updated", "AMC Updated for Asset {{assetId}}",
                    "✏️ AMC details updated for asset {{assetId}}.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            email("AMC_DELETED_EMAIL", "AMC Deleted", "AMC Deleted for Asset {{assetId}}",
                    "🗑️ AMC for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("AMC_CREATED_SMS", "AMC Created", "AMC Created for Asset {{assetId}}",
                    "📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            smsWithSubject("AMC_UPDATED_SMS", "AMC Updated", "AMC Updated for Asset {{assetId}}",
                    "✏️ AMC details updated for asset {{assetId}}.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            smsWithSubject("AMC_DELETED_SMS", "AMC Deleted", "AMC Deleted for Asset {{assetId}}",
                    "🗑️ AMC for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("AMC_CREATED_WA", "AMC Created", "AMC Created for Asset {{assetId}}",
                    "📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            wa("AMC_UPDATED_WA", "AMC Updated", "AMC Updated for Asset {{assetId}}",
                    "✏️ AMC details updated for asset {{assetId}}.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            wa("AMC_DELETED_WA", "AMC Deleted", "AMC Deleted for Asset {{assetId}}",
                    "🗑️ AMC for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("AMC_CREATED_INAPP", "AMC Created", "AMC Created for Asset {{assetId}}",
                    "📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            inapp("AMC_UPDATED_INAPP", "AMC Updated", "AMC Updated for Asset {{assetId}}",
                    "✏️ AMC details updated for asset {{assetId}}.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            inapp("AMC_DELETED_INAPP", "AMC Deleted", "AMC Deleted for Asset {{assetId}}",
                    "🗑️ AMC for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));

        // 10. WARRANTY
        notificationRepo.saveAll(List.of(
            email("WARRANTY_CREATED_EMAIL", "Warranty Created", "Warranty Created for Asset {{assetId}}",
                    "🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            email("WARRANTY_UPDATED_EMAIL", "Warranty Updated", "Warranty Updated for Asset {{assetId}}",
                    "✏️ Warranty for asset {{assetId}} updated successfully.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            email("WARRANTY_DELETED_EMAIL", "Warranty Deleted", "Warranty Deleted for Asset {{assetId}}",
                    "🗑️ Warranty for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("WARRANTY_CREATED_SMS", "Warranty Created", "Warranty Created for Asset {{assetId}}",
                    "🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            smsWithSubject("WARRANTY_UPDATED_SMS", "Warranty Updated", "Warranty Updated for Asset {{assetId}}",
                    "✏️ Warranty for asset {{assetId}} updated successfully.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            smsWithSubject("WARRANTY_DELETED_SMS", "Warranty Deleted", "Warranty Deleted for Asset {{assetId}}",
                    "🗑️ Warranty for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("WARRANTY_CREATED_WA", "Warranty Created", "Warranty Created for Asset {{assetId}}",
                    "🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            wa("WARRANTY_UPDATED_WA", "Warranty Updated", "Warranty Updated for Asset {{assetId}}",
                    "✏️ Warranty for asset {{assetId}} updated successfully.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            wa("WARRANTY_DELETED_WA", "Warranty Deleted", "Warranty Deleted for Asset {{assetId}}",
                    "🗑️ Warranty for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("WARRANTY_CREATED_INAPP", "Warranty Created", "Warranty Created for Asset {{assetId}}",
                    "🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.",
                    "{\"assetId\":\"Asset ID\",\"startDate\":\"Start Date\",\"endDate\":\"End Date\"}", "ASSET_MGMT"),
            inapp("WARRANTY_UPDATED_INAPP", "Warranty Updated", "Warranty Updated for Asset {{assetId}}",
                    "✏️ Warranty for asset {{assetId}} updated successfully.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            inapp("WARRANTY_DELETED_INAPP", "Warranty Deleted", "Warranty Deleted for Asset {{assetId}}",
                    "🗑️ Warranty for asset {{assetId}} deleted.", "{\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));

        // 11. DOCUMENT
        notificationRepo.saveAll(List.of(
            email("DOCUMENT_UPLOADED_EMAIL", "Document Uploaded", "Document Uploaded for Asset {{assetId}}",
                    "📎 Document {{fileName}} uploaded for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            email("DOCUMENT_DELETED_EMAIL", "Document Deleted", "Document Deleted for Asset {{assetId}}",
                    "🗑️ Document {{fileName}} deleted for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("DOCUMENT_UPLOADED_SMS", "Document Uploaded", "Document Uploaded for Asset {{assetId}}",
                    "📎 Document {{fileName}} uploaded for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            smsWithSubject("DOCUMENT_DELETED_SMS", "Document Deleted", "Document Deleted for Asset {{assetId}}",
                    "🗑️ Document {{fileName}} deleted for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("DOCUMENT_UPLOADED_WA", "Document Uploaded", "Document Uploaded for Asset {{assetId}}",
                    "📎 Document {{fileName}} uploaded for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            wa("DOCUMENT_DELETED_WA", "Document Deleted", "Document Deleted for Asset {{assetId}}",
                    "🗑️ Document {{fileName}} deleted for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("DOCUMENT_UPLOADED_INAPP", "Document Uploaded", "Document Uploaded for Asset {{assetId}}",
                    "📎 Document {{fileName}} uploaded for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT"),
            inapp("DOCUMENT_DELETED_INAPP", "Document Deleted", "Document Deleted for Asset {{assetId}}",
                    "🗑️ Document {{fileName}} deleted for asset {{assetId}}.",
                    "{\"fileName\":\"File Name\",\"assetId\":\"Asset ID\"}", "ASSET_MGMT")
        ));

        // 12. USERLINK (template code USERLINK_CREATED_INAP matches SQL seed)
        notificationRepo.saveAll(List.of(
            email("USERLINK_CREATED_EMAIL", "User Link Created", "User Link Created",
                    "🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.",
                    "{\"username\":\"User Name\",\"assetId\":\"Asset ID\",\"subCategory\":\"SubCategory\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("USERLINK_CREATED_SMS", "User Link Created", "User Link Created",
                    "🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.",
                    "{\"username\":\"User Name\",\"assetId\":\"Asset ID\",\"subCategory\":\"SubCategory\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("USERLINK_CREATED_WA", "User Link Created", "User Link Created",
                    "🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.",
                    "{\"username\":\"User Name\",\"assetId\":\"Asset ID\",\"subCategory\":\"SubCategory\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("USERLINK_CREATED_INAP", "User Link Created", "User Link Created",
                    "🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.",
                    "{\"username\":\"User Name\",\"assetId\":\"Asset ID\",\"subCategory\":\"SubCategory\"}", "ASSET_MGMT")
        ));

        // 13. AUDIT
        notificationRepo.saveAll(List.of(
            email("AUDIT_ENTRY_EMAIL", "Audit Log Entry", "New Audit Log Entry",
                    "🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.",
                    "{\"action\":\"Action\",\"entityName\":\"Entity\",\"entityId\":\"ID\",\"username\":\"Actor\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("AUDIT_ENTRY_SMS", "Audit Log Entry", "New Audit Log Entry",
                    "🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.",
                    "{\"action\":\"Action\",\"entityName\":\"Entity\",\"entityId\":\"ID\",\"username\":\"Actor\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("AUDIT_ENTRY_WA", "Audit Log Entry", "New Audit Log Entry",
                    "🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.",
                    "{\"action\":\"Action\",\"entityName\":\"Entity\",\"entityId\":\"ID\",\"username\":\"Actor\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("AUDIT_ENTRY_INAPP", "Audit Log Entry", "New Audit Log Entry",
                    "🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.",
                    "{\"action\":\"Action\",\"entityName\":\"Entity\",\"entityId\":\"ID\",\"username\":\"Actor\"}", "ASSET_MGMT")
        ));

        // 14. FILEDOWNLOAD
        notificationRepo.saveAll(List.of(
            email("FILE_DOWNLOAD_EMAIL", "File Download", "File Downloaded",
                    "📥 File {{fileName}} downloaded successfully by {{username}}.",
                    "{\"fileName\":\"File Name\",\"username\":\"Downloaded By\"}", "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            smsWithSubject("FILE_DOWNLOAD_SMS", "File Download", "File Downloaded",
                    "📥 File {{fileName}} downloaded successfully by {{username}}.",
                    "{\"fileName\":\"File Name\",\"username\":\"Downloaded By\"}", "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("FILE_DOWNLOAD_WA", "File Download", "File Downloaded",
                    "📥 File {{fileName}} downloaded successfully by {{username}}.",
                    "{\"fileName\":\"File Name\",\"username\":\"Downloaded By\"}", "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("FILE_DOWNLOAD_INAPP", "File Download", "File Downloaded",
                    "📥 File {{fileName}} downloaded successfully by {{username}}.",
                    "{\"fileName\":\"File Name\",\"username\":\"Downloaded By\"}", "ASSET_MGMT")
        ));

        // Master Data Bulk Upload (skippedCount per V8 migration)
        String bulkPlaceholders = "{\"entityType\":\"Entity Type\",\"totalCount\":\"Total Rows\",\"successCount\":\"Successful\",\"failureCount\":\"Failed\",\"skippedCount\":\"Skipped\",\"username\":\"User\",\"timestamp\":\"Time\"}";
        String bulkEmailBody = "Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Skipped: {{skippedCount}}. By {{username}} at {{timestamp}}.";
        String bulkSmsBody = "Bulk upload {{entityType}}: Total {{totalCount}}, Success {{successCount}}, Failed {{failureCount}}, Skipped {{skippedCount}}. By {{username}}.";

        notificationRepo.saveAll(List.of(
            email("MASTER_DATA_BULK_UPLOAD_EMAIL", "Master Data Bulk Upload Summary", "Bulk Upload Completed",
                    bulkEmailBody, bulkPlaceholders, "ASSET_MGMT")
        ));
        smsRepo.saveAll(List.of(
            sms("MASTER_DATA_BULK_UPLOAD_SMS", "Master Data Bulk Upload Summary", bulkSmsBody,
                    bulkPlaceholders, "ASSET_MGMT")
        ));
        waRepo.saveAll(List.of(
            wa("MASTER_DATA_BULK_UPLOAD_WHATSAPP", "Master Data Bulk Upload Summary", "Bulk Upload Completed",
                    bulkEmailBody, bulkPlaceholders, "ASSET_MGMT")
        ));
        inappRepo.saveAll(List.of(
            inapp("MASTER_DATA_BULK_UPLOAD_INAPP", "Master Data Bulk Upload Summary", "Bulk Upload Completed",
                    bulkEmailBody, bulkPlaceholders, "ASSET_MGMT")
        ));
    }

    private boolean isEmpty() {
        return smsRepo.count() == 0 && waRepo.count() == 0 &&
               notificationRepo.count() == 0 && inappRepo.count() == 0;
    }

    // Helper Builders
    private SmsTemplateMaster sms(String code, String name, String body, String placeholders, String project) {
        SmsTemplateMaster t = new SmsTemplateMaster();
        t.setTemplateCode(code);
        t.setName(name);
        t.setBody(body);
        t.setPlaceholders(placeholders);
        t.setActive(true);
        t.setProjectType(project);
        return t;
    }

    private SmsTemplateMaster smsWithSubject(String code, String name, String subject, String body,
                                             String placeholders, String project) {
        SmsTemplateMaster t = sms(code, name, body, placeholders, project);
        t.setSubject(subject);
        return t;
    }

    private WhatsappTemplateMaster wa(String code, String name, String subject, String body, String placeholders, String project) {
        WhatsappTemplateMaster t = new WhatsappTemplateMaster();
        t.setTemplateCode(code);
        t.setName(name);
        t.setSubject(subject);
        t.setBody(body);
        t.setPlaceholders(placeholders);
        t.setActive(true);
        t.setProjectType(project);
        return t;
    }

    private NotificationTemplateMaster email(String code, String name, String subject, String body, String placeholders, String project) {
        NotificationTemplateMaster t = new NotificationTemplateMaster();
        t.setTemplateCode(code);
        t.setName(name);
        t.setSubject(subject);
        t.setBody(body);
        t.setPlaceholders(placeholders);
        t.setActive(true);
        t.setProjectType(project);
        return t;
    }

    private InappTemplateMaster inapp(String code, String name, String title, String body, String placeholders, String project) {
        InappTemplateMaster t = new InappTemplateMaster();
        t.setTemplateCode(code);
        t.setName(name);
        t.setTitle(title);
        t.setBody(body);
        t.setPlaceholders(placeholders);
        t.setActive(true);
        t.setProjectType(project);
        return t;
    }
}


