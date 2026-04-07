package com.site.webapp.service;

import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    public TaskService(TaskRepository taskRepository, UserRepository userRepository,NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }


    public List<Task> getAllTasks(String sort, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(
                    search.trim(), search.trim());
        }
        return getSortedTasks(sort);
    }

    private List<Task> getSortedTasks(String sort){
        return switch (sort) {
            case "deadline" -> taskRepository.findAllByOrderByDeadlineDesc();
            case "priority" -> taskRepository.findAllByOrderByPriorityAsc();
            case "id_desc" -> taskRepository.findAllByOrderByIdDesc();
            case "id_asc" -> taskRepository.findAllByOrderByIdAsc();
            default -> taskRepository.findAllByOrderByIdDesc();
        };
    }


    @Transactional
    public void saveTask(Task task,User initiator) {

        String author = (initiator != null && initiator.getEmail() != null) ? initiator.getEmail() : "Система";
        User currentUser = userRepository.findById(task.getArtistId()).orElse(null);

        String msg = author + " назначил вам задачу " + task.getTitle() + " с дедлайном " + task.getDeadline() + "с приоритетом" + task.getPriority();

        notificationService.send(currentUser,msg);

        taskRepository.save(task);

        log.info("Задача создана с ID: {} пользователем {}", task.getId(), author);
    }


    @Transactional
    public void updateTask(Task updatedTask, User initiator) {

        Task task = taskRepository.findById(updatedTask.getId()).orElseThrow(() -> new RuntimeException("Задача не найдена"));

        StringBuilder changes = new StringBuilder();

        String author = (initiator != null && initiator.getEmail() != null) ? initiator.getEmail() : "Система";

        if (!Objects.equals(task.getArtistId(), updatedTask.getArtistId())) {
            if (task.getArtistId() != null) {
                userRepository.findById(task.getArtistId()).ifPresent(oldUser ->{
                    String msg = author + " передал вашу задачу '" + task.getTitle() + " пользователю" + userRepository.findById(updatedTask.getArtistId()).map(User::getEmail).orElse("не назначен");
                    log.info("Уведомление старому исполнителю (ID {}): {}", task.getArtistId(), msg);
                    notificationService.send(oldUser, msg);
                });
            }
            if (updatedTask.getArtistId() != null) {
                userRepository.findById(updatedTask.getArtistId()).ifPresent(newUser ->
                {
                    String msg = author + " назначил вам задачу '" + task.getTitle() + "' пользователя" + userRepository.findById(task.getArtistId()).map(User::getEmail).orElse("никто");
                    notificationService.send(newUser, msg);
                });
            }
        }

        if (!Objects.equals(task.getDeadline(), updatedTask.getDeadline()) && Objects.equals(task.getArtistId(), updatedTask.getArtistId()) && task.getArtistId() != null) {
            userRepository.findById(task.getArtistId()).ifPresent(currentArtist -> {
                String msg = author + " изменил дедлайн задачи '" + task.getTitle() + "' на " + updatedTask.getDeadline();
                log.info("Лог изменения дедлайна {}", msg);
                notificationService.send(currentArtist, msg);
            });
        }

        if(!Objects.equals(task.getPriority(),updatedTask.getPriority()) && task.getArtistId() != null  ){
            userRepository.findById(task.getArtistId()).ifPresent(currentArtist ->{
                String msg = author + " изменил приоритет задачи '" + task.getTitle() + " '  на " + updatedTask.getPriority();
                log.info("Лог изменения приоритета {}", msg);
                notificationService.send(currentArtist,msg);
            });
        }

        task.setTitle(updatedTask.getTitle());
        task.setPriority(updatedTask.getPriority());
        task.setArtistId(updatedTask.getArtistId());
        task.setDeadline(updatedTask.getDeadline());
        task.setComment(updatedTask.getComment());

        taskRepository.save(task);
        log.info("Задача id {} успешно обновлена", task.getId());
    }

    @Transactional
    public void deleteTask(Long id, User initiator) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Задача не найдена"));

        String author = (initiator != null && initiator.getEmail() != null) ? initiator.getEmail()  : "Система";

        if (task.getArtistId() != null) {
            userRepository.findById(task.getArtistId()).ifPresent(artist -> {
                String msg = author + " удалил задачу '" + task.getTitle() + "', которая была назначена вам";
                log.info("Уведомление об удалении задачи ID {}: {}", id, msg);
                notificationService.send(artist, msg);
            });
        }

        taskRepository.deleteById(id);

        log.info("Задача id {} удалена пользователем {}",id,author);
    }


    @Transactional(readOnly = true)
    public List<Task> getSortedFavorites(String email, String sort) {
        User user = userRepository.findByEmail(email);
        if (user == null) return new ArrayList<>();

        List<Task> favorites = new ArrayList<>(user.getFavouriteTasks());

        return switch (sort != null ? sort : "default") {
            case "deadline" -> {
                favorites.sort(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())));
                yield favorites;
            }
            case "priority" -> {
                favorites.sort(Comparator.comparing(Task::getPriority));
                yield favorites;
            }
            case "recent" -> {
                favorites.sort(Comparator.comparing(Task::getId).reversed());
                yield favorites;
            }
            default -> favorites;
        };
    }

    @Transactional
    public void toggleFavorite(String email, Long taskId) {
        log.info("Переключение избранного для пользователя {} и задачи {}", email, taskId);

        User user = userRepository.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("Пользователь не найден");

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        if (user.getFavouriteTasks().contains(task)) {
            user.getFavouriteTasks().remove(task);
            log.info("Пользователь {} удалил задачу {} из избранного", email, taskId);
        } else {
            user.getFavouriteTasks().add(task);
            log.info("Пользователь {} добавил задачу {} в избранное", email, taskId);
        }
    }


    public List<Task> getAllUserTasks(Long userId, String sort, String search){
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(
                    userId,search.trim(),userId,search.trim());
        }
        return getSortedUserTask(userId, sort);
    }

    private List<Task> getSortedUserTask(Long userId, String sort){
        return switch (sort) {
            case "deadline" -> taskRepository.findByArtistIdOrderByDeadlineAsc(userId);
            case "priority" -> taskRepository.findByArtistIdOrderByPriorityAsc(userId);
            case "id_desc" -> taskRepository.findByArtistIdOrderByIdDesc(userId);
            case "id_asc" -> taskRepository.findByArtistIdOrderByIdAsc(userId);
            default -> taskRepository.findAllByOrderByIdDesc();
        };
    }
}
