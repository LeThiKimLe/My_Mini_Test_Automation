package base;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import com.microsoft.playwright.*;

import config.TestConfig;
import factory.BrowserFactory;
import flow.AuthenticationFlow;
import pages.LoginPage;
import pages.ProductPage;

@ExtendWith(PlaywrightTestWatcher.class)
public class BaseTest {
    private static Browser browser;
    protected Page page;

    // Pages
    protected LoginPage loginPage;
    protected ProductPage productPage;
    protected BrowserContext context;

    // Flows
    protected AuthenticationFlow authenticationFlow;

    @BeforeAll
    static void setupBrowser() {
        browser = BrowserFactory.getBrowser();
    }
    
    @BeforeEach
    void setup() {
        context = browser.newContext();
        context.tracing().start(
            new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
        );
        page = context.newPage();
        PlaywrightTestWatcher.setPage(page);
        PlaywrightTestWatcher.setContext(context);
        createPages();
    }

    @AfterAll
    static void teardown() {
        BrowserFactory.close();
        utils.AllureReportHelper.generateSingleReports();
    }

    void createPages() {

        //Navigate to base URL
        page.navigate(TestConfig.getBaseUrl());

        //Init Pages
        loginPage = new LoginPage(page);
        productPage = new ProductPage(page);

        // Init Flows
        authenticationFlow = new AuthenticationFlow(loginPage, productPage);
    }

    @AfterEach
    void closeContext() {
        context.close();
    }
    
}
