package com.repobackend.api.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;
    private String userId;
    private String tokenHash;
    private Date issuedAt = new Date();
    private Date expiresAt;
    private boolean revoked = false;
    private String deviceInfo;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Date getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}
