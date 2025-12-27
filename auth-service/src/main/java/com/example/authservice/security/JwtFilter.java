package com.example.authservice.security;

import com.example.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ===============================================================
 *  🔐 JwtFilter — Enterprise Edition
 * ===============================================================
 *  • Works in both LOCAL and CLOUD modes.
 *  • Logs all authentication decisions.
 *  • Prints clean trace info without exposing the full JWT.
 *  • Fully compatible with JwtUtil (RSA / HMAC).
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final boolean isCloud;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;

        // Detect cloud environment
        this.isCloud = System.getenv("AWS_EXECUTION_ENV") != null ||
                System.getenv("EC2_INSTANCE_ID") != null ||
                System.getenv("ECS_CONTAINER_METADATA_URI") != null;

        System.out.println("--------------------------------------------------");
        System.out.println("🔐 [JwtFilter] Initialized");
        System.out.println("🌍 Environment: " + (isCloud ? "CLOUD (EC2/SSM)" : "LOCAL (IDE/Maven)"));
        System.out.println("--------------------------------------------------");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();

        System.out.printf("➡️ [JwtFilter] %s %s%n", method, path);

        // Handle Authorization header
        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("   ⛔ No Bearer token found → Request is PUBLIC");
            chain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(7);
        String tokenPreview = token.length() > 12
                ? token.substring(0, 10) + "..."
                : token;

        System.out.println("   🔍 JWT Token detected (preview): " + tokenPreview);

        try {
            // --------------------------------------------------------------
            //    🔐 Validate and Parse Token
            // --------------------------------------------------------------
            Jws<Claims> parsed = jwtUtil.parseToken(token);
            Claims claims = parsed.getBody();

            // Extract fields
            Object uidObj = claims.get("uid");
            String uid = uidObj != null ? uidObj.toString() : null;

            List<String> roles = claims.get("roles", List.class);
            if (roles == null) roles = Collections.emptyList();

            List<SimpleGrantedAuthority> authorities =
                    roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            // --------------------------------------------------------------
            //    🧩 Create Authentication Object
            // --------------------------------------------------------------
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(uid, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("   ✅ JWT validated");
            System.out.println("      • User ID: " + uid);
            System.out.println("      • Roles: " + roles);
            System.out.println("      • Authentication set in SecurityContext\n");

        } catch (JwtException e) {
            System.out.println("   ❌ Invalid JWT: " + e.getMessage());

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Invalid or expired token");
            return;
        } catch (Exception e) {
            System.out.println("   ❌ Unexpected JWT error: " + e);

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Token processing failed");
            return;
        }

        // Continue filter chain
        chain.doFilter(req, res);
    }
}


