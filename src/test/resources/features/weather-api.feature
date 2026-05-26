Feature: Kontraktnye proverki WeatherAPI cherez WireMock

  Scenario Outline: Uspeshno poluchaem tekushchuyu pogodu po gorodu
    Given Mok dlya goroda "<city>" otdaet otvet iz faila "<fixture>"
    When Klient zaprashivaet pogodu po gorodu "<city>"
    Then Status otveta 200
    And Pogoda v otvete sovpadaet s ozhidaemoy
      | location.name            | <name>           |
      | location.country         | <country>        |
      | location.tz_id           | <tzId>           |
      | current.temp_c           | <tempC>          |
      | current.condition.text   | <conditionText>  |
      | current.condition.code   | <conditionCode>  |
      | current.wind_kph         | <windKph>        |
      | current.humidity         | <humidity>       |
      | current.cloud            | <cloud>          |
      | current.feelslike_c      | <feelslikeC>     |
      | current.uv               | <uv>             |

    Examples:
      | city     | fixture       | name     | country                  | tzId             | tempC | conditionText | conditionCode | windKph | humidity | cloud | feelslikeC | uv  |
      | London   | london.json    | London   | United Kingdom           | Europe/London    | 33.2  | Sunny         | 1000          | 10.1    | 34       | 0     | 32.9       | 1.4 |
      | Paris    | paris.json     | Paris    | France                   | Europe/Paris     | 31.1  | Sunny         | 1000          | 10.1    | 29       | 0     | 30.3       | 1.2 |
      | Tokyo    | tokyo.json     | Tokyo    | Japan                    | Asia/Tokyo       | 21.4  | Partly cloudy | 1003          | 5.8     | 88       | 75    | 21.4       | 0.0 |
      | New York | new-york.json  | New York | United States of America | America/New_York | 25.3  | Sunny         | 1000          | 9.4     | 32       | 0     | 26.1       | 6.8 |

  Scenario Outline: Poluchaem oshibku na nekorrektnyy zapros
    Given Mok dlya puti "<path>" s parametrami "<query>" otdaet oshibku <status> iz faila "<fixture>"
    When Klient delaet GET zapros v "<path>" s parametrami "<query>"
    Then Status otveta <status>
    And Oshibka v otvete sovpadaet s ozhidaemoy
      | error.code    | <code>    |
      | error.message | <message> |

    Examples:
      | status | path                 | query                    | fixture                     | code | message                                                                                                   |
      | 400    | /v1/current.json     | key={apiKey}             | missing-q-400.json          | 1003 | Parameter 'q' not provided.                                                                               |
      | 401    | /v1/current.json     | key=invalid_key&q=London | invalid-key-401.json        | 2006 | API key provided is invalid                                                                               |
      | 403    | /v1/forecast.json    | key={apiKey}&q=London    | forbidden-resource-403.json | 2009 | API key does not have access to the resource. Please check pricing page for what is allowed in your API subscription plan. |
      | 404    | /v1/not-found.json   | key={apiKey}&q=London    | not-found-404.json          | 1005 | API request url is invalid                                                                                |
