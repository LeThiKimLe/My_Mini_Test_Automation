package pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ProductPage extends BasePage {
    private final String productTitle = ".title";

    public ProductPage(Page page) {
        super(page);
    }

    @Step("Verify user is on product page")
    public void verifyOnProductPage() {
        assertThat(page.locator(productTitle)).hasText("Products");
        logger.info(String.format("Product page is loaded successfully"));
    }

}
