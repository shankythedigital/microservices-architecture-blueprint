package com.example.asset.dto;
import java.util.Map;
public class AssetNotificationRequest {
    public String channel;
    public String username;
    public String templateCode;
    public String userId;
    public Map<String, Object> placeholders;
}
