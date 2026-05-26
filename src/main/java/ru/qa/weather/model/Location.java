package ru.qa.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Location(
        String name,
        String country,
        @JsonProperty("tz_id") String tzId
) {
}
