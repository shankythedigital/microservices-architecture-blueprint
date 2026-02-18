-- Communication opt-out preferences (SMS, Email, WhatsApp, In-App, Push).
-- Default FALSE = user receives notifications; TRUE = user has opted out of that channel.

ALTER TABLE user_detail_master
ADD COLUMN opt_out_sms BOOLEAN DEFAULT FALSE,
ADD COLUMN opt_out_email BOOLEAN DEFAULT FALSE,
ADD COLUMN opt_out_whatsapp BOOLEAN DEFAULT FALSE,
ADD COLUMN opt_out_inapp BOOLEAN DEFAULT FALSE,
ADD COLUMN opt_out_push BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_user_detail_opt_out_sms ON user_detail_master(opt_out_sms);
CREATE INDEX idx_user_detail_opt_out_email ON user_detail_master(opt_out_email);
