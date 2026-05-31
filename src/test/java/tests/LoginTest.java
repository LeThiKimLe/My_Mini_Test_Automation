package tests;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import base.BaseTest;
import config.TestDataLoader;
import io.qameta.allure.Description;
import io.qameta.allure.Step;

public class LoginTest extends BaseTest {

    Properties admin;

    @BeforeEach
    void setupData() {
        admin = TestDataLoader.load("testdata/users/admin.properties");
    }

    @Test
    @Description("Verify user can login successfully")
    void userCanLoginSuccessfully() {
        authenticationFlow.loginAsStandardUser(admin.getProperty("username"), admin.getProperty("right_password"));
    } 

    @Test
    @Description("Verify user cannot login with invalid credentials")
    void userCannotLoginWithInvalidCredentials() {
        loginPage.login(admin.getProperty("username"), admin.getProperty("wrong_password"));
        loginPage.verifyErrorMessageDisplayed();
    }
}
