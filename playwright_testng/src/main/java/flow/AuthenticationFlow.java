package flow;

import io.qameta.allure.Step;
import pages.LoginPage;
import pages.ProductPage;

/** Business workflow facade; tests do not coordinate individual UI actions. */
public class AuthenticationFlow {
    private final LoginPage loginPage;
    private final ProductPage productPage;

    public AuthenticationFlow(LoginPage loginPage, ProductPage productPage) {
        this.loginPage = loginPage;
        this.productPage = productPage;
    }

    @Step("Log in as a standard user")
    public void loginAsStandardUser(String username, String password) {
        loginPage.login(username, password);
        productPage.verifyOnProductPage();
    }
}
