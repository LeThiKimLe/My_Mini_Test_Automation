package pages;

import com.microsoft.playwright.Page;

import io.qameta.allure.Step;
import locators.LoginLocators;
import utils.PlaywrightActions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BasePage {
    
    
    private String usernameText;

    public LoginPage(PlaywrightActions WebUI) {
        super(WebUI);
    }

    @Step("Input username: [{username}]")
    public void enterUsername(String username) {
        WebUI.setText(LoginLocators.usernameInput(), username);
    }

    @Step("Input password: [{password}]")
    public void enterPassword(String password) {
        WebUI.setText(LoginLocators.passwordInput(), password);
    }

    @Step("Click on login button")
    public void clickLogin() {
        WebUI.click(LoginLocators.loginButton());
    }

    @Step("Login with username: [{username}] and password: [{password}]")
    public void login(String username, String password) {
        this.usernameText = username;
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    @Step("Verify error message is displayed for invalid login attempt")
    public void verifyErrorMessageDisplayed() {
        WebUI.verifyLocatorVisible(LoginLocators.errorMessage());
        assert WebUI.getText(LoginLocators.errorMessage()).contains("Username and password do not match any user in this service");
        logger.info(String.format("Verified error message for invalid login attempt with username: %s", usernameText));
    }

}
