package com.example.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * PaaS environments (e.g. Render) often set {@code SPRING_DATASOURCE_USERNAME=postgres} for every
 * service. Supabase pooler endpoints require {@code postgres.&lt;project-ref&gt;} as the database
 * user. This runs before {@code DataSource} autoconfiguration and rewrites the username when the
 * JDBC URL targets a Supabase pooler host.
 * <p>
 * Set {@code SUPABASE_PROJECT_REF} when the JDBC URL does not contain {@code db.&lt;ref&gt;.supabase.co}
 * (typical for pooler-only connection strings).
 */
public class FixSupabasePoolerDatasourceEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final Pattern DB_HOST_PROJECT_REF =
      Pattern.compile("db\\.([a-z0-9]+)\\.supabase\\.co", Pattern.CASE_INSENSITIVE);

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    String url = environment.getProperty("spring.datasource.url");
    if (url == null || !isSupabasePoolerUrl(url)) {
      return;
    }
    String username = environment.getProperty("spring.datasource.username");
    if (username == null || !"postgres".equals(username.trim())) {
      return;
    }
    Optional<String> ref = resolveProjectRef(environment, url);
    if (ref.isEmpty()) {
      return;
    }
    String poolerUser = "postgres." + ref.get();
    Map<String, Object> map = new HashMap<>();
    map.put("spring.datasource.username", poolerUser);
    MutablePropertySources sources = environment.getPropertySources();
    sources.addFirst(new MapPropertySource("fixSupabasePoolerDatasourceUsername", map));
  }

  private static boolean isSupabasePoolerUrl(String url) {
    String u = url.toLowerCase();
    return u.contains("pooler.supabase.com") || u.contains("pooler.supabase.co");
  }

  private static Optional<String> resolveProjectRef(ConfigurableEnvironment env, String jdbcUrl) {
    String explicit = env.getProperty("SUPABASE_PROJECT_REF");
    if (explicit != null && !explicit.isBlank()) {
      return Optional.of(explicit.trim());
    }
    explicit = env.getProperty("spring.supabase.project-ref");
    if (explicit != null && !explicit.isBlank()) {
      return Optional.of(explicit.trim());
    }
    Matcher m = DB_HOST_PROJECT_REF.matcher(jdbcUrl);
    if (m.find()) {
      return Optional.of(m.group(1));
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    // After MapPostgresDatabaseUrlEnvironmentPostProcessor (DATABASE_URL → datasource)
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}
