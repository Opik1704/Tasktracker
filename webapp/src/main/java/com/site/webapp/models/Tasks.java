package com.site.webapp.models;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Название задачи не может быть пустым")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Укажите приоритет")
    private String priority;

    private Long artistId;

    @Future(message = "Дедлайн не может быть в прошлом")
    @NotNull(message = "Дата дедлайна обязательна")
    private LocalDateTime deadline;

    @Size(max = 500, message = "Комментарий слишком длинный")
    private String comment;

//    private String status;

public Tasks() {
}
    public Tasks(String title, String priority, Long artistId, LocalDateTime deadline, String comment){
        this.title = title;
        this.priority = priority;
        this.artistId = artistId;
        this.deadline = deadline;
        this.comment = comment;
//        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tasks task = (Tasks) o;
        return Objects.equals(id, task.id); // Сравнение по ID
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

//    public void setStatus(String status){this.status=status;}
//    public String getStatus(){return status;}

}
