
// // // // package com.example.common.security;

// // // // import io.jsonwebtoken.Claims;
// // // // import jakarta.servlet.FilterChain;
// // // // import jakarta.servlet.ServletException;
// // // // import jakarta.servlet.http.HttpServletRequest;
// // // // import jakarta.servlet.http.HttpServletResponse;
// // // // import org.slf4j.Logger;
// // // // import org.slf4j.LoggerFactory;
// // // // import org.springframework.http.HttpHeaders;
// // // // import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// // // // import org.springframework.security.core.authority.SimpleGrantedAuthority;
// // // // import org.springframework.security.core.context.SecurityContextHolder;
// // // // import org.springframework.stereotype.Component;
// // // // import org.springframework.web.filter.OncePerRequestFilter;

// // // // import java.io.IOException;
// // // // import java.util.Collection;
// // // // import java.util.Collections;
// // // // import java.util.List;

// // // // /**
// // // //  * ✅ JwtAuthFilter
// // // //  * 
// // // //  * Common authentication filter that validates incoming requests
// // // //  * using JWT tokens verified by JwtVerifier.
// // // //  */
// // // // @Component
// // // // public class JwtAuthFilter extends OncePerRequestFilter {

// // // //     private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

// // // //     private final JwtVerifier jwtVerifier;

// // // //     public JwtAuthFilter(JwtVerifier jwtVerifier) {
// // // //         this.jwtVerifier = jwtVerifier;
// // // //     }

// // // //     @Override
// // // //     protected void doFilterInternal(HttpServletRequest request,
// // // //                                     HttpServletResponse response,
// // // //                                     FilterChain filterChain)
// // // //             throws ServletException, IOException {
// // // //         String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

// // // //         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
// // // //             filterChain.doFilter(request, response);
// // // //             return;
// // // //         }

// // // //         try {
// // // //             Claims claims = jwtVerifier.validateToken(authHeader);
// // // //             String username = claims.getSubject();
// // // //             List<String> roles = claims.get("roles", List.class);
// // // //             Collection<SimpleGrantedAuthority> authorities = roles != null
// // // //                     ? roles.stream().map(SimpleGrantedAuthority::new).toList()
// // // //                     : Collections.emptyList();

// // // //             UsernamePasswordAuthenticationToken authentication =
// // // //                     new UsernamePasswordAuthenticationToken(username, authHeader, authorities);
// // // //             SecurityContextHolder.getContext().setAuthentication(authentication);

// // // //             log.debug("✅ Authenticated user={} roles={}", username, roles);
// // // //         } catch (Exception e) {
// // // //             log.warn("⚠️ JWT validation failed: {}", e.getMessage());
// // // //         }

// // // //         filterChain.doFilter(request, response);
// // // //     }
// // // // }


package com.example.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * =======================================================================
 *  🔐 JwtAuthFilter — Enterprise-Grade Authentication Filter
 * =======================================================================
 *
 *  • Validates incoming requests using JwtVerifier.
 *  • Extracts user identity + roles.
 *  • Enriches Spring SecurityContext (ThreadLocal).
 *
 *  Diagnostics:
 *  • Logs token source
 *  • Logs roles extracted
 *  • Logs reason if validation fails
 *  • Ignores missing tokens (allows public endpoints)
 *
 * =======================================================================
 */

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtVerifier verifier;

    public JwtAuthFilter(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    // ===================================================================
    //  🚦 MAIN FILTER LOGIC
    // ===================================================================
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        // ---------------------------------------------------------------
        // 1️⃣ No token → continue without authentication
        // ---------------------------------------------------------------
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("🔸 [JwtAuthFilter] No Authorization header for path={}", path);
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7).trim();
        log.debug("🔍 [JwtAuthFilter] Bearer token detected for path={}", path);

        // ---------------------------------------------------------------
        // 2️⃣ Validate token via JwtVerifier
        // ---------------------------------------------------------------
        try {
            Claims claims = verifier.validate(token);

            String userId = claims.getSubject();
            Object rolesObj = claims.get("roles");
            List<String> roles = (rolesObj instanceof List<?>)
                    ? ((List<?>) rolesObj).stream().map(Object::toString).toList()
                    : Collections.emptyList();

            log.debug("✔ [JwtAuthFilter] Valid token → uid={} roles={}", userId, roles);

            // -----------------------------------------------------------
            // 3️⃣ Build Spring Security authentication
            // -----------------------------------------------------------
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, token, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            log.warn("❌ [JwtAuthFilter] Token validation FAILED → reason={}", ex.getMessage());
            // DO NOT block request — let controller decide authorization
            // (Spring security config handles "authenticated()" paths)
        }

        // ---------------------------------------------------------------
        // 4️⃣ Continue filter chain
        // ---------------------------------------------------------------
        chain.doFilter(req, res);
    }
}



