package com.site.webapp.dto;

import java.time.LocalDate;

/**
 * DTO количества завершённых задач за один день (для графика динамики).
 */
public class DailyCompletionDto {

    private LocalDate date;
    private long count;

    public DailyCompletionDto() {
    }

    public DailyCompletionDto(LocalDate date, long count) {
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}