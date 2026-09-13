package flow;

import io.qameta.allure.Step;
import pages.LoginPage;
import pages.ProductPage;

public class AuthenticationFlow {
    private final LoginPage loginPage;
    private final ProductPage productPage;

    public AuthenticationFlow(LoginPage loginPage, ProductPage productPage) {
        this.loginPage = loginPage;
        this.productPage = productPage;
    }

    @Step("Login as standard user")
    public void loginAsStandardUser(String username, String password) {
        loginPage.login(username, password);
        productPage.verifyOnProductPage();
    }
}
