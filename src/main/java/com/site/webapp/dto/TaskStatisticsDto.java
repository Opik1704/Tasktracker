package com.site.webapp.dto;

import com.site.webapp.models.Task;

/**
 * DTO для персональной статистики задач на главной странице.
 */
public class TaskStatisticsDto {

    private long totalCompleted;
    private double averageCompletionHours;
    private Task fastestCompletedTask;
    private long completedThisWeek;
    private long completedThisMonth;
    private double onTimePercentage;

    public TaskStatisticsDto() {
    }

    public TaskStatisticsDto(long totalCompleted, double averageCompletionHours,
                             Task fastestCompletedTask, long completedThisWeek,
                             long completedThisMonth, double onTimePercentage) {
        this.totalCompleted = totalCompleted;
        this.averageCompletionHours = averageCompletionHours;
        this.fastestCompletedTask = fastestCompletedTask;
        this.completedThisWeek = completedThisWeek;
        this.completedThisMonth = completedThisMonth;
        this.onTimePercentage = onTimePercentage;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(long totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public double getAverageCompletionHours() {
        return averageCompletionHours;
    }

    public void setAverageCompletionHours(double averageCompletionHours) {
        this.averageCompletionHours = averageCompletionHours;
    }

    public Task getFastestCompletedTask() {
        return fastestCompletedTask;
    }

    public void setFastestCompletedTask(Task fastestCompletedTask) {
        this.fastestCompletedTask = fastestCompletedTask;
    }

    public long getCompletedThisWeek() {
        return completedThisWeek;
    }

    public void setCompletedThisWeek(long completedThisWeek) {
        this.completedThisWeek = completedThisWeek;
    }

    public long getCompletedThisMonth() {
        return completedThisMonth;
    }

    public void setCompletedThisMonth(long completedThisMonth) {
        this.completedThisMonth = completedThisMonth;
    }

    public double getOnTimePercentage() {
        return onTimePercentage;
    }

    public void setOnTimePercentage(double onTimePercentage) {
        this.onTimePercentage = onTimePercentage;
    }
}
