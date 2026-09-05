package com.site.webapp.service;

import com.site.webapp.controllers.LoggingController;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TaskReminder {
    private static final Logger log = LoggerFactory.getLogger(TaskReminder.class);
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    public TaskReminder(TaskRepository taskRepository,
                        UserRepository userRepository,
                        NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyPlan() {
        log.info("Формирование плана на неделю");
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);

        processTasks(start, end, "Ваш план на неделю: у вас {} задач(и)");
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyPlan(){
        log.info("Формирование списка задач на день");
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.with(LocalTime.MAX);
        processTasks(start, end, "Сегодня нужно завершить {} задач");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sendUrgentReminders(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);
        List<Task> urgentTasks = taskRepository.findAllByDeadlineBetween(now, twoHoursLater);

        for (Task task : urgentTasks) {
            if (task.getArtistId() == null) {
                continue;
            }
            User user = userRepository.findById(task.getArtistId()).orElse(null);
            if (user != null) {
                notificationService.send(user, "До дедлайна задачи '" + task.getTitle() + "' осталось меньше 2 часов!");
            }
        }
    }

    private void processTasks(LocalDateTime start, LocalDateTime end, String messageTemplate) {
        log.info("Загрузка задач и пользователей за период {} - {}", start, end);

        List<Task> allTasks = taskRepository.findAllByDeadlineBetween(start, end);

        if (allTasks.isEmpty()) {
            log.info("Нет задач в этом периоде");
            return;
        }

        List<User> allUsers = userRepository.findAll();
        Map<Long, User> usersById = allUsers.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Map<Long, List<Task>> tasksByUserId = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getArtistId));

        for (Map.Entry<Long, List<Task>> entry : tasksByUserId.entrySet()) {
            Long userId = entry.getKey();
            User user = usersById.get(userId);

            if (user != null) {
                int count = entry.getValue().size();
                String finalMessage = messageTemplate.replace("{}", String.valueOf(count));
                notificationService.send(user, finalMessage);
                log.info("Уведомление {}: {} задач", user.getEmail(), count);
            }
        }

        log.info(" Обработано {} пользователей с задачами", tasksByUserId.size());
    }
}
