package base;

import com.microsoft.playwright.Page;
import config.TestConfig;
import factory.BrowserFactory;
import flow.AuthenticationFlow;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import pages.LoginPage;
import pages.ProductPage;
import utils.Loggers;
import utils.PlaywrightActions;
import utils.TestContext;
import utils.Utils;

/**
 * TestNG class fixture. Browser ownership remains centralized in BrowserFactory;
 * tests only receive page objects and business flows.
 */
public abstract class BaseTest {
    protected static final String DEFAULT_CONTEXT_NAME = "default";
    protected BrowserFactory browserFactory;
    protected PlaywrightActions webUI;
    protected LoginPage loginPage;
    protected ProductPage productPage;
    protected AuthenticationFlow authenticationFlow;
    protected Loggers logger;
    protected Page page;

    private int testCount;

    @BeforeClass(alwaysRun = true)
    public void createFixture() {
        String className = getClass().getSimpleName();
        String suiteName = suiteName();
        TestContext.set(suiteName, className, className, requirement());
        Utils.prepareTestDirectory();
        browserFactory = new BrowserFactory();
        browserFactory.createBrowser();
        browserFactory.createPage(DEFAULT_CONTEXT_NAME);
        page = browserFactory.getPage(DEFAULT_CONTEXT_NAME);
        webUI = new PlaywrightActions(browserFactory.getContext(DEFAULT_CONTEXT_NAME), page);
        logger = new Loggers();
        loginPage = new LoginPage(webUI);
        productPage = new ProductPage(webUI);
        authenticationFlow = new AuthenticationFlow(loginPage, productPage);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestResult testResult) {
        String testName = testResult.getMethod().getMethodName();
        testCount++;
        TestContext.set(suiteName(), getClass().getSimpleName(), testName, requirement());
        browserFactory.startTracing();
        webUI.navigateToUrl(TestConfig.getBaseUrl());
        logger.info("Starting test case: " + testName);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult testResult) {
        if (browserFactory != null) {
            browserFactory.stopTracing();
        }
        logger.info("Finished test case: " + testResult.getMethod().getMethodName());
    }

    @AfterClass(alwaysRun = true)
    public void closeFixture() {
        if (browserFactory != null) {
            browserFactory.closeBrowserAndStopAllRecords();
        }
        TestContext.clear();
    }

    public void captureFailureArtifacts() {
        if (browserFactory != null) {
            browserFactory.attachScreenshot("Failure screenshot");
        }
    }

    private String requirement() {
        try {
            java.lang.reflect.Field field = getClass().getDeclaredField("REQ");
            field.setAccessible(true);
            return field.get(null).toString();
        } catch (ReflectiveOperationException exception) {
            return "untracked";
        }
    }

    private String suiteName() {
        try {
            java.lang.reflect.Field field = getClass().getDeclaredField("SUITE");
            field.setAccessible(true);
            return field.get(null).toString();
        } catch (ReflectiveOperationException exception) {
            return getClass().getPackageName();
        }
    }
}
