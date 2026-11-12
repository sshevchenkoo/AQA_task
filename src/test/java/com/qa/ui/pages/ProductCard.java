package com.qa.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import com.qa.ui.model.Product;
import org.openqa.selenium.By;

import java.math.BigDecimal;

import static com.codeborne.selenide.Condition.match;

/** Product card markup shared by the Products and Cart pages. */
final class ProductCard {

    static final String CARD = "[data-test='inventory-item']";
    private static final String NAME = "[data-test='inventory-item-name']";
    private static final String PRICE = "[data-test='inventory-item-price']";

    private ProductCard() {
    }

    static Product toProduct(SelenideElement card) {
        String name = card.$(NAME).getText();
        String price = card.$(PRICE).getText().replace("$", "");
        return new Product(name, new BigDecimal(price));
    }

    static WebElementCondition nameOf(Product product) {
        return match("has product name '" + product.name() + "'",
                card -> card.findElement(By.cssSelector(NAME)).getText().equals(product.name()));
    }
}
