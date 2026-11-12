package com.qa.ui.tests;

import com.qa.ui.model.Product;
import com.qa.ui.model.SortOption;
import com.qa.ui.pages.ProductsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SauceDemo: products and cart")
class ProductCartTest extends BaseUiTest {

    @Test
    @DisplayName("Sorted product added to the cart is shown in the cart")
    void sortedProductCanBeAddedToCart() {
        SortOption sortOption = SortOption.PRICE_ASC;
        ProductsPage productsPage = loginAsStandardUser().sortBy(sortOption);

        List<Product> sortedProducts = productsPage.products();
        assertThat(sortedProducts).as("products sorted by '%s'", sortOption.label())
                .isSortedAccordingTo(sortOption.expectedOrder());

        Product cheapestProduct = sortedProducts.get(0);
        List<Product> cartProducts = productsPage
                .addToCart(cheapestProduct)
                .shouldHaveCartBadge(1)
                .openCart()
                .shouldBeOpened()
                .products(1);

        assertThat(cartProducts).as("cart content").containsExactly(cheapestProduct);
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Products are ordered according to the selected sorting")
    @EnumSource(SortOption.class)
    void productsAreSortedBySelectedOption(SortOption sortOption) {
        ProductsPage productsPage = loginAsStandardUser();
        List<Product> productsBeforeSorting = productsPage.products();

        List<Product> sortedProducts = productsPage.sortBy(sortOption).products();

        assertThat(sortedProducts).as("products sorted by '%s'", sortOption.label())
                .isSortedAccordingTo(sortOption.expectedOrder())
                .containsExactlyInAnyOrderElementsOf(productsBeforeSorting);
    }
}
