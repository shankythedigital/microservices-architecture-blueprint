package com.example.notification.service;

import com.example.notification.entity.templates.NotificationTemplateMaster;
import com.example.notification.entity.templates.SmsTemplateMaster;
import com.example.notification.entity.templates.WhatsappTemplateMaster;
import com.example.notification.entity.templates.InappTemplateMaster;
import com.example.notification.repository.NotificationTemplateRepository;
import com.example.notification.repository.SmsTemplateRepository;
import com.example.notification.repository.WhatsappTemplateRepository;
import com.example.notification.repository.InappTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TemplateResolverService {

    private final NotificationTemplateRepository notificationRepo;
    private final SmsTemplateRepository smsRepo;
    private final WhatsappTemplateRepository waRepo;
    private final InappTemplateRepository inappRepo;

    public TemplateResolverService(NotificationTemplateRepository notificationRepo,
                                   SmsTemplateRepository smsRepo,
                                   WhatsappTemplateRepository waRepo,
                                   InappTemplateRepository inappRepo) {
        this.notificationRepo = notificationRepo;
        this.smsRepo = smsRepo;
        this.waRepo = waRepo;
        this.inappRepo = inappRepo;
    }

    public String resolveBody(String channel, String templateCode) {
        switch (channel.toUpperCase()) {
            case "SMS":
                Optional<SmsTemplateMaster> sms = smsRepo.findByTemplateCode(templateCode);
                return sms.map(SmsTemplateMaster::getBody).orElse(null);
            case "WHATSAPP":
                Optional<WhatsappTemplateMaster> wa = waRepo.findByTemplateCode(templateCode);
                return wa.map(WhatsappTemplateMaster::getBody).orElse(null);
            case "INAPP":
                Optional<InappTemplateMaster> inapp = inappRepo.findByTemplateCode(templateCode);
                return inapp.map(InappTemplateMaster::getBody).orElse(null);
            case "EMAIL":
            case "NOTIFICATION":
                Optional<NotificationTemplateMaster> n = notificationRepo.findByTemplateCode(templateCode);
                return n.map(NotificationTemplateMaster::getBody).orElse(null);
            default:
                throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }

    public String resolveSubject(String channel, String templateCode) {
        switch (channel.toUpperCase()) {
            case "WHATSAPP":
                return waRepo.findByTemplateCode(templateCode).map(WhatsappTemplateMaster::getSubject).orElse(null);
            case "EMAIL":
            case "NOTIFICATION":
                return notificationRepo.findByTemplateCode(templateCode).map(NotificationTemplateMaster::getSubject).orElse(null);
            default:
                return null; // SMS/INAPP usually don’t have subjects
        }
    }
}
