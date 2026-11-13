# QA Automation Tests

Automated UI tests for [SauceDemo](https://www.saucedemo.com/) and API tests for
[Restful Booker](https://restful-booker.herokuapp.com/).

Stack: Java 17, Maven, JUnit 5, Selenide, REST Assured, AssertJ, Allure, Docker.

## Test scenarios

| Type | Test class | Scenario |
|------|------------|----------|
| UI | `LoginTest` | Standard user logs in, reaches the Products page, the page contains products |
| UI | `LoginTest` | Login is rejected with an error message (locked-out user, wrong password) |
| UI | `ProductCartTest` | Log in, sort products, verify the order, add a product to the cart, verify it in the cart |
| UI | `ProductCartTest` | Each of the four sorting options orders the products correctly |
| API | `BookingTest` | `POST /booking` creates a booking, `GET /booking/{id}` returns the same data |
| API | `BookingTest` | A deleted booking is no longer available (404) |
| API | `BookingTest` | A booking cannot be deleted without authorization (403) |
| API | `BookingTest` | A booking without mandatory fields is rejected |

## Prerequisites

- **Docker execution:** Docker only.
- **Local execution:** JDK 17+, Maven 3.9+ and Google Chrome.

Both systems under test are public, so internet access is required.

## Local execution

```bash
mvn test                 # all tests
mvn test -Dgroups=ui     # UI tests
mvn test -Dgroups=api    # API tests
```

UI tests run headless by default. To watch the browser:

```bash
mvn test -Dgroups=ui -Dui.headless=false
```

Any value from [`config.properties`](src/test/resources/config.properties) can be overridden
with `-D<key>=<value>`.

Results are printed to the console and saved to `target/surefire-reports`.
`mvn allure:serve` opens the Allure report with test steps, HTTP requests and failure screenshots.

## Docker execution

```bash
docker build -t qa-automation-tests .
docker run --rm qa-automation-tests
```

The image contains the JDK, Maven, Chromium, chromedriver and all dependencies. Maven runs
offline inside the container, so a run downloads nothing.

Arguments after the image name are passed to Maven:

```bash
docker run --rm qa-automation-tests -Dgroups=ui     # UI tests
docker run --rm qa-automation-tests -Dgroups=api    # API tests
```

To keep the reports on the host:

```bash
docker run --rm -v "$(pwd)/target:/app/target" qa-automation-tests
```

The container exits with a non-zero code when a test fails.

## Project structure

```
├── Dockerfile
├── pom.xml
├── .github/workflows/tests.yml      CI: runs all tests on push and pull request
└── src/test
    ├── java/com/qa
    │   ├── config                   TestConfig
    │   ├── ui
    │   │   ├── model                Product, SortOption
    │   │   ├── pages                LoginPage, ProductsPage, CartPage, ProductCard
    │   │   └── tests                BaseUiTest, LoginTest, ProductCartTest
    │   └── api
    │       ├── client               BookingClient
    │       ├── data                 BookingData
    │       ├── model                Booking, BookingDates, CreatedBooking
    │       └── tests                BookingTest
    └── resources
        └── config.properties        URLs, credentials, browser settings
```

## Test approach

### Locators

All locators use the `data-test` attributes that SauceDemo provides for automation
(`[data-test='login-button']`). Unlike CSS classes, element order or text, they do not change
with styling or layout.

The *Add to cart* button is located inside its product card, and the card is found by the exact
product name, so the same code works for any product.

### Synchronization

The project has no `Thread.sleep` and no hand-written wait loops. Every Selenide `should…` condition is retried
until it holds or the timeout (`ui.timeoutMs`) expires:

- a page transition is confirmed by the URL and title of the new page;
- a list is read only after it has the expected number of elements;
- after sorting, the test waits for the active sort option to change;
- after *Add to cart*, the test waits for the *Remove* button and the cart badge.

### Code structure

- **UI:** Page Object pattern. Page classes hold the locators and expose user actions and state
  checks; tests contain no locators and read as a user flow.
- **Sorting check:** each `SortOption` carries a `Comparator`, and the test verifies that the
  products shown on the page follow it. No product list is hard-coded.
- **API:** `BookingClient` defines the base URI, headers and serialization once and returns raw
  responses; tests decide what to assert. Bodies are typed Java records, so created and retrieved
  bookings are compared field by field.
- **Isolation:** every UI test starts in a fresh browser; every API test creates its own booking
  with unique data and deletes it afterwards.
- **Configuration:** one file, `config.properties`, read through `TestConfig`. System properties
  take precedence, so the same code runs locally, in Docker and in CI.

### Assumptions and limitations

- Both applications are public shared sandboxes, so the tests depend on their availability and
  never rely on pre-existing data.
- Restful Booker deviates from REST conventions, and the tests assert its actual behaviour:
  `POST /booking` returns 200, `DELETE` returns 201, and a payload without mandatory fields
  returns 500.
- UI tests run in Chrome/Chromium only.
- In Docker, Chromium runs with `--no-sandbox`, because its sandbox cannot start under Docker's
  default security profile.
- The Chromium version in the image is the one the Debian release provides at build time.
- When mounting `target` on a Linux host, the directory must be writable for UID 1000, the user
  the tests run as.
- Credentials are stored in `config.properties` because they are public demo credentials.
- Tests run sequentially; parallel execution is not needed for this number of tests.
