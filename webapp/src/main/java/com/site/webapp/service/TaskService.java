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

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
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
    public void saveTask(Task task) {
        taskRepository.save(task);
        log.info("✅ Задача создана с ID: {}", task.getId());
    }

    @Transactional
    public void updateTask(Long id, String title, String priority, Long artistId, LocalDateTime deadline, String comment) {

        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Задача не найдена"));

        task.setTitle(title);
        task.setPriority(priority);
        task.setArtistId(artistId);
        task.setDeadline(deadline);
        task.setComment(comment);

        taskRepository.save(task);
        log.info("Задача id {} успешно обновлена", id);

    }
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
        log.info("Задача id {} удалена",id);
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
