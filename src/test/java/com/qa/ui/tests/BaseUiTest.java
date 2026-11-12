package com.qa.ui.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.qa.config.TestConfig;
import com.qa.ui.pages.LoginPage;
import com.qa.ui.pages.ProductsPage;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("ui")
abstract class BaseUiTest {

    private static final String ALLURE_LISTENER = "allure";

    @BeforeAll
    static void configureSelenide() {
        Configuration.baseUrl = TestConfig.uiBaseUrl();
        Configuration.browser = TestConfig.browser();
        Configuration.browserSize = TestConfig.browserSize();
        Configuration.headless = TestConfig.headless();
        Configuration.timeout = TestConfig.uiTimeoutMs();
        Configuration.reportsFolder = "target/selenide-reports";

        if (!SelenideLogger.hasListener(ALLURE_LISTENER)) {
            SelenideLogger.addListener(ALLURE_LISTENER,
                    new AllureSelenide().screenshots(true).savePageSource(false));
        }
    }

    /** Fresh browser per test, so session and cart state never leak between tests. */
    @AfterEach
    void closeBrowser() {
        Selenide.closeWebDriver();
    }

    protected ProductsPage loginAsStandardUser() {
        return LoginPage.open()
                .loginAs(TestConfig.uiUsername(), TestConfig.uiPassword())
                .shouldBeOpened();
    }
}
