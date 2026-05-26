package ru.qa.weather.compare;

import ru.qa.weather.model.WeatherResponse;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WeatherComparator {
    private static final List<ComparableWeatherField> COMPARABLE_FIELDS = List.of(
            new ComparableWeatherField("location.name", response -> response.location().name()),
            new ComparableWeatherField("location.country", response -> response.location().country()),
            new ComparableWeatherField("location.tz_id", response -> response.location().tzId()),
            new ComparableWeatherField("current.temp_c", response -> response.current().tempC()),
            new ComparableWeatherField("current.condition.text", response -> response.current().condition().text()),
            new ComparableWeatherField("current.condition.code", response -> response.current().condition().code()),
            new ComparableWeatherField("current.wind_kph", response -> response.current().windKph()),
            new ComparableWeatherField("current.humidity", response -> response.current().humidity()),
            new ComparableWeatherField("current.cloud", response -> response.current().cloud()),
            new ComparableWeatherField("current.feelslike_c", response -> response.current().feelslikeC()),
            new ComparableWeatherField("current.uv", response -> response.current().uv())
    );

    public List<WeatherFieldDifference> compare(WeatherResponse expected, WeatherResponse actual) {
        return compareFields(expected, actual).stream()
                .filter(comparison -> !comparison.matches())
                .map(WeatherFieldComparison::difference)
                .toList();
    }

    public List<WeatherFieldComparison> compareFields(WeatherResponse expected, WeatherResponse actual) {
        return COMPARABLE_FIELDS.stream()
                .map(field -> field.compare(expected, actual))
                .toList();
    }

    public String buildLog(WeatherResponse expected, WeatherResponse actual) {
        return compareFields(expected, actual).stream()
                .map(WeatherFieldComparison::toLogLine)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private record ComparableWeatherField(String name, Function<WeatherResponse, Object> extractor) {
        private WeatherFieldComparison compare(WeatherResponse expected, WeatherResponse actual) {
            return new WeatherFieldComparison(name, extractor.apply(expected), extractor.apply(actual));
        }
    }
}
