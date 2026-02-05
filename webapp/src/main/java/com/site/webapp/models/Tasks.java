package com.site.webapp.models;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String title;

    public Tasks() {
    }
        public Tasks(String title, String priority, Long artistId, LocalDateTime deadline, String comment){
        this.title = title;
        this.priority = priority;
        this.artistId = artistId;
        this.deadline = deadline;
        this.comment = comment;
    }

    private LocalDateTime deadline;
    private String priority;
    private Long artistId;
    private String comment;

    public String getTitle() {
        return title;
    }
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
        return title;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
}
