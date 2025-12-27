package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.entity.*;
import com.example.notification.repository.*;
import com.example.common.util.HashUtil;
import com.example.notification.util.TemplateEngineUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final SmsLogRepository smsRepo;
    private final NotificationLogRepository notificationRepo;
    private final WhatsappLogRepository whatsappRepo;
    private final InappLogRepository inappRepo;
    private final TemplateResolverService templateResolver;

    public NotificationService(
            SmsLogRepository smsRepo,
            NotificationLogRepository notificationRepo,
            WhatsappLogRepository whatsappRepo,
            InappLogRepository inappRepo,
            TemplateResolverService templateResolver) {
        this.smsRepo = smsRepo;
        this.notificationRepo = notificationRepo;
        this.whatsappRepo = whatsappRepo;
        this.inappRepo = inappRepo;
        this.templateResolver = templateResolver;
    }

    @Transactional
    public void enqueue(NotificationRequest req) {
        // Step 1: Resolve template subject + body
        String rawBody = templateResolver.resolveBody(req.getChannel(), req.getTemplateCode());
        String subject = templateResolver.resolveSubject(req.getChannel(), req.getTemplateCode());

        // Step 2: Render body with placeholders
        String renderedBody = TemplateEngineUtil.render(rawBody, req.getPlaceholders());

        // Step 3: Persist based on channel
        switch (req.getChannel().toUpperCase()) {
            case "SMS" -> saveSms(req, renderedBody);
            case "WHATSAPP" -> saveWhatsapp(req, renderedBody);
            case "EMAIL", "NOTIFICATION" -> saveNotification(req, renderedBody, subject);
            case "INAPP" -> saveInapp(req, renderedBody);
            default -> throw new IllegalArgumentException("Unsupported channel: " + req.getChannel());
        }
    }

    private void saveSms(NotificationRequest req, String body) {
        SmsLog sms = new SmsLog();
        sms.setUsername(req.getUsername());
        sms.setMobile(req.getMobile());
        sms.setMessage(body);
        sms.setTemplateCode(req.getTemplateCode());
        sms.setUserId(req.getUserId());
        sms.setCreatedAt(LocalDateTime.now());

        // if (sms.getMobile() != null) {
        //     sms.setMobileFingerprint(HashUtil.fingerprint(sms.getMobile()));
        // }

        smsRepo.save(sms);
    }

    private void saveWhatsapp(NotificationRequest req, String body) {
        WhatsappLog wa = new WhatsappLog();
        wa.setUsername(req.getUsername());
        wa.setMobile(req.getMobile());
        wa.setMessage(body);
        wa.setTemplateCode(req.getTemplateCode());
        wa.setUserId(req.getUserId());
        wa.setCreatedAt(LocalDateTime.now());

        // if (wa.getMobile() != null) {
        //     wa.setMobileFingerprint(HashUtil.fingerprint(wa.getMobile()));
        // }

        whatsappRepo.save(wa);
    }

    private void saveNotification(NotificationRequest req, String body, String subject) {
        NotificationLog n = new NotificationLog();
        n.setUsername(req.getUsername());
        n.setEmail(req.getEmail());
        n.setSubject(subject != null ? subject : req.getSubject());
        n.setMessage(body);
        n.setChannel(req.getChannel());
        n.setTemplateCode(req.getTemplateCode());
        n.setUserId(req.getUserId());
        n.setCreatedAt(LocalDateTime.now());

        // if (n.getEmail() != null) {
        //     n.setEmailFingerprint(HashUtil.fingerprint(n.getEmail()));
        // }

        notificationRepo.save(n);
    }

    private void saveInapp(NotificationRequest req, String body) {
        InappLog in = new InappLog();
        in.setUsername(req.getUsername());
        in.setTitle(req.getSubject()); // in-app often uses a title
        in.setMessage(body);
        in.setTemplateCode(req.getTemplateCode());
        in.setUserId(req.getUserId());
        in.setCreatedAt(LocalDateTime.now());

        inappRepo.save(in);
    }
}


