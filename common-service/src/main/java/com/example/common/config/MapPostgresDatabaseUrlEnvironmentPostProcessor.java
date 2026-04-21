package com.example.common.config;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

/**
 * Normalizes Postgres datasource configuration for PaaS deployments (e.g. Render).
 * <p>
 * Environment variables aligned with a typical Render dashboard:
 * <ul>
 *   <li>{@value #ENV_SPRING_DATASOURCE_URL} → {@code spring.datasource.url} (JDBC)</li>
 *   <li>{@value #ENV_SPRING_DATASOURCE_USERNAME} → {@code spring.datasource.username}</li>
 *   <li>{@value #ENV_SPRING_DATASOURCE_PASSWORD} → {@code spring.datasource.password}</li>
 *   <li>{@value #ENV_DATABASE_URL} → optional {@code postgres://} / {@code postgresql://}; parsed into the three
 *       properties above only when {@code spring.datasource.url} is not already set (e.g. linked Postgres adds
 *       {@code DATABASE_URL} but not Spring-prefixed vars)</li>
 *   <li>{@value #ENV_SUPABASE_AUTH_SCHEMA} → optional; if set and the JDBC URL has no {@code currentSchema=},
 *       appends {@code currentSchema=&lt;schema&gt;} so the driver search_path matches Hibernate
 *       {@code default_schema}</li>
 * </ul>
 * Other keys from the same deployment (not read here): {@code PORT}, {@code AUTH_SERVICE_URL},
 * {@code LOGGING_LEVEL_COM_EXAMPLE}.
 */
public class MapPostgresDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  /** Render / shell: {@code SPRING_DATASOURCE_URL}. */
  public static final String ENV_SPRING_DATASOURCE_URL = "SPRING_DATASOURCE_URL";
  /** Render / shell: {@code SPRING_DATASOURCE_USERNAME}. */
  public static final String ENV_SPRING_DATASOURCE_USERNAME = "SPRING_DATASOURCE_USERNAME";
  /** Render / shell: {@code SPRING_DATASOURCE_PASSWORD}. */
  public static final String ENV_SPRING_DATASOURCE_PASSWORD = "SPRING_DATASOURCE_PASSWORD";
  /** Render linked Postgres often exposes this instead of {@value #ENV_SPRING_DATASOURCE_URL}. */
  public static final String ENV_DATABASE_URL = "DATABASE_URL";
  /** Supabase / multi-schema: matches auth-service {@code spring.jpa.properties.hibernate.default_schema}. */
  public static final String ENV_SUPABASE_AUTH_SCHEMA = "SUPABASE_AUTH_SCHEMA";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Map<String, Object> overrides = new HashMap<>();

    String url = environment.getProperty("spring.datasource.url");
    String username = environment.getProperty("spring.datasource.username");
    String password = environment.getProperty("spring.datasource.password");

    if (!isDatasourceUrlSet(url)) {
      ParsedFromUri parsed = tryParseDatabaseUrl(environment.getProperty(ENV_DATABASE_URL));
      if (parsed != null) {
        url = parsed.jdbcUrl;
        username = parsed.username;
        password = parsed.password;
        overrides.put("spring.datasource.url", url);
        overrides.put("spring.datasource.username", username);
        overrides.put("spring.datasource.password", password);
      }
    }

    String effectiveUrl = overrides.containsKey("spring.datasource.url")
        ? (String) overrides.get("spring.datasource.url")
        : environment.getProperty("spring.datasource.url");
    String schema = environment.getProperty(ENV_SUPABASE_AUTH_SCHEMA);
    if (isDatasourceUrlSet(effectiveUrl) && schema != null && StringUtils.hasText(schema)) {
      String withSchema = appendCurrentSchemaIfMissing(effectiveUrl, schema.trim());
      if (!withSchema.equals(effectiveUrl)) {
        overrides.put("spring.datasource.url", withSchema);
      }
    }

    if (!overrides.isEmpty()) {
      MutablePropertySources sources = environment.getPropertySources();
      sources.addFirst(new MapPropertySource("mapPostgresDatabaseUrl", overrides));
    }
  }

  private static ParsedFromUri tryParseDatabaseUrl(String databaseUrl) {
    if (!StringUtils.hasText(databaseUrl)) {
      return null;
    }
    String normalized = databaseUrl.trim();
    if (!normalized.startsWith("postgres://") && !normalized.startsWith("postgresql://")) {
      return null;
    }
    try {
      URI uri = URI.create(normalized.replaceFirst("^postgres(ql)?:", "postgresql:"));
      String userInfo = uri.getUserInfo();
      if (!StringUtils.hasText(userInfo)) {
        return null;
      }
      String[] creds = userInfo.split(":", 2);
      String user = URLDecoder.decode(creds[0], StandardCharsets.UTF_8);
      String pass = creds.length > 1 ? URLDecoder.decode(creds[1], StandardCharsets.UTF_8) : "";

      String host = uri.getHost();
      if (!StringUtils.hasText(host)) {
        return null;
      }
      int port = uri.getPort() == -1 ? 5432 : uri.getPort();
      String path = uri.getPath();
      if (path != null && path.startsWith("/")) {
        path = path.substring(1);
      }
      if (!StringUtils.hasText(path)) {
        path = "postgres";
      }
      String query = uri.getQuery();
      String jdbcUrl = buildJdbcUrl(host, port, path, query);
      return new ParsedFromUri(jdbcUrl, user, pass);
    } catch (Exception e) {
      return null;
    }
  }

  private static boolean isDatasourceUrlSet(String url) {
    if (!StringUtils.hasText(url)) {
      return false;
    }
    String t = url.trim();
    if (t.startsWith("${") && t.endsWith("}")) {
      return false;
    }
    return true;
  }

  private static String buildJdbcUrl(String host, int port, String database, String query) {
    StringBuilder jdbc = new StringBuilder();
    jdbc.append("jdbc:postgresql://").append(host).append(":").append(port).append("/").append(database);
    if (StringUtils.hasText(query)) {
      jdbc.append("?").append(query);
      if (!query.contains("sslmode=")) {
        jdbc.append("&sslmode=require");
      }
    } else {
      jdbc.append("?sslmode=require");
    }
    return jdbc.toString();
  }

  /**
   * Adds {@code currentSchema} when absent so pooled/direct connections agree with
   * {@code SUPABASE_AUTH_SCHEMA} / Hibernate {@code default_schema}.
   */
  private static String appendCurrentSchemaIfMissing(String jdbcUrl, String schema) {
    if (!StringUtils.hasText(jdbcUrl) || !StringUtils.hasText(schema)) {
      return jdbcUrl;
    }
    if (jdbcUrl.toLowerCase().contains("currentschema=")) {
      return jdbcUrl;
    }
    String enc = URLEncoder.encode(schema, StandardCharsets.UTF_8);
    if (jdbcUrl.contains("?")) {
      return jdbcUrl + "&currentSchema=" + enc;
    }
    return jdbcUrl + "?currentSchema=" + enc;
  }

  private record ParsedFromUri(String jdbcUrl, String username, String password) {}

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
