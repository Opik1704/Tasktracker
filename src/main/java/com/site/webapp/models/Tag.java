package com.site.webapp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.hibernate.annotations.*;

@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tags_name", columnList = "name", unique = true)
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tags_seq_gen")
    @SequenceGenerator(name = "tags_seq_gen", sequenceName = "tags_seq", allocationSize = 50)
    private Long id;

    @NotBlank(message = "Название тега не может быть пустым")
    @Size(max = 50, message = "Название тега не может превышать 50 символов")
    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название тега не может быть пустым");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Название тега не может превышать 50 символов");
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // equals/hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id != null && Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}