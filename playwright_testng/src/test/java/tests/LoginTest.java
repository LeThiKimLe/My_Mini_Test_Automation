package tests;

import config.TestDataLoader;
import io.qameta.allure.Description;
import java.util.Properties;
import base.BaseTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    private static final String REQ = "3.10.22.1";
    private static final String SUITE = "Login_Tests";
    private Properties admin;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "createFixture")
    public void loadData() {
        admin = TestDataLoader.load("testdata/users/admin.properties");
    }

    @Test(groups = {"smoke", "regression"}, description = "Verify user can login successfully")
    @Description("Verify user can login successfully")
    public void userCanLoginSuccessfully() {
        authenticationFlow.loginAsStandardUser(
                admin.getProperty("username"),
                admin.getProperty("right_password"));
    }

    @Test(groups = {"regression"}, description = "Verify user cannot login with invalid credentials")
    @Description("Verify user cannot login with invalid credentials")
    public void userCannotLoginWithInvalidCredentials() {
        loginPage.login(admin.getProperty("username"), admin.getProperty("wrong_password"));
        loginPage.verifyErrorMessageDisplayed();
    }
}
