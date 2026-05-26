package ru.qa.weather.client;

public record WeatherApiHttpResponse(int statusCode, String body) {
}
