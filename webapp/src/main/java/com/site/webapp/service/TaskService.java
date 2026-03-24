package com.site.webapp.service;

import com.site.webapp.models.Tasks;
import com.site.webapp.models.User;
import com.site.webapp.repo.TasksRepository;
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
    private final TasksRepository tasksRepository;
    private final UserRepository userRepository;

    public TaskService(TasksRepository tasksRepository,UserRepository userRepository) {
        this.tasksRepository = tasksRepository;
        this.userRepository = userRepository;
    }


    public List<Tasks> getAllTasks(String sort, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return tasksRepository.findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(
                    search.trim(), search.trim());
        }
        return getSortedTasks(sort);
    }

    private List<Tasks> getSortedTasks(String sort){
        return switch (sort) {
            case "deadline" -> tasksRepository.findAllByOrderByDeadlineDesc();
            case "priority" -> tasksRepository.findAllByOrderByPriorityAsc();
            case "id_desc" -> tasksRepository.findAllByOrderByIdDesc();
            case "id_asc" -> tasksRepository.findAllByOrderByIdAsc();
            default -> tasksRepository.findAllByOrderByIdDesc();
        };
    }

    @Transactional
    public void saveTask(Tasks task) {
        tasksRepository.save(task);
        log.info("✅ Задача создана с ID: {}", task.getId());
    }

    @Transactional
    public void updateTask(Long id, String title, String priority, Long artistId, LocalDateTime deadline, String comment) {

        Tasks task = tasksRepository.findById(id).orElseThrow(() -> new RuntimeException("Задача не найдена"));

        task.setTitle(title);
        task.setPriority(priority);
        task.setArtistId(artistId);
        task.setDeadline(deadline);
        task.setComment(comment);

        tasksRepository.save(task);
        log.info("Задача id {} успешно обновлена", id);

    }
    @Transactional
    public void deleteTask(Long id) {
        tasksRepository.deleteById(id);
        log.info("Задача id {} удалена",id);
    }

    @Transactional(readOnly = true)
    public List<Tasks> getSortedFavorites(String email, String sort) {
        User user = userRepository.findByEmail(email);
        if (user == null) return new ArrayList<>();

        List<Tasks> favorites = new ArrayList<>(user.getFavouriteTasks());

        return switch (sort != null ? sort : "default") {
            case "deadline" -> {
                favorites.sort(Comparator.comparing(Tasks::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())));
                yield favorites;
            }
            case "priority" -> {
                favorites.sort(Comparator.comparing(Tasks::getPriority));
                yield favorites;
            }
            case "recent" -> {
                favorites.sort(Comparator.comparing(Tasks::getId).reversed());
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

        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        if (user.getFavouriteTasks().contains(task)) {
            user.getFavouriteTasks().remove(task);
            log.info("Пользователь {} удалил задачу {} из избранного", email, taskId);
        } else {
            user.getFavouriteTasks().add(task);
            log.info("Пользователь {} добавил задачу {} в избранное", email, taskId);
        }
    }
    public List<Tasks> getAllUserTasks(Long userId, String sort,String search){
        if (search != null && !search.trim().isEmpty()) {
            return tasksRepository.findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(
                    userId,search.trim(),userId,search.trim());
        }
        return getSortedUserTask(userId, sort);
    }
    private List<Tasks> getSortedUserTask(Long userId,String sort){
        return switch (sort) {
            case "deadline" -> tasksRepository.findByArtistIdOrderByDeadlineAsc(userId);
            case "priority" -> tasksRepository.findByArtistIdOrderByPriorityAsc(userId);
            case "id_desc" -> tasksRepository.findByArtistIdOrderByIdDesc(userId);
            case "id_asc" -> tasksRepository.findByArtistIdOrderByIdAsc(userId);
            default -> tasksRepository.findAllByOrderByIdDesc();
        };
    }
}
