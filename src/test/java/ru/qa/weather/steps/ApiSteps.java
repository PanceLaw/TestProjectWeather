package ru.qa.weather.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import ru.qa.weather.client.ApiClient;
import ru.qa.weather.client.ApiResponse;
import ru.qa.weather.compare.ResponseComparator;
import ru.qa.weather.controller.Controller;
import ru.qa.weather.exception.ServiceException;
import ru.qa.weather.model.CurrentResponse;
import ru.qa.weather.model.ErrorResponse;
import ru.qa.weather.parser.ResponseParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiSteps {
    private static final Logger LOGGER = Logger.getLogger(ApiSteps.class.getName());
    private static final String API_KEY = "test-api-key";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private static WireMockServer wireMockServer;

    private final ResponseParser parser = new ResponseParser();
    private final ResponseComparator comparator = new ResponseComparator();

    private Controller controller;
    private ApiResponse response;

    @Before
    public void setUp() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
        }
        wireMockServer.resetAll();
        controller = new Controller(new ApiClient(wireMockServer.baseUrl(), API_KEY));
    }

    @AfterAll
    public static void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @Given("Мок для города {string} отдает ответ из файла {string}")
    public void mockReturnsCurrentResponseForCity(String city, String fixtureName) {
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/current.json"))
                .withQueryParam("key", equalTo(API_KEY))
                .withQueryParam("q", equalTo(city))
                .withQueryParam("aqi", equalTo("no"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", JSON_CONTENT_TYPE)
                        .withBody(readResource("wiremock/weather/" + fixtureName))));
    }

    @Given("Мок для пути {string} с параметрами {string} отдает ошибку {int} из файла {string}")
    public void mockReturnsErrorResponse(String path, String query, int statusCode, String fixtureName) {
        var mappingBuilder = get(urlPathEqualTo(path));
        queryParams(query).forEach((name, value) -> mappingBuilder.withQueryParam(name, equalTo(value)));

        wireMockServer.stubFor(mappingBuilder.willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", JSON_CONTENT_TYPE)
                .withBody(readResource("wiremock/errors/" + fixtureName))));
    }

    @When("Клиент запрашивает погоду по городу {string}")
    public void clientRequestsCurrentForCity(String city) {
        response = controller.getCurrent(city);
    }

    @When("Клиент выполняет GET-запрос в {string} с параметрами {string}")
    public void clientSendsGetRequest(String path, String query) {
        response = controller.sendGet(path, query.replace("{apiKey}", API_KEY));
    }

    @Then("Статус ответа {int}")
    public void responseStatusIs(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    @Then("Погода в ответе совпадает с ожидаемой")
    public void currentResponseEquals(DataTable dataTable) {
        CurrentResponse actual = parser.parseCurrent(response.body());
        CurrentResponse expected = ExpectedResponseFactory.from(dataTable.asMap());

        ResponseComparator.ComparisonResult comparison = comparator.compare(expected, actual);
        String comparisonLog = comparison.log();

        LOGGER.info(System.lineSeparator() + comparisonLog);
        Allure.addAttachment("weather-comparison", "text/plain", comparisonLog);

        assertTrue(!comparison.hasDifferences(), "Current response fields differ:%n%s".formatted(comparisonLog));
    }

    @Then("Ошибка в ответе совпадает с ожидаемой")
    public void errorResponseEquals(DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap();
        ErrorResponse actual = parser.parseError(response.body());

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
        try (var inputStream = ApiSteps.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ServiceException("Resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceException("Resource reading failed: " + resourcePath, e);
        }
    }

    private static Map<String, String> queryParams(String query) {
        return Arrays.stream(query.replace("{apiKey}", API_KEY).split("&"))
                .map(parameter -> parameter.split("=", 2))
                .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter.length > 1 ? parameter[1] : ""));
    }
}
