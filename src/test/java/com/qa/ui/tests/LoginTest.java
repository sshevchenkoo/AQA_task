package com.qa.ui.tests;

import com.qa.config.TestConfig;
import com.qa.ui.model.Product;
import com.qa.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SauceDemo: login")
class LoginTest extends BaseUiTest {

    @Test
    @DisplayName("Standard user logs in and sees the product catalogue")
    void standardUserSeesProductsAfterLogin() {
        List<Product> products = LoginPage.open()
                .loginAs(TestConfig.uiUsername(), TestConfig.uiPassword())
                .shouldBeOpened()
                .products();

        assertThat(products).as("products on the Products page")
                .isNotEmpty()
                .allSatisfy(product -> {
                    assertThat(product.name()).as("product name").isNotBlank();
                    assertThat(product.price()).as("price of '%s'", product.name()).isGreaterThan(BigDecimal.ZERO);
                });
    }

    @ParameterizedTest(name = "user ''{0}'' → ''{2}''")
    @DisplayName("Login is rejected with a clear error message")
    @CsvSource({
            "locked_out_user, secret_sauce,   'Sorry, this user has been locked out.'",
            "standard_user,   wrong_password, 'Username and password do not match any user in this service'"
    })
    void loginIsRejected(String username, String password, String expectedError) {
        LoginPage.open()
                .submitCredentials(username, password)
                .shouldShowError(expectedError)
                .shouldBeOpened();
    }
}
