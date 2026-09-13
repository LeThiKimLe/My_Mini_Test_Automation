package pages;

import io.qameta.allure.Step;
import locators.ProductLocators;
import utils.PlaywrightActions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ProductPage extends BasePage {

    public ProductPage(PlaywrightActions WebUI) {
        super(WebUI);
    }

    @Step("Verify user is on product page")
    public void verifyOnProductPage() {
        // ProductLocators.productTitle() returns a selector string; convert to Locator via WebUI
        assertThat(WebUI.findLocator(ProductLocators.productTitle())).hasText("Products");
        logger.info(String.format("Product page is loaded successfully"));
    }

}
