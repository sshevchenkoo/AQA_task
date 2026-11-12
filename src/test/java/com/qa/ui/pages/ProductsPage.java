package com.qa.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.qa.ui.model.Product;
import com.qa.ui.model.SortOption;

import java.util.List;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class ProductsPage {

    private final SelenideElement title = $("[data-test='title']");
    private final ElementsCollection productCards = $$(ProductCard.CARD);
    private final SelenideElement sortDropdown = $("[data-test='product-sort-container']");
    private final SelenideElement activeSortOption = $("[data-test='active-option']");
    private final SelenideElement cartLink = $("[data-test='shopping-cart-link']");
    private final SelenideElement cartBadge = $("[data-test='shopping-cart-badge']");

    public ProductsPage shouldBeOpened() {
        webdriver().shouldHave(urlContaining("/inventory.html"));
        title.shouldBe(visible).shouldHave(exactText("Products"));
        return this;
    }

    public List<Product> products() {
        return productCards.shouldHave(sizeGreaterThan(0))
                .asFixedIterable().stream()
                .map(ProductCard::toProduct)
                .toList();
    }

    public ProductsPage sortBy(SortOption option) {
        sortDropdown.selectOptionByValue(option.value());
        // The label is re-rendered together with the list, so it signals that sorting was applied.
        activeSortOption.shouldHave(exactText(option.label()));
        return this;
    }

    public ProductsPage addToCart(Product product) {
        SelenideElement card = productCards.findBy(ProductCard.nameOf(product));
        card.$("button[data-test^='add-to-cart']").click();
        card.$("button[data-test^='remove']").shouldBe(visible);
        return this;
    }

    public ProductsPage shouldHaveCartBadge(int expectedCount) {
        cartBadge.shouldHave(exactText(String.valueOf(expectedCount)));
        return this;
    }

    public CartPage openCart() {
        cartLink.click();
        return new CartPage();
    }
}
