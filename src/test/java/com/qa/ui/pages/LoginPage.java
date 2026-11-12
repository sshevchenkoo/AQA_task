package com.qa.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {

    private final SelenideElement usernameInput = $("[data-test='username']");
    private final SelenideElement passwordInput = $("[data-test='password']");
    private final SelenideElement loginButton = $("[data-test='login-button']");
    private final SelenideElement errorMessage = $("[data-test='error']");

    public static LoginPage open() {
        Selenide.open("/");
        return new LoginPage();
    }

    public ProductsPage loginAs(String username, String password) {
        submitCredentials(username, password);
        return new ProductsPage();
    }

    public LoginPage submitCredentials(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        loginButton.click();
        return this;
    }

    public LoginPage shouldShowError(String expectedMessage) {
        errorMessage.shouldBe(visible).shouldHave(text(expectedMessage));
        return this;
    }

    public LoginPage shouldBeOpened() {
        loginButton.shouldBe(visible);
        return this;
    }
}
