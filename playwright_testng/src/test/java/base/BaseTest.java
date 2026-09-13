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
    protected static final String DEFAULT_CONTEXT_NAME = "Default";
    
    // Thread-local storage for shared instances
    private static final ThreadLocal<LoginPage> threadLocalPage = new ThreadLocal<>();
    private static final ThreadLocal<ProductPage> threadLocalProductPage = new ThreadLocal<>();
    private static final ThreadLocal<BrowserFactory> threadLocalBrowserFactory = new ThreadLocal<>();
    private static final ThreadLocal<Loggers> threadLocalLogger = new ThreadLocal<>();
    private static final ThreadLocal<AuthenticationFlow> threadLocalAuthenticationFlow = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalCurrentPageName = new ThreadLocal<>();
    private static final ThreadLocal<PlaywrightActions> threadLocalWebUI = new ThreadLocal<>();
    
    // Instance variables
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
        String requirement = requirement();
        TestContext.set(suiteName, className, className, requirement());
        TestContext.setNeedReplaceSuiteName(!suiteName.equals(getClass().getPackageName()));
        TestContext.setNumberOfTestCases(0);
        Utils.prepareTestDirectory();
        browserFactory = getBrowserFactory();
        browserFactory.createBrowser();
        browserFactory.createPage(DEFAULT_CONTEXT_NAME);
        page = browserFactory.getPage(DEFAULT_CONTEXT_NAME);
        webUI = new PlaywrightActions(browserFactory.getContext(DEFAULT_CONTEXT_NAME), page);
        threadLocalWebUI.set(webUI);
        logger = getLogger();
        loginPage = new LoginPage(webUI);
        productPage = new ProductPage(webUI);
        authenticationFlow = new AuthenticationFlow(loginPage, productPage);
        logger.info(String.format("REQ value for test class %s: %s", getClass().getName(), requirement));
        logger.info(String.format("SUITE of Test Class [%s] is [%s]", getClass().getName(), suiteName));
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestResult testResult) {
        String testName = displayName(testResult);
        testCount++;
        TestContext.set(suiteName(), getClass().getSimpleName(), testName, requirement());
        TestContext.setNeedReplaceSuiteName(!suiteName().equals(getClass().getPackageName()));
        TestContext.setNumberOfScreenshots(1);
        TestContext.setNumberOfTestCases(testCount);
        Utils.cleanAndPrepareResultDirectories(testName, getClass().getSimpleName());
        browserFactory.startTracing();
        webUI.navigateToUrl(TestConfig.getBaseUrl());
        logger.info("Starting test case: " + testName);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult testResult) {
        if (browserFactory != null) {
            browserFactory.stopTracing();
        }
        Utils.takeWindowsScreenshot();
        logger.info("Finished test case: " + displayName(testResult));
    }

    @AfterClass(alwaysRun = true)
    public void closeFixture() {
        if (browserFactory != null) {
            browserFactory.closeBrowserAndStopAllRecords();
        }
        TestContext.clear();
        threadLocalBrowserFactory.remove();
        threadLocalWebUI.remove();
        threadLocalLogger.remove();
        threadLocalPage.remove();
        threadLocalProductPage.remove();
        threadLocalAuthenticationFlow.remove();
        threadLocalCurrentPageName.remove();
    }

    public void captureFailureArtifacts() {
        if (browserFactory != null) {
            browserFactory.attachScreenshot("Failure screenshot");
        }
    }

    // Static getters for use in listeners and other static contexts
    public static PlaywrightActions getPlaywrightActionsObject() {
        return threadLocalWebUI.get();
    }

    public static BrowserFactory getBrowserFactoryObject() {
        return threadLocalBrowserFactory.get();
    }

    public static Loggers getLoggerObject() {
        return threadLocalLogger.get();
    }

    public static String getCurrentPageName() {
        return threadLocalCurrentPageName.get();
    }

    // Instance getters
    protected BrowserFactory getBrowserFactory() {
        if (threadLocalBrowserFactory.get() == null) {
            threadLocalBrowserFactory.set(new BrowserFactory());
        }
        return threadLocalBrowserFactory.get();
    }

    protected PlaywrightActions getPlaywrightActions() {
        if (threadLocalWebUI.get() == null) {
            threadLocalWebUI.set(new PlaywrightActions(getBrowserFactory().getContext(DEFAULT_CONTEXT_NAME), 
                    getBrowserFactory().getPage(DEFAULT_CONTEXT_NAME)));
            threadLocalCurrentPageName.set(DEFAULT_CONTEXT_NAME);
        }
        return threadLocalWebUI.get();
    }

    protected Loggers getLogger() {
        if (threadLocalLogger.get() == null) {
            threadLocalLogger.set(new Loggers());
        }
        return threadLocalLogger.get();
    }

    protected LoginPage getLoginPage() {
        if (threadLocalPage.get() == null) {
            threadLocalPage.set(new LoginPage(getPlaywrightActions()));
        }
        return threadLocalPage.get();
    }

    protected ProductPage getProductPage() {
        if (threadLocalProductPage.get() == null) {
            threadLocalProductPage.set(new ProductPage(getPlaywrightActions()));
        }
        return threadLocalProductPage.get();
    }

    protected AuthenticationFlow getAuthenticationFlow() {
        if (threadLocalAuthenticationFlow.get() == null) {
            threadLocalAuthenticationFlow.set(new AuthenticationFlow(getLoginPage(), getProductPage()));
        }
        return threadLocalAuthenticationFlow.get();
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

    private String displayName(ITestResult testResult) {
        String description = testResult.getMethod().getDescription();
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return testResult.getMethod().getMethodName();
    }
}
