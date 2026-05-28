package ru.qa.weather.client;

import ru.qa.weather.exception.ServiceException;

import java.net.URI;
import java.net.http.HttpClient;

public class ApiClient {
    private final URI baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;

    public ApiClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, HttpClient.newHttpClient());
    }

    public ApiClient(String baseUrl, String apiKey, HttpClient httpClient) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ServiceException("Base URL is empty");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new ServiceException("API key is empty");
        }
        if (httpClient == null) {
            throw new ServiceException("HTTP client is empty");
        }
        this.baseUrl = URI.create(baseUrl);
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    public URI baseUrl() {
        return baseUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public HttpClient httpClient() {
        return httpClient;
    }
}
