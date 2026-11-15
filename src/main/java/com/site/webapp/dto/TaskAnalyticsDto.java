package com.site.webapp.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO аналитической панели задач пользователя.
 * Содержит агрегированные показатели за выбранный период и сравнение с предыдущим.
 */
public class TaskAnalyticsDto {

    private long totalTasks;
    private long activeTasks;
    private long completedTasks;
    private long overdueTasks;
    private double completionPercentage;

    private long previousPeriodCreated;
    private long previousPeriodCompleted;

    private List<DailyCompletionDto> dailyCompletions;

    public TaskAnalyticsDto() {
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(long activeTasks) {
        this.activeTasks = activeTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public long getPreviousPeriodCreated() {
        return previousPeriodCreated;
    }

    public void setPreviousPeriodCreated(long previousPeriodCreated) {
        this.previousPeriodCreated = previousPeriodCreated;
    }

    public long getPreviousPeriodCompleted() {
        return previousPeriodCompleted;
    }

    public void setPreviousPeriodCompleted(long previousPeriodCompleted) {
        this.previousPeriodCompleted = previousPeriodCompleted;
    }

    public List<DailyCompletionDto> getDailyCompletions() {
        return dailyCompletions;
    }

    public void setDailyCompletions(List<DailyCompletionDto> dailyCompletions) {
        this.dailyCompletions = dailyCompletions;
    }
}