package locators;

import utils.Locators;

public final class LoginLocators extends CommonLocators {
    private LoginLocators() {
    }

    public static String usernameInput() { return Locators.create("#user-name"); }
    public static String passwordInput() { return Locators.create("#password"); }
    public static String loginButton() { return Locators.create("#login-button"); }
    public static String errorMessage() { return Locators.create("[data-test='error']"); }
}
