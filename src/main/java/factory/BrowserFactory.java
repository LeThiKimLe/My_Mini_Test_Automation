package factory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import config.ConfigReader;
import config.TestConfig;
import io.qameta.allure.Step;
import utils.Loggers;
import utils.Utils;

public class BrowserFactory {
    private static Playwright playwright;
    private static Browser browser;
    private Loggers logger = new Loggers();
    private Map<String, Page> pages = new HashMap<>();
    private Map<String, BrowserContext> contexts = new HashMap<>();
    private Map<String, Boolean> isTracingStarted = new HashMap<>();
    private Map<String, Path> userDataDirs = new HashMap<>();
    private static ThreadLocal<String> currentContextName = new ThreadLocal<>();
    private static ThreadLocal<Map<String, Path>> threadLocalRecordPaths = ThreadLocal.withInitial(HashMap::new);

    public Browser createBrowser() {
        String browserName = ConfigReader.getEnvironmentProperty("browser");
        if (browserName == null || browserName.trim().isEmpty()) {
            browserName = "chromium";
        }
        return createBrowser(browserName);
    }

    @Step("Create new [ {browserName} ]")
    public Browser createBrowser(String browserName) {
        if (playwright == null) {
            playwright = Playwright.create();
            logger.passed("Playwright has been created");
        }

        boolean headless = Boolean.parseBoolean(ConfigReader.getEnvironmentProperty("headless"));
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless)
                .setArgs(Arrays.asList("--start-maximized"));
        if (browserName.equalsIgnoreCase("chromium")) {
            browser = playwright.chromium().launch(launchOptions);
        } else if (browserName.equalsIgnoreCase("chrome")) {
            browser = playwright.chromium().launch(launchOptions.setChannel("chrome"));
        } else if (browserName.equalsIgnoreCase("edge")) {
            browser = playwright.chromium().launch(launchOptions.setChannel("msedge"));
        } else if (browserName.equalsIgnoreCase("firefox")) {
            browser = playwright.firefox().launch(launchOptions);
        } else if (browserName.equalsIgnoreCase("webkit")) {
            browser = playwright.webkit().launch(launchOptions);
        } else {
            throw new IllegalArgumentException(String.format("Could not Launch Browser for type [%s]", browserName));
        }
        logger.passed(String.format("Create Browser with type [%s] successfully", browserName));
        System.setProperty("browserVersion", browser.version());
        return browser;
    }

    @Step("Create new Page [ {contextName} ]")
    public void createPage(String contextName) {
        if (browser == null) {
            createBrowser();
        }
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setIgnoreHTTPSErrors(true)
                .setViewportSize(null)
                .setRecordVideoDir(Paths.get(ConfigReader.getGlobalVariable("resultsDirectory")))
                .setRecordVideoSize(1280, 720).setAcceptDownloads(true);
        BrowserContext context = browser.newContext(contextOptions);
        contexts.put(contextName, context);
        // Auto start tracing
        startTracing(contextName);
        Page page = context.newPage();
        logger.passed(String.format(
                "[Thread-%d] Create a new Page [%s - %s] from Browser with new Context (%s) successfully",
                Thread.currentThread().getId(), contextName, context.hashCode(), contextName));
        pages.put(contextName, page);
        currentContextName.set(contextName);
    }

    public void createNewPageWithGuestMode(String contextName) throws IOException {
        String browserType = ConfigReader.getEnvironmentProperty("browser");
        if (browserType.equalsIgnoreCase("edge"))
            browserType = "msedge";
        createNewPageWithGuestMode(contextName, browserType);
    }

    @Step("Create new Page [ {contextName}] with Guest mode")
    public void createNewPageWithGuestMode(String contextName, String browserType) throws IOException {
        if (playwright == null){
            playwright = Playwright.create();
            logger.passed("Playwright has been created");
        }

        Path userDataDir = Paths.get(System.getProperty("java.io.tmpdir"), String.format("guest-profile-%s-%s", contextName, UUID.randomUUID()));
        BrowserType. LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
        .setHeadless(false)
        .setArgs(Arrays. asList("--guest", "--start-maximized"))
        .setViewportSize(null)
        .setChannel(browserType).setIgnoreHTTPSErrors(true).setViewportSize(null)
        .setRecordVideoDir(Paths.get(ConfigReader.getGlobalVariable("resultsDirectory"))).setRecordVideoSize(1280, 720).setAcceptDownloads(true);
        BrowserContext context = playwright.chromium().launchPersistentContext(userDataDir, options);
        userDataDirs.put(contextName, userDataDir);
        contexts.put(contextName, context);
        // Auto start tracing
        startTracing(contextName);
        Page page = context.pages().get(0);
        pages.put(contextName, page);
    }

    public Page getPage(String contextName) {
        return pages.get(contextName);
    }

    public void setPage(Page page, String contextName) {
        pages.put(contextName, page);
    }

    public BrowserContext getContext(String contextName){
        return contexts.get(contextName);
    }

    public void closeBrowserAndStopAllRecords() {
        for (Map.Entry<String, BrowserContext> entry : contexts.entrySet()) {
            for (Page page : entry.getValue().pages()) {
                if (!page.isClosed()) {
                    if (page.video() != null) {
                        addRecordPath(entry.getKey(), page.video().path());
                    }
                    page.close();
                }
            }
            entry.getValue().close();
        }
        contexts.clear();
        pages.clear();
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
        for (Map.Entry<String, Path> entry : threadLocalRecordPaths.get().entrySet()) {
            Utils.moveRecordFileToCorrectDirectory(entry.getValue(), entry.getKey());
        }
        logger.passed("Close all pages and contexts of browser successfully");
        logger.passed("Close Playwright successfully");
    }

    public static void close() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    public void startTracingAll() {
        for (String pageName : contexts.keySet()) {
            startTracing(pageName);
        }
    }

    public void stopTracingAll() {
        for (String pageName : contexts.keySet()) {
            stopTracing(pageName);
        }
    }

    public void startTracing(String contextName) {
        if (!isTracingStarted(contextName)) {
            getContext(contextName).tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(false));
            isTracingStarted.put(contextName, true);
        }
    }

    public void stopTracing(String contextName) {
        if (isTracingStarted(contextName)) {
            isTracingStarted.remove(contextName);
            getContext(contextName).tracing().stop(new Tracing.StopOptions().setPath(Utils.getTracePath(contextName)));
        }
    }

    private boolean isTracingStarted(String contextName) {
        return isTracingStarted.getOrDefault(contextName, false);
    }

    @Step("Close Page [{contextName} ]")
    public void closePageAndContext(String contextName) throws IOException {
        Page page = getPage(contextName);
        Path recordPath = page.video().path();
        stopTracing(contextName);
        page.close();
        getContext(contextName).close();
        pages.remove(contextName);
        contexts.remove(contextName);
        addRecordPath(recordPath);
        if (userDataDirs.keySet().contains(contextName)) {
            Utils.deleteFolder(userDataDirs.get(contextName));
        }
    }

    public void setCurrentContextName(String contextName) {
        currentContextName.set(contextName);
    }

    public static void addRecordPath(Path path) {
        addRecordPath(currentContextName.get(), path);
    }

    public static void addRecordPath(String contextName, Path path) {
        long numberRecordOfCurrentContext = threadLocalRecordPaths.get().keySet().stream()
                .filter(k -> k.equals(contextName) || k.startsWith(String.format("%s_", contextName))).count();
        String finalContextName = (numberRecordOfCurrentContext == 0) ? contextName
                : String.format("%s_%s", contextName, numberRecordOfCurrentContext + 1);
        threadLocalRecordPaths.get().put(finalContextName, path);
    }
    
}
