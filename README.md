# Тестовое задание QA: сервис погоды

## Описание

В проекте реализованы BDD-проверки API сервиса WeatherAPI.

Итоговые тесты выполняются с использованием WireMock и не обращаются к реальному сервису WeatherAPI.
Реальный API использовался только для получения примеров ответов и формирования JSON-фикстур.

В проекте проверяются:

- получение текущей погоды по четырем городам;
- обработка ошибок API;
- парсинг JSON-ответов;
- сравнение фактических значений с ожидаемыми;
- вывод результата сравнения по каждому проверяемому полю в лог;
- формирование результатов Allure.

## Стек

В проекте используются:

- Java 21;
- Maven;
- JUnit 5;
- Cucumber;
- WireMock;
- Allure;
- Jackson.

## Архитектура

Клиент для обращения к API реализован в `WeatherApiClient`.

Он использует стандартный `java.net.http.HttpClient` и умеет:

- запрашивать текущую погоду;
- выполнять GET-запрос по указанному path и query-параметрам;
- возвращать HTTP-статус и тело ответа.

Модели ответа расположены в пакете `ru.qa.weather.model`.

Для успешного ответа используются:

- `WeatherResponse`;
- `Location`;
- `CurrentWeather`;
- `Condition`.

Для ошибочного ответа используются:

- `WeatherErrorResponse`;
- `WeatherError`.

JSON-ответы парсятся через `WeatherJsonParser`.

Сравнение ожидаемых и фактических значений выполняется в `WeatherComparator`.
Результат сравнения содержит статус по каждому полю: `OK` или `DIFF`.

## Контракт WeatherAPI

Для текущей погоды используется endpoint:

```text
GET /v1/current.json?key=<API_KEY>&q=<CITY>&aqi=no
```

Успешный ответ содержит блоки:

- `location`;
- `current`.

Ошибочный ответ содержит блок:

- `error`.

Реальные ответы WeatherAPI сохранены как фикстуры в каталоге:

```text
src/test/resources/wiremock/weather
```

Фикстуры ошибок расположены в каталоге:

```text
src/test/resources/wiremock/errors
```

## WireMock

WireMock используется для эмуляции WeatherAPI.

В позитивных сценариях WireMock возвращает JSON текущей погоды из фикстур.

В негативных сценариях WireMock возвращает ошибочные ответы со статусами:

- `400`;
- `401`;
- `403`;
- `404`.

Для негативных сценариев WireMock проверяет:

- path запроса;
- query-параметры запроса.

## Позитивные проверки

Проверяется получение текущей погоды для четырех городов:

- London;
- Paris;
- Tokyo;
- New York.

Для каждого города проверяются поля:

- `location.name`;
- `location.country`;
- `location.tz_id`;
- `current.temp_c`;
- `current.condition.text`;
- `current.condition.code`;
- `current.wind_kph`;
- `current.humidity`;
- `current.cloud`;
- `current.feelslike_c`;
- `current.uv`.

## Негативные проверки

Проверяются 4 варианта ошибочных ответов:

- `400` - отсутствует обязательный параметр `q`;
- `401` - передан некорректный API key;
- `403` - нет доступа к ресурсу;
- `404` - запрошен несуществующий endpoint.

Для каждого ошибочного ответа проверяются:

- `error.code`;
- `error.message`.

## Cucumber

BDD-сценарии описаны в файле:

```text
src/test/resources/features/weather-api.feature
```

Шаги сценариев реализованы в классе:

```text
src/test/java/ru/qa/weather/steps/WeatherApiSteps.java
```

Класс запуска тестов:

```text
src/test/java/ru/qa/weather/WeatherApiWireMockContractTests.java
```

## Allure

В Allure передаются результаты выполнения Cucumber-сценариев.

Для проверок добавляются attachments с логом сравнения:

- ожидаемое значение;
- фактическое значение;
- статус сравнения `OK` или `DIFF`.

Результаты Allure формируются в каталоге:

```text
target/allure-results
```

## Исключения

В проекте используется доменное runtime-исключение `WeatherApiException`.

Сообщения исключений написаны транслитом, чтобы они стабильно отображались в консоли и логах.

## Запуск

Запустить тесты можно командой:

```bash
mvn test
```

Ожидаемый результат:

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

Сгенерировать и открыть Allure-отчет можно командой:

```bash
mvn allure:serve
```
