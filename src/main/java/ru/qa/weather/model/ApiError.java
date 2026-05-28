package ru.qa.weather.model;

public record ApiError(
        int code,
        String message
) {
}
