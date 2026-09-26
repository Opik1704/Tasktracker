package com.site.webapp.service;

import com.site.webapp.models.Task;
import com.site.webapp.repo.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TaskPlanService {
    private static final Logger log = LoggerFactory.getLogger(TaskPlanService.class);

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public TaskPlanService(TaskRepository taskRepository,
                           NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    public int sendWeeklyPlan() {
        log.info("Формирование плана на неделю");
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);

        return processTasks(start, end, "Ваш план на неделю: у вас {} задач(и)");
    }

    public int sendDailyPlan(){
        log.info("Формирование списка задач на день");
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.with(LocalTime.MAX);

        return processTasks(start, end, "Сегодня нужно завершить {} задач");
    }

    private int processTasks(LocalDateTime start, LocalDateTime end, String messageTemplate) {
        log.info("Загрузка задач и пользователей за период {} - {}", start, end);

        List<Task> allTasks = taskRepository.findAllByDeadlineBetween(start, end);

        if (allTasks.isEmpty()) {
            log.info("Нет задач в этом периоде");
            return 0;
        }

        Map<Long, List<Task>> tasksByUserId = allTasks.stream()
                .filter(task -> task.getArtistId() != null)
                .collect(Collectors.groupingBy(Task::getArtistId));

        for (Map.Entry<Long, List<Task>> entry : tasksByUserId.entrySet()) {
            Long userId = entry.getKey();
            int count = entry.getValue().size();

            String finalMessage = messageTemplate.replace("{}", String.valueOf(count));
            notificationService.createNotification(userId, finalMessage);

            log.info("Создано планируемое уведомление для пользователя ID {}: {} задач", userId, count);
        }

        log.info(" Обработано {} пользователей с задачами", tasksByUserId.size());
        return tasksByUserId.size();
    }
}
