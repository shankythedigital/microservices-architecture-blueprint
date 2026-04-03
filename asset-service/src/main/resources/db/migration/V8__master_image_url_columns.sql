-- Optional public/relative image URLs for masters and assets (UI thumbnails, CDN).
ALTER TABLE product_category ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE product_sub_category ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE product_make ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE product_model ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE asset_component_master ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE vendor_master ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE purchase_outlet ADD COLUMN image_url VARCHAR(512) NULL;
ALTER TABLE asset_master ADD COLUMN image_url VARCHAR(512) NULL;
