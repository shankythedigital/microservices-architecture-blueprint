package com.example.notification.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEngineUtil {

    /**
     * ✅ Renders a template by replacing placeholders like {{key}} with values from the map.
     *    Key matching is case-insensitive.
     */
    public static String render(String template, Map<String, Object> placeholders) {
        if (template == null) return null;
        String rendered = template;

        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Use case-insensitive regex to replace all forms of {{key}}
                String regex = "(?i)\\{\\{" + Pattern.quote(key) + "\\}\\}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(rendered);
                rendered = matcher.replaceAll(Matcher.quoteReplacement(String.valueOf(value)));
            }
        }

        return rendered;
    }
}

