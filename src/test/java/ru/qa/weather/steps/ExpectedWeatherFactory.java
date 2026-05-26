package ru.qa.weather.steps;

import ru.qa.weather.model.Condition;
import ru.qa.weather.model.CurrentWeather;
import ru.qa.weather.model.Location;
import ru.qa.weather.model.WeatherResponse;

import java.util.Map;

final class ExpectedWeatherFactory {
    private ExpectedWeatherFactory() {
    }

    static WeatherResponse from(Map<String, String> expected) {
        return new WeatherResponse(location(expected), currentWeather(expected));
    }

    private static Location location(Map<String, String> expected) {
        return new Location(
                expected.get("location.name"),
                expected.get("location.country"),
                expected.get("location.tz_id")
        );
    }

    private static CurrentWeather currentWeather(Map<String, String> expected) {
        return new CurrentWeather(
                doubleValue(expected, "current.temp_c"),
                new Condition(
                        expected.get("current.condition.text"),
                        null,
                        intValue(expected, "current.condition.code")
                ),
                doubleValue(expected, "current.wind_kph"),
                intValue(expected, "current.humidity"),
                intValue(expected, "current.cloud"),
                doubleValue(expected, "current.feelslike_c"),
                doubleValue(expected, "current.uv")
        );
    }

    private static int intValue(Map<String, String> source, String key) {
        return Integer.parseInt(source.get(key));
    }

    private static double doubleValue(Map<String, String> source, String key) {
        return Double.parseDouble(source.get(key));
    }
}
