package base;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import io.qameta.allure.junit5.AllureJunit5;
import listener.AllureStepScreenshotListener;

import config.TestConfig;
import factory.BrowserFactory;
import flow.AuthenticationFlow;
import pages.LoginPage;
import pages.ProductPage;
import utils.Loggers;
import utils.PlaywrightActions;
import utils.Utils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({AllureJunit5.class, AllureStepScreenshotListener.class})
public class BaseTest {
    protected Page page;

    //Thread Local to store the object for each test thread
    private ThreadLocal<LoginPage> threadLocalPage = new ThreadLocal<>();
    private ThreadLocal<ProductPage> threadLocalProductPage = new ThreadLocal<>();
    private ThreadLocal<BrowserFactory> threadLocalBrowserFactory = new ThreadLocal<>();
    private static final ThreadLocal<Loggers> threadLocalLogger = new ThreadLocal<>();
    private ThreadLocal<AuthenticationFlow> threadLocalAuthenticationFlow = new ThreadLocal<>();
    private static ThreadLocal<String> threadLocalCurrentPageName = new ThreadLocal<>();
    private static ThreadLocal<PlaywrightActions> threadLocalWebUI = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalTestCaseName = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalTestClassName = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalTestSuiteName = new ThreadLocal<>();
    private static ThreadLocal<Integer> threadLocalNumberOfScreenshots = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<Integer> threadLocalNumberOfTestCases = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<String> threadLocalREQ = new ThreadLocal<>();
    private static ThreadLocal<Boolean> threadLocalNeedReplaceSuiteName = ThreadLocal.withInitial(() -> false);
    private boolean pageBaseInitialized;
    protected static final String defaultContextName = "Default";

    // Pages
    protected LoginPage loginPage;
    protected ProductPage productPage;
    protected BrowserFactory browserFactory;
    protected PlaywrightActions webUI;
    protected Loggers logger;

    // Flows
    protected AuthenticationFlow authenticationFlow;

    @BeforeAll
    static void setupBrowser() {
        // Allure results are cleaned once per session by listener.LauncherSessionListener.
        // This call is a safe no-op if the listener already ran (AtomicBoolean guard in Utils).
        Utils.cleanAllureResults();
    }

    void initPageBase() {
        initPageBase(true);
    }

    private void initPageBase(boolean createBrowserAndPage) {
        String testClassName = this.getClass().getName();
        testClassName = testClassName.substring(testClassName.lastIndexOf(".") + 1);
        threadLocalTestSuiteName.set(this.getClass().getPackageName());
        threadLocalTestClassName.set(testClassName);
        threadLocalNeedReplaceSuiteName.set(false);
        threadLocalNumberOfTestCases.set(0);
        utils.TestContext.setTestSuiteName(this.getClass().getPackageName());
        utils.TestContext.setTestClassName(testClassName);
        utils.TestContext.setNeedReplaceSuiteName(false);
        utils.TestContext.setNumberOfTestCases(0);
        if (createBrowserAndPage) {
            getBrowserFactory().createBrowser();
            createNewPageWithNewContext(defaultContextName);
        }
        webUI = getPlaywrightActions();
        this.page = getBrowserFactory().getPage(defaultContextName);
        logger = getLogger();
        loginPage = getLoginPage();
        productPage = getProductPage();
        authenticationFlow = getAuthenticationFlow();
        
        Class<?> clazz = this.getClass();
        try {
            Field field = clazz.getDeclaredField("REQ");
            field.setAccessible(true);
            threadLocalREQ.set((String) field.get(null));
            utils.TestContext.setREQ(threadLocalREQ.get());
            logger.info(String.format("REQ value for test class %s: %s", clazz.getName(), threadLocalREQ.get())); 

            Field suite = clazz.getDeclaredField("SUITE");
            suite.setAccessible(true);
            String suiteName = (String) suite.get(null);
            threadLocalTestSuiteName.set(suiteName);
            threadLocalNeedReplaceSuiteName.set(true);
            utils.TestContext.setTestSuiteName(suiteName);
            utils.TestContext.setNeedReplaceSuiteName(true);
            logger.info(
                String.format(
                    "SUITE of Test Class [%s] is [%s]",
                    clazz.getName(),
                    suiteName
                )
            );
        } catch (Exception e) {
            logger.warn("Error occurred while fetching REQ/Suite value in " + clazz.getName());
        }
    }

    @BeforeEach
    public void testSetup(TestInfo testInfo) {
        if (!pageBaseInitialized) {
            initPageBase();
            pageBaseInitialized = true;
        }
        threadLocalTestCaseName.set(testInfo.getDisplayName());
        threadLocalNumberOfScreenshots.set(1);
        threadLocalNumberOfTestCases.set(threadLocalNumberOfTestCases.get() + 1);
        utils.TestContext.setTestCaseName(testInfo.getDisplayName());
        utils.TestContext.setNumberOfScreenshots(1);
        utils.TestContext.setNumberOfTestCases(threadLocalNumberOfTestCases.get());
        Utils.cleanAndPrepareResultDirectories(threadLocalTestCaseName.get(), threadLocalTestClassName.get());
        logger.info(String.format("Starting test case: %s", threadLocalTestCaseName.get()));
        getBrowserFactory().startTracingAll();
        getPlaywrightActions().navigateToUrl(TestConfig.getBaseUrl());
    }

    @AfterEach
    public void testTeardown(TestInfo testInfo) {
        Utils.takeWindowsScreenshot();
        switchToDefaultPage();
    }

    @AfterAll
    public void closeBrowserAndStopAllRecords() {
        getBrowserFactory().closeBrowserAndStopAllRecords();
        threadLocalCurrentPageName.remove();
        threadLocalBrowserFactory.remove();
        threadLocalWebUI.remove();
        threadLocalLogger.remove();
    }

    public static String getTestCaseName() {
        return utils.TestContext.getTestCaseName();
    }

    public static String getTestClassName() {
        return utils.TestContext.getTestClassName();
    }

    public static String getTestSuiteName() {
        return utils.TestContext.getTestSuiteName();
    }

    public static Boolean getNeedReplaceSuiteName() {
        return utils.TestContext.getNeedReplaceSuiteName();
    }

    public static PlaywrightActions getPlaywrightActionsObject() {
        return threadLocalWebUI.get();
    }

    public static String getCurrentPageName() {
        return threadLocalCurrentPageName.get();
    }

    public static int getNumberOfScreenshots() {
        return utils.TestContext.getNumberOfScreenshots();
    }

    public static int getNumberOfTestCases() {
        return utils.TestContext.getNumberOfTestCases();
    }

    public static String getREQ() {
        return utils.TestContext.getREQ();
    }

    protected Loggers getLogger() {
        if (threadLocalLogger.get() == null) {
            threadLocalLogger.set(new Loggers());
        }
        return threadLocalLogger.get();
    }

    private BrowserFactory getBrowserFactory() {
        if (threadLocalBrowserFactory.get() == null) {
            threadLocalBrowserFactory.set(new BrowserFactory());
        }
        return threadLocalBrowserFactory.get();
    }

    private void createNewPageWithNewContext(String contextName) {
        getBrowserFactory().createPage(contextName);
    }

    protected void createNewPageWithGuestMode(String contextName) {
        try {
            getBrowserFactory().createNewPageWithGuestMode(contextName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PlaywrightActions getPlaywrightActions() {
        if (threadLocalWebUI.get() == null) {
            threadLocalWebUI.set(new PlaywrightActions(getBrowserFactory().getContext(defaultContextName), getBrowserFactory().getPage(defaultContextName)));
            threadLocalCurrentPageName.set(defaultContextName);
        }
        return threadLocalWebUI.get();
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

    protected void closePageAndContext(String contextName) throws IOException {
        getBrowserFactory().closePageAndContext(contextName);
    }

    protected void switchToAnotherContext(String contextName) {
        threadLocalWebUI.get().switchToSpecificContextWithFirstPage(getBrowserFactory().getContext(contextName));
        threadLocalCurrentPageName.set(contextName);
    }

    protected void switchToDefaultPage() {
        switchToAnotherContext(defaultContextName);
    }
}
