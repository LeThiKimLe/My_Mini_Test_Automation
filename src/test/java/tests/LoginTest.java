package tests;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import annotations.RegressionTest;
import annotations.SmokeRegressionTest;
import base.BaseTest;
import config.TestDataLoader;
import io.qameta.allure.Description;

public class LoginTest extends BaseTest {

    private static final String REQ = "3.10.22.1";
    private static final String SUITE = "Login_Tests";

    Properties admin;

    @BeforeEach
    void setupData() {
        admin = TestDataLoader.load("testdata/users/admin.properties");
    }

    @Test
    @DisplayName("Verify user can login successfully")
    @Tag("sprint-login")
    @Tag("release-1.0")
    void userCanLoginSuccessfully() {
        authenticationFlow.loginAsStandardUser(admin.getProperty("username"), admin.getProperty("right_password"));
    } 

    @Test
    @DisplayName ("Verify user cannot login with invalid credentials")
    @Tag("sprint-login")
    @Tag("release-2.0")
    void userCannotLoginWithInvalidCredentials() {
        loginPage.login(admin.getProperty("username"), admin.getProperty("wrong_password"));
        loginPage.verifyErrorMessageDisplayed();
    }
}
