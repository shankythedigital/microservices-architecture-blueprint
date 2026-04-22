package com.example.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

/**
 * Render (and similar IPv4-only hosts) often get {@code Network is unreachable} when using Supabase
 * <strong>direct</strong> connections ({@code db.&lt;ref&gt;.supabase.co}:5432) because that hostname can resolve
 * to IPv6-only routes. Supabase <strong>pooler</strong> hosts ({@code aws-0-&lt;region&gt;.pooler.supabase.com})
 * are reachable over IPv4.
 * <p>
 * When {@code RENDER=true} (or {@code FORCE_SUPABASE_POOLER=true}) and {@code SUPABASE_REGION} is set
 * (e.g. {@code ap-northeast-1} from the Supabase dashboard), rewrites a direct JDBC URL to the transaction
 * pooler (port {@value #DEFAULT_POOLER_PORT}, override with {@code SUPABASE_POOLER_PORT}).
 * Also sets {@code SUPABASE_PROJECT_REF} so {@link FixSupabasePoolerDatasourceEnvironmentPostProcessor} can
 * adjust {@code postgres} → {@code postgres.&lt;ref&gt;} after the hostname no longer contains {@code db.*.supabase.co}.
 */
public class RewriteSupabaseDirectToPoolerForRenderEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  static final int DEFAULT_POOLER_PORT = 6543;

  private static final Pattern DIRECT_JDBC =
      Pattern.compile(
          "jdbc:postgresql://db\\.([a-z0-9]+)\\.supabase\\.co:\\d+/([^?]+)(\\?.*)?",
          Pattern.CASE_INSENSITIVE);

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    if (!shouldRewrite(environment)) {
      return;
    }
    String region = environment.getProperty("SUPABASE_REGION");
    if (!StringUtils.hasText(region)) {
      String url = environment.getProperty("spring.datasource.url");
      if (isDirectSupabaseDbUrl(url)) {
        System.err.println(
            "[microservices] RENDER: spring.datasource.url uses direct db.*.supabase.co — "
                + "set SUPABASE_REGION (e.g. ap-northeast-1 from Supabase Settings) to rewrite to IPv4 pooler, "
                + "or paste the pooler JDBC URL from Supabase Connect.");
      }
      return;
    }
    String url = environment.getProperty("spring.datasource.url");
    if (!StringUtils.hasText(url)) {
      return;
    }
    Matcher m = DIRECT_JDBC.matcher(url.trim());
    if (!m.matches()) {
      return;
    }
    String ref = m.group(1).toLowerCase();
    String database = m.group(2);
    String existingQuery = m.group(3) != null ? m.group(3) : "";

    int poolerPort = DEFAULT_POOLER_PORT;
    String portProp = environment.getProperty("SUPABASE_POOLER_PORT");
    if (StringUtils.hasText(portProp)) {
      try {
        poolerPort = Integer.parseInt(portProp.trim());
      } catch (NumberFormatException ignored) {
        poolerPort = DEFAULT_POOLER_PORT;
      }
    }

    String host = "aws-0-" + region.trim() + ".pooler.supabase.com";
    StringBuilder jdbc = new StringBuilder();
    jdbc.append("jdbc:postgresql://").append(host).append(":").append(poolerPort).append("/").append(database);
    if (StringUtils.hasText(existingQuery)) {
      jdbc.append(existingQuery);
      if (!existingQuery.toLowerCase().contains("sslmode=")) {
        jdbc.append("&sslmode=require");
      }
    } else {
      jdbc.append("?sslmode=require");
    }

    Map<String, Object> map = new HashMap<>();
    map.put("spring.datasource.url", jdbc.toString());
    map.put("SUPABASE_PROJECT_REF", ref);
    MutablePropertySources sources = environment.getPropertySources();
    sources.addFirst(new MapPropertySource("rewriteSupabaseDirectToPoolerForRender", map));
    System.err.println(
        "[microservices] Rewrote Supabase direct host to pooler " + host + ":" + poolerPort + " (ref=" + ref + ")");
  }

  private static boolean shouldRewrite(ConfigurableEnvironment env) {
    if ("true".equalsIgnoreCase(env.getProperty("FORCE_SUPABASE_POOLER"))) {
      return true;
    }
    String r = env.getProperty("RENDER");
    if (r != null && ("true".equalsIgnoreCase(r) || "1".equals(r))) {
      return true;
    }
    if (StringUtils.hasText(env.getProperty("RENDER_SERVICE_ID"))) {
      return true;
    }
    // Render sets this on web services even when RENDER is not visible in the dashboard env editor.
    return StringUtils.hasText(env.getProperty("RENDER_EXTERNAL_URL"));
  }

  private static boolean isDirectSupabaseDbUrl(String url) {
    if (!StringUtils.hasText(url)) {
      return false;
    }
    String u = url.toLowerCase();
    return u.contains("db.") && u.contains(".supabase.co") && !u.contains("pooler.supabase");
  }

  @Override
  public int getOrder() {
    // After MapPostgresDatabaseUrlEnvironmentPostProcessor, before FixSupabasePoolerDatasourceEnvironmentPostProcessor
    return Ordered.HIGHEST_PRECEDENCE + 5;
  }
}
