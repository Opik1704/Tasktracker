package com.site.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeamCreateDto {

    @NotBlank(message = "Название команды обязательно")
    private String name;

    public TeamCreateDto() {
    }

    public TeamCreateDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
