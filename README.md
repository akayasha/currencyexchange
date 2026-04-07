# Currency Exchange API

This project is a small Spring Boot service for working with currencies, exchange rates, and conversions.

You can:

- store currencies like `USD`, `EUR`, and `JPY`
- store exchange rates between currency pairs
- convert an amount using a saved rate
- fall back to a live external rate when a pair is missing locally
- review conversion history and rate-change history
- initialize demo data and sync rates from the admin endpoints

It uses H2 for local data, Swagger for API docs, and Maven for build/test.

## What is inside

The stack is simple and standard:

- Java 17
- Spring Boot 3.2.3
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring AOP
- H2 in-memory database
- Swagger / OpenAPI via `springdoc`
- JUnit 5 and Mockito

## Before you run it

You need:

- Java 17+
- a terminal

You can use your own Maven install, but the repo already includes the wrapper, so `./mvnw` is the easiest option.

## Run the app

From the project root:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

Once it starts, use these URLs:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

H2 connection details:

- JDBC URL: `jdbc:h2:mem:currencyexchangedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`
- Password: leave it empty

## How responses look

Most successful endpoints return the same wrapper:

```json
{
  "code": 200,
  "message": "Operation successful",
  "data": {},
  "timestamp": [2026, 4, 7, 4, 13, 26, 780312373]
}
```

The real payload is usually inside `data`.

That matters for:

- normal objects like a currency or exchange rate
- lists
- paginated results such as `data.content`, `data.totalElements`, and related page fields

## Main areas of the API

### Currency endpoints

These are for basic currency records.

- `GET /api/currencies`
- `GET /api/currencies/{id}`
- `GET /api/currencies/code/{code}`
- `GET /api/currencies/search?keyword=...`
- `GET /api/currencies/region/{region}`
- `GET /api/currencies/active`
- `GET /api/currencies/stats`
- `POST /api/currencies`
- `POST /api/currencies/bulk`
- `PUT /api/currencies/{id}`
- `PATCH /api/currencies/{uuid}?isActive=true`
- `PATCH /api/currencies/bulk/status`
- `DELETE /api/currencies/{id}`

### Exchange rate endpoints

These handle the actual rates between two currencies.

- `GET /api/exchange-rates`
- `GET /api/exchange-rates/{id}`
- `GET /api/exchange-rates/pair?from=USD&to=EUR`
- `GET /api/exchange-rates/details?from=USD&to=EUR`
- `GET /api/exchange-rates/search?keyword=USD`
- `GET /api/exchange-rates/base/{fromCode}`
- `GET /api/exchange-rates/analytics`
- `POST /api/exchange-rates`
- `POST /api/exchange-rates/bulk`
- `PUT /api/exchange-rates/{id}`
- `PUT /api/exchange-rates/bulk`
- `DELETE /api/exchange-rates/{id}`

### Conversion endpoints

These are for turning one amount into another currency amount.

- `POST /api/convert`
- `GET /api/convert?from=USD&to=EUR&amount=100`
- `POST /api/convert/bulk`
- `GET /api/convert/history`
- `GET /api/convert/stats`

Conversion behavior is:

1. check the local database first
2. if the pair is missing, call the external API
3. return the source in the response, usually `db` or `external-api`

The base external API URL is configured in [application.properties](/home/karumakarumakaruma/IdeaProjects/currencyexchange/src/main/resources/application.properties#L24):

```properties
exchange.rate.api.base-url=https://open.er-api.com/v6/latest
```

### History endpoints

There are two kinds of history in this project.

Exchange-rate history:

- `GET /api/history/pair?from=USD&to=EUR`
- `GET /api/history/currency?from=USD`
- `GET /api/history/pair/details?from=USD&to=EUR`
- `GET /api/history/{id}`
- `GET /api/history/pair/latest?from=USD&to=EUR`
- `GET /api/history/pair/stats?from=USD&to=EUR`

Conversion history:

- `GET /api/convert/history`
- `GET /api/convert/stats`

### Admin endpoints

These are useful when you first start the app.

- `POST /api/admin/init-database`
- `POST /api/admin/sync-exchange-rates`

`init-database` seeds popular currencies and pulls initial USD-based rates from the external API.

`sync-exchange-rates` fetches live rates for every currency already in the database, then:

- updates existing pairs and records each change in `exchange_rate_history` with reason `API_SYNC_UPDATE`.
- creates missing pairs, saving the initial value and a history entry with reason `API_SYNC_CREATE`.
- responds with counts: `ratesUpdated`, `ratesCreated`, `ratesFailed`, `totalCurrencies`, plus a `timestamp`.

Step-by-step to refresh rates safely:

1. Start the app: `./mvnw spring-boot:run` (default port 8080).
2. Seed base data once: `POST /api/admin/init-database`.
3. Refresh all rates: `POST /api/admin/sync-exchange-rates`.
4. Inspect history for a pair (e.g., USD/EUR): `GET /api/history/pair?from=USD&to=EUR` or `GET /api/history/pair/latest?from=USD&to=EUR`.

Manual edits also write history:

- `PUT /api/exchange-rates/{id}` now records a history row with reason `MANUAL_UPDATE` whenever the rate value changes. If the value is unchanged, the service skips the write.

## Simple request examples

Create a currency:

```bash
curl -X POST http://localhost:8080/api/currencies \
  -H "Content-Type: application/json" \
  -d '{
    "code": "USD",
    "name": "United States Dollar",
    "symbol": "$",
    "region": "North America",
    "isActive": true
  }'
```

Create an exchange rate:

```bash
curl -X POST http://localhost:8080/api/exchange-rates \
  -H "Content-Type: application/json" \
  -d '{
    "fromCurrencyCode": "USD",
    "toCurrencyCode": "EUR",
    "rate": 0.9250
  }'
```

Convert currency:

```bash
curl -X POST http://localhost:8080/api/convert \
  -H "Content-Type: application/json" \
  -d '{
    "fromCurrencyCode": "USD",
    "toCurrencyCode": "EUR",
    "amount": 100.00
  }'
```

Initialize demo data:

```bash
curl -X POST http://localhost:8080/api/admin/init-database
```

## Postman collection

The repo includes [Currency_Exchange_API.postman_collection.json](/home/karumakarumakaruma/IdeaProjects/currencyexchange/Currency_Exchange_API.postman_collection.json).

It now uses Postman variables instead of hardcoded values, including:

- `{{baseUrl}}`
- `{{fromCurrencyCode}}`
- `{{toCurrencyCode}}`
- `{{amount}}`
- `{{currencyId}}`
- `{{exchangeRateId}}`
- `{{historyId}}`

Recommended setup in Postman:

1. Import the collection.
2. Create an environment.
3. Set `baseUrl` to `http://localhost:8080`.
4. Change `fromCurrencyCode`, `toCurrencyCode`, IDs, and amounts when you want to test other cases.

This makes it much easier to reuse the same requests without editing every URL or request body by hand.

## Project structure

At a high level:

```text
src/main/java/com/example/currencyexchange/
  aspect/        logging
  config/        security, OpenAPI, JPA, RestTemplate
  controller/    REST endpoints
  dto/           request/response models and ResponseApi
  exception/     global exception handling
  model/         JPA entities
  repository/    database access
  service/       business logic

src/test/java/com/example/currencyexchange/
  controller/
  dto/
  repository/
  service/
```

## Testing

Run everything:

```bash
./mvnw test
```

Run one test class:

```bash
./mvnw -Dtest=ExchangeRateControllerTest test
```

## A couple of small notes

- Security is open for `/api/**`, Swagger, and H2 in local use, so you do not need authentication for this demo app.
- [QUICK_START.md](/home/karumakarumakaruma/IdeaProjects/currencyexchange/QUICK_START.md#L1) still lags behind the current API a bit, so this README is the better reference right now.
