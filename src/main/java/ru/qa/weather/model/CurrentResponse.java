package ru.qa.weather.model;

public record CurrentResponse(
        Location location,
        Current current
) {
}
