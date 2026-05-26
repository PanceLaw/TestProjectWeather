package ru.qa.weather.model;

public record Condition(
        String text,
        String icon,
        int code
) {
}
