package ru.qa.weather.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.qa.weather.exception.ServiceException;
import ru.qa.weather.model.CurrentResponse;
import ru.qa.weather.model.ErrorResponse;

public class ResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public CurrentResponse parseCurrent(String json) {
        try {
            return objectMapper.readValue(json, CurrentResponse.class);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Current response JSON parsing failed: " + e.getMessage(), e);
        }
    }

    public ErrorResponse parseError(String json) {
        try {
            return objectMapper.readValue(json, ErrorResponse.class);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error response JSON parsing failed: " + e.getMessage(), e);
        }
    }
}
