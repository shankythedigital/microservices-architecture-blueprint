
// // // // package com.example.authservice.security;


// // // // import org.springframework.security.core.Authentication;
// // // // import org.springframework.security.core.context.SecurityContextHolder;

// // // // import org.springframework.context.annotation.Bean;
// // // // import org.springframework.context.annotation.Configuration;
// // // // import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// // // // import org.springframework.security.config.http.SessionCreationPolicy;
// // // // import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// // // // import org.springframework.security.crypto.password.PasswordEncoder;
// // // // import org.springframework.security.web.SecurityFilterChain;
// // // // import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// // // // @Configuration
// // // // public class SecurityConfig {

// // // //     private final JwtFilter jwtFilter;

// // // //     public SecurityConfig(JwtFilter jwtFilter) {
// // // //         this.jwtFilter = jwtFilter;
// // // //     }

// // // //     public static Long getCurrentUserId() {
// // // //         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
// // // //         if (auth == null || auth.getName() == null) return null;
// // // //         try {
// // // //             return Long.parseLong(auth.getName());
// // // //         } catch (NumberFormatException e) {
// // // //             return null;
// // // //         }
// // // //     }

// // // //     @Bean
// // // //     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// // // //         http.csrf(csrf -> csrf.disable())
// // // //             .authorizeHttpRequests(auth -> auth
// // // //                 .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
// // // //                 .requestMatchers("/api/admin/**").hasRole("ADMIN")
// // // //                 .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
// // // //                 .anyRequest().authenticated()
// // // //             )
// // // //             .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// // // //             .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

// // // //         return http.build();
// // // //     }

// // // //     @Bean
// // // //     public PasswordEncoder passwordEncoder() {
// // // //         return new BCryptPasswordEncoder();
// // // //     }
// // // // }


package com.example.authservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final Environment env;

    public SecurityConfig(JwtFilter jwtFilter, Environment env) {
        this.jwtFilter = jwtFilter;
        this.env = env;
    }

    // =====================================================================
    // 🔍 Current Authenticated User Helper
    // =====================================================================
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =====================================================================
    // 🔐 Main Spring Security Filter Chain
    // =====================================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // -----------------------------------------------------------------
        // 🌍 Detect Cloud or Local Environment
        // -----------------------------------------------------------------
        boolean isCloud = System.getenv("AWS_EXECUTION_ENV") != null
                || System.getenv("EC2_INSTANCE_ID") != null
                || System.getenv("ECS_CONTAINER_METADATA_URI") != null;

        String envSource = isCloud ? "🌩️ CLOUD (EC2/SSM)" : "💻 LOCAL (IDE/Maven)";
        System.out.println("\n==============================================");
        System.out.println("🔐 [SecurityConfig] Building Security FilterChain");
        System.out.println("🔎 Environment Mode: " + envSource);
        System.out.println("==============================================");

        // -----------------------------------------------------------------
        // 🔓 Authorization Rules (Printed for Diagnostics)
        // -----------------------------------------------------------------
        System.out.println("➡️  PUBLIC ENDPOINTS:");
        System.out.println("   - /api/auth/**");
        System.out.println("   - /actuator/health");

        System.out.println("➡️  ADMIN ENDPOINT:");
        System.out.println("   - /api/admin/** (ROLE_ADMIN)");

        System.out.println("➡️  USER ENDPOINTS:");
        System.out.println("   - /api/user/** (ROLE_USER or ROLE_ADMIN)");

        System.out.println("➡️  ALL OTHER ENDPOINTS → AUTHENTICATED\n");

        // -----------------------------------------------------------------
        // 🔄 JWT Filter Diagnostic Log
        // -----------------------------------------------------------------
        System.out.println("🔧 Injecting JwtFilter BEFORE UsernamePasswordAuthenticationFilter");
        System.out.println("   Filter class: " + jwtFilter.getClass().getSimpleName());
        System.out.println("==============================================\n");

        // -----------------------------------------------------------------
        // ⚙️ Configure Security
        // -----------------------------------------------------------------
        http.csrf(csrf -> csrf.disable());

        // CORS for Postman / Browser
        http.cors(customizer -> {});

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/uploads/**",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================================
    // 🔑 Password Encoder
    // =====================================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("🔐 [SecurityConfig] BCryptPasswordEncoder initialized");
        return new BCryptPasswordEncoder();
    }
}


