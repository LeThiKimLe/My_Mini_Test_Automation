package pages;

import io.qameta.allure.Step;
import locators.ProductLocators;
import utils.PlaywrightActions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ProductPage extends BasePage {
    public ProductPage(PlaywrightActions webUI) {
        super(webUI);
    }

    @Step("Verify products page is displayed")
    public void verifyOnProductPage() {
        assertThat(webUI.findLocator(ProductLocators.title())).hasText("Products");
    }
}
