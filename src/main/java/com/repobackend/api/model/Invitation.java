package com.repobackend.api.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "invitaciones")
public class Invitation {
    @Id
    private String id;
    private String tallerId;
    private String fromUserId;
    private String codeHash; // hash of the code sent to user
    private String email; // optional
    private String role;
    private boolean redeemed = false;
    private String redeemedByUserId;
    private int attempts = 0;
    private int maxAttempts = 10;
    private boolean blocked = false;
    private Date lastAttemptAt;
    private Date expiresAt;
    private Date createdAt = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTallerId() { return tallerId; }
    public void setTallerId(String tallerId) { this.tallerId = tallerId; }
    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isRedeemed() { return redeemed; }
    public void setRedeemed(boolean redeemed) { this.redeemed = redeemed; }
    public String getRedeemedByUserId() { return redeemedByUserId; }
    public void setRedeemedByUserId(String redeemedByUserId) { this.redeemedByUserId = redeemedByUserId; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public Date getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Date lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
