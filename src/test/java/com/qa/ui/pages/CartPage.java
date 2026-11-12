package com.qa.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.qa.ui.model.Product;

import java.util.List;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class CartPage {

    private final SelenideElement title = $("[data-test='title']");
    private final ElementsCollection productCards = $$("[data-test='cart-list'] " + ProductCard.CARD);

    public CartPage shouldBeOpened() {
        webdriver().shouldHave(urlContaining("/cart.html"));
        title.shouldBe(visible).shouldHave(exactText("Your Cart"));
        return this;
    }

    public List<Product> products(int expectedCount) {
        return productCards.shouldHave(size(expectedCount))
                .asFixedIterable().stream()
                .map(ProductCard::toProduct)
                .toList();
    }
}
