package ru.qa.weather.compare;

import java.util.Objects;

public record WeatherFieldComparison(String field, Object expected, Object actual) {
    public boolean matches() {
        return Objects.equals(expected, actual);
    }

    public WeatherFieldDifference difference() {
        return new WeatherFieldDifference(field, expected, actual);
    }

    public String toLogLine() {
        String status = matches() ? "OK" : "DIFF";
        return "%s %s: expected=%s, actual=%s".formatted(status, field, expected, actual);
    }
}
