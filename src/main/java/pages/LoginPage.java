package pages;

import com.microsoft.playwright.Page;

import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BasePage {
    
    private final String usernameInput = "#user-name";
    private final String passwordInput = "#password";
    private final String loginButton = "#login-button";
    private final String errorMessage = "[data-test='error']";
    private String usernameText;

    public LoginPage(Page page) {
        super(page);
    }

    @Step("Input username: [{username}]")
    public void enterUsername(String username) {
        page.fill(usernameInput, username);
    }

    @Step("Input password: [{password}]")
    public void enterPassword(String password) {
        page.fill(passwordInput, password);
    }

    @Step("Click on login button")
    public void clickLogin() {
        page.click(loginButton);
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
        assertThat(page.locator(errorMessage)).isVisible();
        assert page.locator(errorMessage).textContent().contains("Username and password do not match any user in this service");
        logger.info(String.format("Verified error message for invalid login attempt with username: %s", usernameText));
    }

}
