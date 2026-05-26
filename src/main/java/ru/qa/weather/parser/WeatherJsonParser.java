package ru.qa.weather.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.qa.weather.exception.WeatherApiException;
import ru.qa.weather.model.WeatherErrorResponse;
import ru.qa.weather.model.WeatherResponse;

public class WeatherJsonParser {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public WeatherResponse parseWeather(String json) {
        try {
            return objectMapper.readValue(json, WeatherResponse.class);
        } catch (JsonProcessingException e) {
            throw new WeatherApiException("Ne udalos rasparsit json pogody: " + e.getMessage(), e);
        }
    }

    public WeatherErrorResponse parseError(String json) {
        try {
            return objectMapper.readValue(json, WeatherErrorResponse.class);
        } catch (JsonProcessingException e) {
            throw new WeatherApiException("Ne udalos rasparsit json oshibki: " + e.getMessage(), e);
        }
    }
}
