package com.site.webapp.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invite_tokens")
public class InviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime usedAt;

    protected InviteToken() {
    }

    private InviteToken(String email, Role role, int days) {
        this.token = UUID.randomUUID().toString();
        this.email = email;
        this.role = role;
        this.expiresAt = LocalDateTime.now().plusDays(days);
    }

    public static InviteToken create(String email, Role role) {
        return new InviteToken(email, role, 3);
    }



    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
    public boolean isUsed() {
        return this.usedAt != null;
    }
    public boolean isValid() {
        return !isUsed() && !isExpired();
    }
    public void markAsUsed() {this.usedAt = LocalDateTime.now(); }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {return role;}
    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {return usedAt;}
    public void setUsedAt(LocalDateTime usedAt) {this.usedAt = usedAt;}
}
