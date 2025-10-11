package com.example.notification.config;

import com.example.notification.entity.templates.NotificationTemplateMaster;
import com.example.notification.entity.templates.SmsTemplateMaster;
import com.example.notification.entity.templates.WhatsappTemplateMaster;
import com.example.notification.entity.templates.InappTemplateMaster;
import com.example.notification.repository.NotificationTemplateRepository;
import com.example.notification.repository.SmsTemplateRepository;
import com.example.notification.repository.WhatsappTemplateRepository;
import com.example.notification.repository.InappTemplateRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

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
    public void seed() {
        if (notificationRepo.count() == 0) {
            NotificationTemplateMaster t = new NotificationTemplateMaster();
            t.setTemplateCode("WELCOME_EMAIL");
            t.setName("Welcome Email");
            t.setSubject("Welcome to Our Store");
            t.setBody("Hello {{name}}, thank you for registering with us!");
            t.setPlaceholders("{\"name\":\"Customer Name\"}");
            t.setActive(true);
            t.setProjectType("ECOM");
            notificationRepo.save(t);
        }

        if (smsRepo.count() == 0) {
            SmsTemplateMaster s = new SmsTemplateMaster();
            s.setTemplateCode("OTP_SMS");
            s.setName("OTP SMS");
            s.setBody("Your OTP is {{otp}}. Do not share it.");
            s.setPlaceholders("{\"otp\":\"One-Time Password\"}");
            s.setActive(true);
            s.setProjectType("ECOM");
            smsRepo.save(s);
        }

        if (waRepo.count() == 0) {
            WhatsappTemplateMaster w = new WhatsappTemplateMaster();
            w.setTemplateCode("WELCOME_WA");
            w.setName("Welcome WhatsApp");
            w.setSubject("Welcome");
            w.setBody("Hi {{name}}, welcome to Our Store!");
            w.setPlaceholders("{\"name\":\"Customer Name\"}");
            w.setActive(true);
            w.setProjectType("ECOM");
            waRepo.save(w);
        }

        if (inappRepo.count() == 0) {
            InappTemplateMaster i = new InappTemplateMaster();
            i.setTemplateCode("WELCOME_INAPP");
            i.setName("Welcome InApp");
            i.setTitle("Welcome");
            i.setBody("Hi {{name}}, thanks for registering!");
            i.setPlaceholders("{\"name\":\"Customer Name\"}");
            i.setActive(true);
            i.setProjectType("ECOM");
            inappRepo.save(i);
        }
    }
}
