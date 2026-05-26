package ru.qa.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrentWeather(
        @JsonProperty("temp_c") double tempC,
        Condition condition,
        @JsonProperty("wind_kph") double windKph,
        int humidity,
        int cloud,
        @JsonProperty("feelslike_c") double feelslikeC,
        double uv
) {
}
