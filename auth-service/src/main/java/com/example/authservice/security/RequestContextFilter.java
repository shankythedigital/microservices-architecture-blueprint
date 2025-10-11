package com.example.authservice.security;

import com.example.authservice.util.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RequestContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            String url = request.getRequestURI();
            String method = request.getMethod();

            RequestContext.setIp(ip);
            RequestContext.setUserAgent(ua);
            RequestContext.setUrl(url);
            RequestContext.setMethod(method);

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clearAll();
        }
    }
}
