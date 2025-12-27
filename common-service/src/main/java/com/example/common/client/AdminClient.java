package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * ✅ AdminClient
 * Fetches project admin users from the auth-service.
 */
@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AdminClient {

    /**
     * Returns a list of admin user details (id, username, email, mobile)
     * for the specified project type.
     */
    @GetMapping("/api/auth/v1/admins")
    List<Map<String, Object>> getAdminsByProjectType(@RequestParam("projectType") String projectType);
}

