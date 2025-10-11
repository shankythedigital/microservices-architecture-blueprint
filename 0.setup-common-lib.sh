#!/usr/bin/env bash
set -e

PROJECT_ROOT=$(pwd)
COMMON_DIR="$PROJECT_ROOT/common-lib"

echo "📦 Setting up common-lib module in $COMMON_DIR ..."


# 1. Create directories
mkdir -p $COMMON_DIR/src/main/java/com/example/common/dto
mkdir -p $COMMON_DIR/src/main/java/com/example/common/projection
mkdir -p $COMMON_DIR/src/main/java/com/example/common/converter
mkdir -p $COMMON_DIR/src/main/java/com/example/common/util
mkdir -p $COMMON_DIR/src/main/java/com/example/common/config

# 2. Create pom.xml
cat > $COMMON_DIR/pom.xml <<'EOF'
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.example</groupId>
    <artifactId>microservices-architecture-blueprint</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
  </parent>

  <artifactId>common-lib</artifactId>
  <packaging>jar</packaging>

  <properties>
        <java.version>17</java.version>
    </properties>

  <dependencies>

      
      <!-- Web, JPA, Security, OAuth2 -->
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
      </dependency>
      <!-- In common-lib/pom.xml add Feign + Security -->
          <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
      </dependency>

      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
      </dependency>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
      </dependency>
  
      <!-- JWT (jjwt) -->
      <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
      </dependency>
      <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
      </dependency>
      <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
      </dependency>
      <dependency>
  
      <groupId>javax.validation</groupId>
  
      <artifactId>validation-api</artifactId>
  
      <version>2.0.1.Final</version>
  
  </dependency>
  
      <dependency>
  
      <groupId>jakarta.validation</groupId>
  
      <artifactId>jakarta.validation-api</artifactId>
  
      <version>3.0.0</version>
  
  </dependency>
  
      <!-- MySQL -->
      <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
      </dependency>
  
      <!-- lombok -->
      <dependency>
          <groupId>org.projectlombok</groupId>
          <artifactId>lombok</artifactId>
          <version>1.18.30</version>
          <scope>provided</scope>
          <optional>true</optional>
      </dependency>
  
      <!-- Test -->
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
      </dependency>

      <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
        <scope>provided</scope>
      </dependency>
  
  
  </dependencies>



</project>

EOF

# 3. Add sample DTO
cat > $COMMON_DIR/src/main/java/com/example/common/dto/UserDto.java <<'EOF'
package com.example.common.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
}
EOF

# 4. Add sample Projection
cat > $COMMON_DIR/src/main/java/com/example/common/projection/UsersProjection.java <<'EOF'
package com.example.common.projection;

public interface UsersProjection {
    Long getId();
    String getUsername();
    String getEmail();
}
EOF

################################################################################
# 4. Utility: AES Attribute Converter for PII encryption
################################################################################
cat > $COMMON_DIR/src/main/java/com/example/common/converter/AesAttributeConverter.java <<'EOF'
package com.example.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/*
 Simple AES/CBC/PKCS5 implementation for demonstration.
 In production:
 - Use a KMS to manage keys (AWS KMS, GCP KMS, Azure KeyVault)
 - Use authenticated encryption (GCM) and rotate keys
 - Do not store keys in source code or config files in plaintext
*/
@Component
@Converter
public class AesAttributeConverter implements AttributeConverter<String, String> {

    private static String SECRET = System.getenv().getOrDefault("ENCRYPTION_KEY", "change-this-to-32-byte-secret!!");
    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    private SecretKeySpec getKeySpec() {
        byte[] keyBytes = new byte[16];
        byte[] secretBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            byte[] iv = new byte[IV_SIZE];
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), ivSpec);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception ex) {
            throw new RuntimeException("Encryption error", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), new IvParameterSpec(iv));
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Decryption error", ex);
        }
    }
}
EOF

cat > $COMMON_DIR/src/main/java/com/example/common/converter/OtpConverter.java <<'EOF'

package com.example.common.converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpConverter {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 3;

    public String generateOtp(String mobile) {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        String otpStr = String.valueOf(otp);

        return otpStr;
    }

    public boolean validateOtp(String otpInput, String getOtpCode) {
        boolean valid = passwordEncoder.matches(otpInput, getOtpCode);
        return valid;
    }
}



EOF


cat > $COMMON_DIR/src/main/java/com/example/common/util/OtpUtils.java <<'EOF'
package com.example.common.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;

public class OtpUtils {

    private static final String DIGITS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 3;

    /**
     * Generates an OTP of the specified length.
     *
     * @param mobile The mobile number (can be used for logging or additional logic).
     * @param length The length of the OTP to generate.
     * @return A randomly generated OTP as a String.
     */
    public static String generateOtp(String mobile) {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }

    /**
     * Validates the OTP input against the stored OTP code.
     *
     * @param otpInput   The OTP input provided by the user.
     * @param getOtpCode The stored OTP code to validate against.
     * @param passwordEncoder The PasswordEncoder instance used for validation.
     * @return True if the OTP is valid, false otherwise.
     */
    public static boolean validateOtp(String otpInput, String getOtpCode, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(otpInput, getOtpCode);
    }
}

EOF


cat > $COMMON_DIR/src/main/java/com/example/common/config/FeignAuthConfig.java <<'EOF'

package com.example.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            String token = System.getenv().getOrDefault("ACCESS_TOKEN", "");
            if (!token.isEmpty()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}

EOF

cat > $COMMON_DIR/src/main/java/com/example/common/util/JwtTokenUtil.java <<'EOF'
// common-lib/src/main/java/com/example/common/util/JwtTokenUtil.java
package com.example.common.util;

import io.jsonwebtoken.*;
import java.util.Date;

public class JwtTokenUtil {
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "4aD9#kLp!2zQmN7xYvRtWpShUfBgJcKd");

    public static String generateToken(String subject, long expirationMs) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();
    }

    public static String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET.getBytes())
                .parseClaimsJws(token).getBody().getSubject();
    }
}

EOF




# 5. Add MainClass
cat > $COMMON_DIR/src/main/java/com/example/common/MainClass.java <<'EOF'
package com.example.common;

public class MainClass {
    public static void main(String[] args) {
        System.out.println("✅ common-lib module is working!");
    }
}
EOF

# 6. Add module to parent pom.xml if missing
if ! grep -q "<module>common-lib</module>" "$PROJECT_ROOT/pom.xml"; then
  echo "🔧 Adding <module>common-lib</module> to parent pom.xml ..."
  sed -i.bak '/<modules>/a\    <module>common-lib</module>' "$PROJECT_ROOT/pom.xml"
fi

# 7. Build
echo "🚀 Running mvn clean install ..."
mvn clean install -pl common-lib

echo "✅ common-lib setup complete!"
