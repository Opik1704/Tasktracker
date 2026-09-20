package com.site.webapp.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "archived_users",indexes = {
        @Index(name = "idx_archived_users_original_id", columnList = "originalUserId"), //[cite: 8]
        @Index(name = "idx_archived_users_email", columnList = "originalEmail") //[cite: 8]
})
public class ArchivedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long originalUserId;

    private String originalEmail;
    private String originalFirstName;
    private String originalLastName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime archivedAt;

    private String archivedByAdminEmail;

    public ArchivedUser() {}

    public ArchivedUser(User user, String adminEmail) {
        this.originalUserId = user.getId();
        this.originalEmail = user.getEmail();
        this.originalFirstName = user.getFirstName();
        this.originalLastName = user.getLastName();
        this.archivedByAdminEmail = adminEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOriginalUserId() {
        return originalUserId;
    }

    public void setOriginalUserId(Long originalUserId) {
        this.originalUserId = originalUserId;
    }

    public String getOriginalEmail() {
        return originalEmail;
    }

    public void setOriginalEmail(String originalEmail) {
        this.originalEmail = originalEmail;
    }

    public String getOriginalFirstName() {
        return originalFirstName;
    }

    public void setOriginalFirstName(String originalFirstName) {
        this.originalFirstName = originalFirstName;
    }

    public String getOriginalLastName() {
        return originalLastName;
    }

    public void setOriginalLastName(String originalLastName) {
        this.originalLastName = originalLastName;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public String getArchivedByAdminEmail() {
        return archivedByAdminEmail;
    }

    public void setArchivedByAdminEmail(String archivedByAdminEmail) {
        this.archivedByAdminEmail = archivedByAdminEmail;
    }
}
