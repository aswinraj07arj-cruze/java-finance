package com.aswin.financetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150, columnDefinition = "VARCHAR(150) DEFAULT 'User'")
    private String fullName = "User";

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "username", nullable = true, length = 150)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255, columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String passwordHash = "";

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'USER'")
    private String role = "USER";

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String fullName, String email) {
        this(fullName, email, "defaultPasswordHash");
    }

    public User(String fullName, String email, String passwordHash) {
        this.fullName = fullName != null ? fullName : "User";
        this.email = email != null ? email.toLowerCase().trim() : null;
        this.username = this.email;
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.role = "USER";
        this.enabled = true;
    }

    public User(String fullName, String email, String passwordHash, String role, boolean enabled) {
        this.fullName = fullName != null ? fullName : "User";
        this.email = email != null ? email.toLowerCase().trim() : null;
        this.username = this.email;
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.role = role != null ? role : "USER";
        this.enabled = enabled;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
        if (this.username == null) {
            this.username = this.email;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username != null ? username : email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase().trim() : null;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
