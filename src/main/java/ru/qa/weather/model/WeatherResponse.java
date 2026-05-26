package ru.qa.weather.model;

public record WeatherResponse(
        Location location,
        CurrentWeather current
) {
}
