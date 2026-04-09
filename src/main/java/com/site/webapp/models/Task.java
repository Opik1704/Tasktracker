package com.site.webapp.models;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Название задачи не может быть пустым")
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Укажите приоритет")
    private String priority;

    private Long artistId;

    private Long ownerId;

    @Future(message = "Дедлайн не может быть в прошлом")
    @NotNull(message = "Дата дедлайна обязательна")
    private LocalDateTime deadline;

    @Size(max = 500, message = "Комментарий слишком длинный")
    private String comment;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 255, unique = true)
    private String storedFileName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.NEW;

    public enum TaskStatus {
        NEW, IN_PROGRESS,TESTING, REVIEW, COMPLETED
    }

public Task() {
}
    public Task(String title, String priority, Long artistId,Long ownerId, LocalDateTime deadline,TaskStatus status, String comment,String originalFileName,String storedFileName){
        this.title = title;
        this.priority = priority;
        this.artistId = artistId;
        this.ownerId = ownerId;
        this.deadline = deadline;
        this.comment = comment;
        this.status = status;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public String getTitle() {return title;}
    public void setTitle(String title) {
        this.title = title;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getArtistId() {
        return artistId;
    }
    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }
    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public LocalDateTime getDeadline() {
        return deadline;
    }
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
