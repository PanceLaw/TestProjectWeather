package ru.qa.weather.steps;

import ru.qa.weather.model.Condition;
import ru.qa.weather.model.Current;
import ru.qa.weather.model.Location;
import ru.qa.weather.model.CurrentResponse;

import java.util.Map;

final class ExpectedResponseFactory {
    private ExpectedResponseFactory() {
    }

    static CurrentResponse from(Map<String, String> expected) {
        return new CurrentResponse(location(expected), current(expected));
    }

    private static Location location(Map<String, String> expected) {
        return new Location(
                expected.get("location.name"),
                expected.get("location.country"),
                expected.get("location.tz_id")
        );
    }

    private static Current current(Map<String, String> expected) {
        return new Current(
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
