package ru.qa.weather.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import ru.qa.weather.client.WeatherApiClient;
import ru.qa.weather.client.WeatherApiHttpResponse;
import ru.qa.weather.compare.WeatherComparator;
import ru.qa.weather.compare.WeatherFieldDifference;
import ru.qa.weather.exception.WeatherApiException;
import ru.qa.weather.model.WeatherErrorResponse;
import ru.qa.weather.model.WeatherResponse;
import ru.qa.weather.parser.WeatherJsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeatherApiSteps {
    private static final Logger LOGGER = Logger.getLogger(WeatherApiSteps.class.getName());
    private static final String API_KEY = "test-api-key";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private static WireMockServer wireMockServer;

    private final WeatherJsonParser parser = new WeatherJsonParser();
    private final WeatherComparator comparator = new WeatherComparator();

    private WeatherApiClient client;
    private WeatherApiHttpResponse response;

    @Before
    public void setUp() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
        }
        wireMockServer.resetAll();
        client = new WeatherApiClient(wireMockServer.baseUrl(), API_KEY);
    }

    @AfterAll
    public static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @Given("Mok dlya goroda {string} otdaet otvet iz faila {string}")
    public void weatherApiReturnsCurrentWeatherForCity(String city, String fixtureName) {
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/current.json"))
                .withQueryParam("key", equalTo(API_KEY))
                .withQueryParam("q", equalTo(city))
                .withQueryParam("aqi", equalTo("no"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", JSON_CONTENT_TYPE)
                        .withBody(readResource("wiremock/weather/" + fixtureName))));
    }

    @Given("Mok dlya puti {string} s parametrami {string} otdaet oshibku {int} iz faila {string}")
    public void weatherApiReturnsErrorFromFixture(String path, String query, int statusCode, String fixtureName) {
        var mappingBuilder = get(urlPathEqualTo(path));
        queryParams(query).forEach((name, value) -> mappingBuilder.withQueryParam(name, equalTo(value)));

        wireMockServer.stubFor(mappingBuilder.willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", JSON_CONTENT_TYPE)
                .withBody(readResource("wiremock/errors/" + fixtureName))));
    }

    @When("Klient zaprashivaet pogodu po gorodu {string}")
    public void clientRequestsCurrentWeatherForCity(String city) {
        response = client.getCurrentWeather(city);
    }

    @When("Klient delaet GET zapros v {string} s parametrami {string}")
    public void clientSendsGetRequest(String path, String query) {
        response = client.sendGet(path, query.replace("{apiKey}", encode(API_KEY)));
    }

    @Then("Status otveta {int}")
    public void responseStatusIs(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    @Then("Pogoda v otvete sovpadaet s ozhidaemoy")
    public void currentWeatherEquals(DataTable dataTable) {
        WeatherResponse actual = parser.parseWeather(response.body());
        WeatherResponse expected = ExpectedWeatherFactory.from(dataTable.asMap());

        List<WeatherFieldDifference> differences = comparator.compare(expected, actual);
        String comparisonLog = comparator.buildLog(expected, actual);

        LOGGER.info(System.lineSeparator() + comparisonLog);
        Allure.addAttachment("weather-comparison", "text/plain", comparisonLog);

        assertTrue(differences.isEmpty(), "Polya pogody otlichayutsya:%n%s".formatted(comparisonLog));
    }

    @Then("Oshibka v otvete sovpadaet s ozhidaemoy")
    public void errorResponseEquals(DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap();
        WeatherErrorResponse actual = parser.parseError(response.body());

        String comparisonLog = """
                error.code: expected=%s, actual=%s
                error.message: expected=%s, actual=%s
                """.formatted(
                expected.get("error.code"), actual.error().code(),
                expected.get("error.message"), actual.error().message()
        );

        LOGGER.info(System.lineSeparator() + comparisonLog);
        Allure.addAttachment("error-comparison", "text/plain", comparisonLog);

        assertEquals(Integer.parseInt(expected.get("error.code")), actual.error().code());
        assertEquals(expected.get("error.message"), actual.error().message());
    }

    private static String readResource(String resourcePath) {
        try (var inputStream = WeatherApiSteps.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new WeatherApiException("Resurs ne nayden: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new WeatherApiException("Ne udalos prochitat resurs: " + resourcePath, e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Map<String, String> queryParams(String query) {
        return Arrays.stream(query.replace("{apiKey}", API_KEY).split("&"))
                .map(parameter -> parameter.split("=", 2))
                .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter.length > 1 ? parameter[1] : ""));
    }
}
