package ru.qa.weather.compare;

import ru.qa.weather.model.CurrentResponse;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResponseComparator {
    private static final List<ComparedField> COMPARED_FIELDS = List.of(
            new ComparedField("location.name", response -> response.location().name()),
            new ComparedField("location.country", response -> response.location().country()),
            new ComparedField("location.tz_id", response -> response.location().tzId()),
            new ComparedField("current.temp_c", response -> response.current().tempC()),
            new ComparedField("current.condition.text", response -> response.current().condition().text()),
            new ComparedField("current.condition.code", response -> response.current().condition().code()),
            new ComparedField("current.wind_kph", response -> response.current().windKph()),
            new ComparedField("current.humidity", response -> response.current().humidity()),
            new ComparedField("current.cloud", response -> response.current().cloud()),
            new ComparedField("current.feelslike_c", response -> response.current().feelslikeC()),
            new ComparedField("current.uv", response -> response.current().uv())
    );

    public ComparisonResult compare(CurrentResponse expected, CurrentResponse actual) {
        List<FieldResult> fields = compareFields(expected, actual);
        return new ComparisonResult(fields);
    }

    private List<FieldResult> compareFields(CurrentResponse expected, CurrentResponse actual) {
        return COMPARED_FIELDS.stream()
                .map(field -> field.compare(expected, actual))
                .toList();
    }

    private record ComparedField(String name, Function<CurrentResponse, Object> extractor) {
        private FieldResult compare(CurrentResponse expected, CurrentResponse actual) {
            return new FieldResult(name, extractor.apply(expected), extractor.apply(actual));
        }
    }

    private record FieldResult(String name, Object expected, Object actual) {
        private boolean matches() {
            return Objects.equals(expected, actual);
        }

        private String toLogLine() {
            String status = matches() ? "OK" : "DIFF";
            return "%s %s: expected=%s, actual=%s".formatted(status, name, expected, actual);
        }
    }

    public static final class ComparisonResult {
        private final List<FieldResult> fields;

        private ComparisonResult(List<FieldResult> fields) {
            this.fields = fields;
        }

        public boolean hasDifferences() {
            return fields.stream().anyMatch(field -> !field.matches());
        }

        public String log() {
            return fields.stream()
                    .map(FieldResult::toLogLine)
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
