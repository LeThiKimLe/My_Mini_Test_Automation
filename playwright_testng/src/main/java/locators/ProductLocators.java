package locators;

import utils.Locators;

public final class ProductLocators extends CommonLocators {
    private ProductLocators() {
    }

    public static String title() { return Locators.create("[data-test='title']"); }
}
