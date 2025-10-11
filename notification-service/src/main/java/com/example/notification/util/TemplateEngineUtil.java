package com.example.notification.util;

import java.util.Map;

public class TemplateEngineUtil {
    public static String render(String template, Map<String, Object> placeholders) {
        if (template == null) return null;
        String rendered = template;
        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                String key = "{{" + entry.getKey() + "}}";
                rendered = rendered.replace(key, String.valueOf(entry.getValue()));
            }
        }
        return rendered;
    }
}
