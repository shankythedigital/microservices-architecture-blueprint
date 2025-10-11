package com.example.notification;

// import com.example.notification.crypto.JpaAttributeEncryptor;
import com.example.notification.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import java.util.Base64;

@SpringBootApplication
public class NotificationServiceApplication {

    // @Value("${notify.enc.key}")
    // private String notifyEncKey;

    // @Value("${notify.hmac.key}")
    // private String notifyHmacKey;

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    // @PostConstruct
    // public void initCrypto() {
    //     byte[] enc = Base64.getDecoder().decode(notifyEncKey);
    //     JpaAttributeEncryptor.init(enc);
    //     HashUtil.init(notifyHmacKey); // base64 string
    // }
}

