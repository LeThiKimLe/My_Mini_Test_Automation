package tests;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;

import annotations.RegressionTest;
import annotations.SmokeRegressionTest;
import base.BaseTest;
import config.TestDataLoader;

public class LoginTest extends BaseTest {

    Properties admin;

    @BeforeEach
    void setupData() {
        admin = TestDataLoader.load("testdata/users/admin.properties");
    }

    @SmokeRegressionTest(description = "Verify user can login successfully")
    void userCanLoginSuccessfully() {
        authenticationFlow.loginAsStandardUser(admin.getProperty("username"), admin.getProperty("right_password"));
    } 

    @RegressionTest(description = "Verify user cannot login with invalid credentials")
    void userCannotLoginWithInvalidCredentials() {
        loginPage.login(admin.getProperty("username"), admin.getProperty("wrong_password"));
        loginPage.verifyErrorMessageDisplayed();
    }
}
