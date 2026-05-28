package ru.qa.weather.controller;

import ru.qa.weather.client.ApiClient;
import ru.qa.weather.client.ApiResponse;
import ru.qa.weather.exception.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Controller {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final ApiClient client;

    public Controller(ApiClient client) {
        if (client == null) {
            throw new ServiceException("API client is empty");
        }
        this.client = client;
    }

    public ApiResponse getCurrent(String city) {
        if (city == null || city.isBlank()) {
            throw new ServiceException("City is empty");
        }

        String query = "key=%s&q=%s&aqi=no".formatted(encode(client.apiKey()), encode(city));
        return sendGet("/v1/current.json", query);
    }

    public ApiResponse sendGet(String path, String query) {
        if (path == null || path.isBlank()) {
            throw new ServiceException("Request path is empty");
        }
        if (query == null || query.isBlank()) {
            throw new ServiceException("Request query is empty");
        }

        URI requestUri = client.baseUrl().resolve(path + "?" + query);
        HttpRequest request = HttpRequest.newBuilder(requestUri)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.httpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return new ApiResponse(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new ServiceException("Request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Request interrupted", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
