package ru.qa.weather.client;

import ru.qa.weather.exception.WeatherApiException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WeatherApiClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final URI baseUri;
    private final String apiKey;
    private final HttpClient httpClient;

    public WeatherApiClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, HttpClient.newHttpClient());
    }

    public WeatherApiClient(String baseUrl, String apiKey, HttpClient httpClient) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new WeatherApiException("Bazovyy url pustoy");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new WeatherApiException("Api key pustoy");
        }
        if (httpClient == null) {
            throw new WeatherApiException("Http client pustoy");
        }
        this.baseUri = URI.create(baseUrl);
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    public WeatherApiHttpResponse getCurrentWeather(String city) {
        if (city == null || city.isBlank()) {
            throw new WeatherApiException("Gorod pustoy");
        }

        String query = "key=%s&q=%s&aqi=no".formatted(encode(apiKey), encode(city));
        return sendGet("/v1/current.json", query);
    }

    public WeatherApiHttpResponse sendGet(String path, String query) {
        if (path == null || path.isBlank()) {
            throw new WeatherApiException("Put zaprosa pustoy");
        }
        if (query == null || query.isBlank()) {
            throw new WeatherApiException("Query zaprosa pustoy");
        }

        URI requestUri = baseUri.resolve(path + "?" + query);
        HttpRequest request = HttpRequest.newBuilder(requestUri)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new WeatherApiHttpResponse(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new WeatherApiException("Zapros zavershilsya oshibkoy: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherApiException("Zapros prervan", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
