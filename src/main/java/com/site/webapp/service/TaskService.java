package com.site.webapp.service;

import com.site.webapp.models.Task;
import com.site.webapp.models.TaskAttachment;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TaskAttachmentService taskAttachmentService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,NotificationService notificationService,TaskAttachmentService taskAttachmentService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.taskAttachmentService = taskAttachmentService;
    }


    @Transactional
    public Task createTask(Task task, User initiator) {
        task.setStatus(Task.TaskStatus.NEW);

        String authorName;
        if (initiator != null) {
            task.setOwnerId(initiator.getId());
            authorName = initiator.getFullName();
        } else {
            authorName = "Система";
        }

        String msg = authorName + " назначил(а) вам задачу " + task.getTitle() + " с дедлайном " + task.getDeadline() + " с приоритетом " + task.getPriority();
        if (task.getArtistId() != null) {
            notificationService.send(userRepository.findById(task.getArtistId()).orElse(null), msg);
        }
        taskRepository.save(task);
        log.info("Задача создана с ID: {} пользователем {}", task.getId(), authorName);
        return task;
    }


    @Transactional(readOnly = true)
    public List<Task> getAllTasks(String sort, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(
                    search.trim(), search.trim());
        }
        return getSortedTasks(sort);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllUserTasks(Long userId, String sort, String search){
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(
                    userId,search.trim(),userId,search.trim());
        }
        return getSortedUserTask(userId, sort);
    }

    @Transactional(readOnly = true)
    public List<Task> getSortedFavorites(String email, String sort) {
        User user = userRepository.findByEmail(email);
        if (user == null) return new ArrayList<>();

        return getSortedFavoriteTasks(new ArrayList<>(user.getFavouriteTasks()), sort);
    }


    @Transactional
    public void updateTask(Task updatedTask, User initiator) {

        Task task = taskRepository.findById(updatedTask.getId()).orElseThrow(() -> new RuntimeException("Задача не найдена"));

        String authorName = initiator != null ? initiator.getFullName() : "Система";

        if (!Objects.equals(task.getArtistId(), updatedTask.getArtistId())) {
            if (task.getArtistId() != null) {
                userRepository.findById(task.getArtistId()).ifPresent(oldUser ->{
                    String msg = authorName + " передал вашу задачу '" + task.getTitle() + "' пользователю " + userRepository.findById(updatedTask.getArtistId()).map(User::getEmail).orElse("не назначен");
                    log.info("Уведомление старому исполнителю (ID {}): {}", task.getArtistId(), msg);
                    notificationService.send(oldUser, msg);
                });
            }
            if (updatedTask.getArtistId() != null) {
                userRepository.findById(updatedTask.getArtistId()).ifPresent(newUser ->
                {
                    String msg = authorName + " назначил вам задачу '" + task.getTitle() + "' (ранее у: " + userRepository.findById(task.getArtistId()).map(User::getFullName).orElse("никто");
                    notificationService.send(newUser, msg);
                });
            }
        }

        if (!Objects.equals(task.getDeadline(), updatedTask.getDeadline()) && Objects.equals(task.getArtistId(), updatedTask.getArtistId()) && task.getArtistId() != null) {
            userRepository.findById(task.getArtistId()).ifPresent(currentArtist -> {
                String msg = authorName + " изменил дедлайн задачи '" + task.getTitle() + "' на " + updatedTask.getDeadline();
                log.info("Лог изменения дедлайна {}", msg);
                notificationService.send(currentArtist, msg);
            });
        }

        if(!Objects.equals(task.getPriority(),updatedTask.getPriority()) && task.getArtistId() != null  ){
            userRepository.findById(task.getArtistId()).ifPresent(currentArtist ->{
                String msg = authorName + " изменил приоритет задачи '" + task.getTitle() + " '  на " + updatedTask.getPriority();
                log.info("Лог изменения приоритета {}", msg);
                notificationService.send(currentArtist,msg);
            });
        }

        task.setTitle(updatedTask.getTitle());
        task.setPriority(updatedTask.getPriority());
        task.setArtistId(updatedTask.getArtistId());
        task.setDeadline(updatedTask.getDeadline());
        task.setComment(updatedTask.getComment());
        task.setStatus(updatedTask.getStatus());

        taskRepository.save(task);
        log.info("Задача id {} успешно обновлена", task.getId());
    }

//
//    @PostConstruct
//    public void init() {
//        try {
//            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
//            if (!Files.exists(root)) {
//                Files.createDirectories(root);
//                System.out.println("Папка для загрузок создана по пути: " + root);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Не удалось инициализировать папку для загрузок!", e);
//        }
//    }
//
//    public String saveFile(MultipartFile file) throws IOException {
//        if (file == null || file.isEmpty()) return null;
//
//        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
//        if (!Files.exists(root)) {
//            Files.createDirectories(root);
//        }
//
//        String resultFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//        Path filePath = root.resolve(resultFilename);
//
//        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//        return resultFilename;
//    }

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

        taskAttachmentService.deleteAllByTaskId(id);

        notificationService.deleteAllByTaskId(id);

        taskRepository.deleteById(id);

        log.info("Задача id {} удалена пользователем {}",id,author);
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
