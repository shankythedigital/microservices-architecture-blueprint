# Connection Error Fix Guide

## Problem
```
DioException [connection error]: The connection errored: The XMLHttpRequest onError callback was called.
```

## Root Causes

1. **Web Platform Localhost Issue**: On web, `localhost` may not resolve correctly
2. **CORS Issues**: Backend may not have CORS enabled
3. **Backend Not Running**: Services may not be started
4. **Network Configuration**: Incorrect API endpoint configuration

## Solutions

### 1. For Web Platform

If running on web (Chrome, Edge, etc.), you have two options:

#### Option A: Use Machine IP Address
1. Find your machine's IP address:
   ```bash
   # macOS/Linux
   ifconfig | grep "inet "
   
   # Windows
   ipconfig
   ```

2. Update `app_config.dart` to use your IP:
   ```dart
   static String get _defaultHost {
     if (kIsWeb) {
       return '192.168.1.100'; // Your machine's IP
     }
     // ...
   }
   ```

#### Option B: Use Localhost with Backend CORS
Ensure your backend services have CORS enabled:

**Auth Service (SecurityConfig.java):**
```java
http.cors(customizer -> {});
```

**Asset Service (SecurityConfig.java):**
```java
http.cors(customizer -> {});
```

**Notification Service (SecurityConfig.java):**
```java
http.cors(customizer -> {});
```

### 2. For Android Emulator

Android emulator uses `10.0.2.2` to access host machine's localhost. The code now handles this automatically.

### 3. For iOS Simulator

iOS simulator can use `localhost` directly. The code handles this automatically.

### 4. Environment Variables

You can override URLs using environment variables:

```bash
# Run with custom URLs
flutter run --dart-define=AUTH_SERVICE_URL=http://192.168.1.100:8081 \
           --dart-define=ASSET_SERVICE_URL=http://192.168.1.100:8083 \
           --dart-define=NOTIFICATION_SERVICE_URL=http://192.168.1.100:8082
```

### 5. Verify Backend Services

Ensure all services are running:

```bash
# Check if services are running
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # Notification Service
curl http://localhost:8083/actuator/health  # Asset Service
```

### 6. Check CORS Configuration

Verify CORS is enabled in all Spring Boot services:

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // In production, specify actual origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## Error Handling Improvements

The code now:
1. ✅ Detects connection errors more accurately
2. ✅ Provides helpful error messages
3. ✅ Handles platform-specific URL configuration
4. ✅ Skips retries for connection errors (prevents spam)
5. ✅ Better error messages for CORS issues

## Testing

1. **Test on Web:**
   ```bash
   flutter run -d chrome
   ```

2. **Test on Android:**
   ```bash
   flutter run -d android
   ```

3. **Test on iOS:**
   ```bash
   flutter run -d ios
   ```

## Common Issues

### Issue: "Cannot connect to localhost"
**Solution:** Use your machine's IP address instead of localhost for web platform.

### Issue: "CORS error"
**Solution:** Enable CORS in backend services or use a proxy.

### Issue: "Backend not responding"
**Solution:** Ensure all Spring Boot services are running and accessible.

## Next Steps

1. Update `app_config.dart` with your machine's IP if using web platform
2. Ensure CORS is enabled in all backend services
3. Verify all services are running
4. Test the connection

---

**Last Updated:** 2025-12-11

