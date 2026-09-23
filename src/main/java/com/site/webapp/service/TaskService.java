package com.site.webapp.service;

import com.site.webapp.events.TaskCreatedEvent;
import com.site.webapp.events.TaskDeletedEvent;
import com.site.webapp.events.TaskUpdatedEvent;
import com.site.webapp.exception.TaskNotFoundException;
import com.site.webapp.exception.UnauthorizedAccessException;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAttachmentService taskAttachmentService;
    private final ApplicationEventPublisher eventPublisher;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       TaskAttachmentService taskAttachmentService,
                       ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskAttachmentService = taskAttachmentService;
        this.eventPublisher = eventPublisher;
    }


    @Transactional
    public Task createTask(Task task, CustomUserDetails userDetails) {

        task.setStatus(Task.TaskStatus.NEW);
        task.setOwnerId(userDetails.getId());
        taskRepository.save(task);
        eventPublisher.publishEvent(new TaskCreatedEvent(
                task.getId(),
                task.getTitle(),
                task.getDeadline(),
                task.getPriority(),
                task.getArtistId(),
                userDetails.getFullName())
        );

        log.info("Задача создана с ID: {} пользователем {}", task.getId(), userDetails.getFullName());
        return task;
    }


    @Transactional(readOnly = true)
    public List<Task> getAllTasks(String sort, String search) {
        log.debug("Запрос списка всех задач. Фильтр: '{}', Сортировка: '{}'", search, sort);
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(
                    search.trim(), search.trim());
        }
        return getSortedTasks(sort);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllUserTasks(Long userId, String sort, String search){
        log.debug("Запрос задач исполнителя [User ID: {}]. Фильтр: '{}', Сортировка: '{}'", userId, search, sort);
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(
                    userId,search.trim(),userId,search.trim());
        }
        return getSortedUserTask(userId, sort);
    }

    @Transactional(readOnly = true)
    public List<Task> getSortedFavorites(String email, String sort) {
        log.debug("Запрос избранных задач для [Email: {}]", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return getSortedFavoriteTasks(new ArrayList<>(user.getFavouriteTasks()), sort);
    }


    @Transactional
    public void updateTask(Task updatedTask, CustomUserDetails userDetails) {

        Task task = taskRepository.findById(updatedTask.getId()).orElseThrow(() -> new TaskNotFoundException(updatedTask.getId()));

        validateTaskAccess(task, userDetails, "TASK_EDIT");

        if (updatedTask.getVersion() != null) {
            task.setVersion(updatedTask.getVersion());
        }

        Long oldArtistId = task.getArtistId();
        LocalDateTime oldDeadline = task.getDeadline();
        Task.Priority oldPriority = task.getPriority();

        task.setTitle(updatedTask.getTitle());
        task.setPriority(updatedTask.getPriority());
        task.setArtistId(updatedTask.getArtistId());
        task.setDeadline(updatedTask.getDeadline());
        task.setComment(updatedTask.getComment());
        task.setStatus(updatedTask.getStatus());

        eventPublisher.publishEvent(new TaskUpdatedEvent(
                task.getId(),
                task.getTitle(),
                userDetails.getFullName(),
                oldArtistId,
                task.getArtistId(),
                oldDeadline,
                task.getDeadline(),
                oldPriority,
                task.getPriority()
        ));

        taskRepository.save(task);

        log.info("Задача id {} успешно обновлена", task.getId());
    }

    @Transactional
    public void softDeleteTask(Long taskId, CustomUserDetails userDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        validateTaskAccess(task, userDetails, "TASK_DELETE");

        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());

        taskRepository.save(task);

        log.info("Задача id {} перемещена в корзину пользователем {}", taskId, userDetails.getFullName());
    }

    @Transactional
    public void restoreTask(Long taskId, CustomUserDetails userDetails) {
        Task task = taskRepository.findByIdAndOwnerIdAndDeletedTrue(taskId, userDetails.getId())
                .orElseThrow(() -> new TaskNotFoundException("Задача в корзине не найдена"));

        validateTaskAccess(task, userDetails, "TASK_DELETE");

        task.setDeleted(false);
        task.setDeletedAt(null);
        taskRepository.save(task);

        log.info("Задача id {} успешно восстановлена из корзины пользователем {}", taskId, userDetails.getFullName());
    }

    @Transactional(readOnly = true)
    public List<Task> getTrashTasks(Long ownerId, CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getId();

        boolean isSelf = currentUserId.equals(ownerId);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSelf && !isAdmin) {
            log.warn("Пользователь {} пытался получить доступ к корзине пользователя {}", userDetails.getUsername(), ownerId);
            throw new UnauthorizedAccessException("У вас нет прав для просмотра корзины этого пользователя");
        }
        log.debug("Запрос задач из корзины для владельца Owner ID: {}", ownerId);
        return taskRepository.findAllByOwnerIdAndDeletedTrue(ownerId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('TASK_DELETE_PERMANENT') or hasRole('ADMIN')")
    public void hardDeleteTask(Long taskId, CustomUserDetails userDetails) {
        Task task = taskRepository.findByIdAndDeletedTrue(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        String authorName = userDetails.getFullName();

        eventPublisher.publishEvent(new TaskDeletedEvent(
                task.getId(),
                task.getTitle(),
                task.getArtistId(),
                userDetails.getFullName()
        ));

        taskAttachmentService.deleteAllByTaskId(taskId);

        taskRepository.deleteByIdAndDeletedTrue(taskId);

        log.info("Задача id {} удалена пользователем {}",taskId, userDetails.getFullName());
    }


    private void validateTaskAccess(Task task, CustomUserDetails userDetails, String requiredPermission) {
        Long userId = userDetails.getId();

        boolean isOwner = task.getOwnerId() != null && task.getOwnerId().equals(userId);
        boolean isArtist = task.getArtistId() != null && task.getArtistId().equals(userId);

        if (isOwner || isArtist) {
            return;
        }

        boolean hasPermission = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredPermission) ||
                        a.getAuthority().equals("ROLE_ADMIN"));

        if (!hasPermission) {
            log.warn("Отказ в доступе к задаче {} пользователю {}", task.getId(), userDetails.getUsername());
            throw new UnauthorizedAccessException("У вас нет прав для выполнения этой операции над задачей");
        }
    }


    @Transactional
    public void toggleFavorite(String email, Long taskId) {
        log.info("Переключение избранного для пользователя {} и задачи {}", email, taskId);

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        if (user.getFavouriteTasks().contains(task)) {
            user.getFavouriteTasks().remove(task);
            log.info("Пользователь {} удалил задачу {} из избранного", email, taskId);
        } else {
            user.getFavouriteTasks().add(task);
            log.info("Пользователь {} добавил задачу {} в избранное", email, taskId);
        }
    }

    private List<Task> getSortedTasks(String sortParam) {
        Sort sort = switch (sortParam != null ? sortParam : "default") {
            case "status_asc"  -> Sort.by(Sort.Direction.ASC, "status");
            case "status_desc" -> Sort.by(Sort.Direction.DESC, "status");
            case "deadline"    -> Sort.by(Sort.Direction.ASC, "deadline");
            case "priority"    -> Sort.by(Sort.Direction.ASC, "priority");
            case "created_asc"  -> Sort.by(Sort.Direction.ASC, "createdAt");
            default            -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        return taskRepository.findAll(sort);
    }

    private List<Task> getSortedUserTask(Long userId, String sortParam){
        Sort sort = switch (sortParam != null ? sortParam : "default"){
            case "status_asc"  -> Sort.by(Sort.Direction.ASC,"status");
            case "status_desc" -> Sort.by(Sort.Direction.DESC, "status");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            case "priority" -> Sort.by(Sort.Direction.ASC, "priority");
            case "created_asc"  -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        return taskRepository.findByArtistId(userId, sort);
    }

    private List<Task> getSortedFavoriteTasks(List<Task> favorites, String sort) {
        Comparator<Task> comparator = switch (sort != null ? sort : "default") {
            case "status_asc"  -> Comparator.comparing(Task::getStatus);
            case "status_desc" -> Comparator.comparing(Task::getStatus).reversed();
            case "deadline" -> Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
            case "priority" -> Comparator.comparing(Task::getPriority, Comparator.nullsLast(Comparator.naturalOrder()));
            case "recent"   -> Comparator.comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
            default         -> null;
        };

        if (comparator == null) {
            return favorites;
        }

        return favorites.stream()
                .sorted(comparator)
                .toList();
    }
}
