package com.kouetcha.dto.tasksmanager;

import lombok.Data;

@Data
public class ApiResponseSimple {
    private String message;

    public ApiResponseSimple(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
