package pages;

import io.qameta.allure.Step;
import locators.LoginLocators;
import utils.PlaywrightActions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BasePage {
    private String lastUsername;

    public LoginPage(PlaywrightActions webUI) {
        super(webUI);
    }

    @Step("Enter username")
    public void enterUsername(String username) {
        webUI.setText(LoginLocators.usernameInput(), username);
    }

    @Step("Enter password")
    public void enterPassword(String password) {
        webUI.setText(LoginLocators.passwordInput(), password);
    }

    @Step("Submit login form")
    public void clickLogin() {
        webUI.click(LoginLocators.loginButton());
    }

    @Step("Log in as user [{username}]")
    public void login(String username, String password) {
        lastUsername = username;
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    @Step("Verify invalid-login error is displayed")
    public void verifyErrorMessageDisplayed() {
        assertThat(webUI.findLocator(LoginLocators.errorMessage())).isVisible();
        logger.info("Verified invalid login for user " + lastUsername);
    }
}
