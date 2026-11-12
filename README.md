# QA Automation Tests

Automated UI and API tests:

| Scope | System under test | Tooling |
|-------|-------------------|---------|
| UI    | [SauceDemo](https://www.saucedemo.com/) | Selenide |
| API   | [Restful Booker](https://restful-booker.herokuapp.com/) | REST Assured |

Stack: Java 17, Maven, JUnit 5, Selenide, REST Assured, AssertJ, Allure, Docker.

## Test scenarios

| Test class | Scenario |
|------------|----------|
| `LoginTest` | Standard user logs in, lands on the Products page, the page contains products |
| `LoginTest` | Login is rejected with a clear error (locked-out user, wrong password) |
| `ProductCartTest` | Login → sort products → verify order → add product to cart → verify it in the cart |
| `ProductCartTest` | Every sorting option orders the catalogue correctly (parameterized, 4 options) |
| `BookingTest` | `POST /booking` creates a booking, `GET /booking/{id}` returns the same data |
| `BookingTest` | Deleted booking is no longer available (404) |
| `BookingTest` | Booking cannot be deleted without authorization (403) and stays intact |
| `BookingTest` | Booking without mandatory fields is rejected |

## Prerequisites

**Docker execution** (recommended): only Docker.

**Local execution:**

- JDK 17 or newer
- Maven 3.9+
- Google Chrome (the matching driver is downloaded automatically by Selenium Manager)

Both systems under test are public, so internet access is required.

## Local execution

```bash
mvn test                 # all tests
mvn test -Dgroups=ui     # UI tests only
mvn test -Dgroups=api    # API tests only
```

UI tests run headless by default. To watch the browser:

```bash
mvn test -Dgroups=ui -Dui.headless=false
```

Any value from [`config.properties`](src/test/resources/config.properties) can be overridden
the same way, with `-D<key>=<value>`.

### Report

Plain results are printed to the console and stored in `target/surefire-reports`.
For the Allure HTML report (steps, HTTP requests/responses, screenshots on failure):

```bash
mvn allure:serve
```

## Docker execution

```bash
docker build -t qa-automation-tests .
docker run --rm qa-automation-tests
```

The image contains JDK 17, Maven, Chromium, chromedriver and all project dependencies —
nothing has to be installed on the host. It builds on both amd64 and arm64 (Apple Silicon).

- Maven runs in offline mode inside the container: everything is resolved at build time, so a
  run downloads nothing and depends only on the two systems under test.
- Tests run as a non-root user (`tester`, UID 1000).
- The base image tag pins Maven, the JDK major version and the Debian release. Chromium and
  chromedriver come from the same Debian repository, so their versions always match.

Arguments after the image name are passed to Maven:

```bash
docker run --rm qa-automation-tests -Dgroups=ui     # UI tests only
docker run --rm qa-automation-tests -Dgroups=api    # API tests only
```

To keep the reports (Surefire, Allure results, failure screenshots) on the host:

```bash
docker run --rm -v "$(pwd)/target:/app/target" qa-automation-tests
```

On a Linux host the mounted `target` directory must be writable for UID 1000 (the default user
on most distributions).

The container exits with a non-zero code when a test fails, so it can be used in CI as is.

## Project structure

```
├── Dockerfile
├── pom.xml
├── .github/workflows/tests.yml      CI: runs all tests, uploads reports
└── src/test
    ├── java/com/qa
    │   ├── config/TestConfig        configuration access (file + -D overrides)
    │   ├── ui
    │   │   ├── model                Product, SortOption
    │   │   ├── pages                LoginPage, ProductsPage, CartPage, ProductCard
    │   │   └── tests                BaseUiTest, LoginTest, ProductCartTest
    │   └── api
    │       ├── client               BookingClient
    │       ├── data                 BookingData (test data factory)
    │       ├── model                Booking, BookingDates, CreatedBooking
    │       └── tests                BookingTest
    └── resources
        ├── config.properties        URLs, credentials, browser settings
        └── allure.properties
```

## Test approach

### Locators

SauceDemo marks its elements with dedicated `data-test` attributes, and every locator in the
project uses them (`[data-test='login-button']`, `[data-test='inventory-item-name']`, …).
They exist for automation, so they do not change when styling, layout or texts change — unlike
CSS classes, element order or XPath by text.

Elements that repeat per product (the *Add to cart* button) are located **inside** the product
card, and the card is found by the exact product name. This avoids building locators from product
names (`add-to-cart-sauce-labs-backpack`) and works for any product.

### Synchronization

There are no `Thread.sleep` calls or explicit waits in the project. Everything relies on
Selenide's built-in waiting: each `should…` condition is retried until it holds or the timeout
(`ui.timeoutMs`) expires.

- Page transitions are confirmed by a condition on the new page (URL and title) before the test
  goes on — `shouldBeOpened()`.
- Collections are read only after a collection condition passed (`sizeGreaterThan(0)`,
  `size(1)`), so a list is never read while it is still being rendered.
- After choosing a sorting option the page object waits for the displayed active option to
  change; it is rendered together with the list, so it is a reliable signal that sorting was
  applied.
- After *Add to cart* the page object waits for the button to turn into *Remove* and for the cart
  badge to show the expected count.

### Code structure

**UI** — Page Object pattern. Page classes own the locators and expose user actions
(`loginAs`, `sortBy`, `addToCart`) and state checks (`shouldBeOpened`); they return the next page
so tests read as a user flow. Tests contain no locators. Business assertions (is the list sorted?
is the right product in the cart?) stay in the tests and are made with AssertJ on small value
objects (`Product`), which gives readable failure messages.

The expected order for each sorting option lives in the `SortOption` enum as a `Comparator`.
The sorting check compares what is on the page with that comparator instead of a hard-coded list
of products, so it does not depend on the catalogue content.

**API** — `BookingClient` wraps REST Assured: base URI, headers, serialization and logging are
defined once, and each method maps to one endpoint and returns the raw `Response`. The client
knows *how* to call the API; tests decide *what* to assert (status code, content type, body).
Request and response bodies are typed Java records, so the "created data equals retrieved data"
check is a single field-by-field comparison covering first name, last name, total price, deposit
paid and booking dates.

**Test isolation** — tests do not depend on each other or on execution order:

- every UI test starts in a fresh browser (no shared cookies, session or cart);
- every API test creates its own booking with unique data and deletes it afterwards.

**Configuration** — one file, `config.properties`, read through `TestConfig`. System properties
take precedence, which is what makes the same code run locally, in Docker and in CI.

### Assumptions and limitations

- Both applications are public shared sandboxes. The tests are only as available as these
  services are; Restful Booker (hosted on Heroku) can respond slowly on the first request.
- Restful Booker deviates from REST conventions, and the tests assert its documented/actual
  behaviour: `POST /booking` returns **200** (not 201), `DELETE` returns **201**, and a payload
  with missing mandatory fields results in **500** (not 400).
- Restful Booker resets its data periodically and other people use it at the same time, so the
  tests never rely on pre-existing bookings or on total counts.
- The Chromium version in the image is whatever the pinned Debian release provides at build
  time, so two builds made months apart may contain different browser versions.
- In Docker Chromium runs with `--no-sandbox`, because its sandbox cannot start under Docker's
  default security profile. This is acceptable for a throwaway test container.
- UI tests run in Chrome/Chromium only. Cross-browser runs were left out as unnecessary for this
  exercise (`ui.browser` is configurable, but other browsers are not installed in the image).
- Credentials are kept in `config.properties` because they are public demo credentials. Real
  secrets would come from environment variables or a secret store.
- Tests run sequentially. With this number of tests parallel execution would add complexity
  without a noticeable gain.
