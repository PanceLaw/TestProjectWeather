package ru.qa.weather.model;

public record WeatherError(
        int code,
        String message
) {
}
