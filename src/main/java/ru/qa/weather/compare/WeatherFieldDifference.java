package ru.qa.weather.compare;

public record WeatherFieldDifference(String field, Object expected, Object actual) {
}
