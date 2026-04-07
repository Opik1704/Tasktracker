package com.site.webapp.service;

import com.site.webapp.controllers.LoggingController;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class TaskReminder extends LoggingController {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

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
            User user = userRepository.findById(task.getArtistId()).orElse(null);
            if (user != null) {
                notificationService.send(user, "До дедлайна задачи '" + task.getTitle() + "' осталось меньше 2 часов!");
            }
        }
    }

    private void processTasks(LocalDateTime start, LocalDateTime end, String messageTemplate) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            List<Task> userTasks = taskRepository.findAllByArtistIdAndDeadlineBetween(user.getId(), start, end);

            if (!userTasks.isEmpty()) {
                int count = userTasks.size();
                String finalMessage = messageTemplate.replace("{}", String.valueOf(count));

                notificationService.send(user, finalMessage);
                log.info("Отправлено напоминание для {}: {} задач", user.getEmail(), count);
            }
        }
    }
}
