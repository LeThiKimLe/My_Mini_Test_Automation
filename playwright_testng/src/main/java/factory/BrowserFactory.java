package factory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import config.TestConfig;
import io.qameta.allure.Allure;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import utils.Loggers;
import utils.TestContext;
import utils.Utils;

/** The single owner of Playwright, browser, context, tracing, and video lifecycle. */
public class BrowserFactory {
    private final Loggers logger = new Loggers();
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private boolean tracing;
    private Path tracePath;

    public void createBrowser() {
        if (browser != null) {
            return;
        }
        playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.isHeadless());
        String browserName = TestConfig.getBrowser().toLowerCase();
        if ("firefox".equals(browserName)) {
            browser = playwright.firefox().launch(options);
        } else if ("webkit".equals(browserName)) {
            browser = playwright.webkit().launch(options);
        } else if ("chrome".equals(browserName)) {
            browser = playwright.chromium().launch(options.setChannel("chrome"));
        } else if ("edge".equals(browserName)) {
            browser = playwright.chromium().launch(options.setChannel("msedge"));
        } else if ("chromium".equals(browserName)) {
            browser = playwright.chromium().launch(options);
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }
        System.setProperty("browserVersion", browser.version());
        logger.passed("Created " + browserName + " browser");
    }

    public void createPage(String contextName) {
        if (browser == null) {
            createBrowser();
        }
        Path videoDirectory = Utils.resultDirectory().resolve(TestContext.getTestClassName() == null
                ? "unknown" : TestContext.getTestClassName()).resolve("videos");
        try {
            Files.createDirectories(videoDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create video directory", exception);
        }
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setIgnoreHTTPSErrors(true)
                .setViewportSize(null)
                .setAcceptDownloads(true)
                .setRecordVideoDir(videoDirectory)
                .setRecordVideoSize(1280, 720);
        context = browser.newContext(options);
        page = context.newPage();
        logger.passed("Created page in context [" + contextName + "]");
    }

    public void startTracing() {
        if (context != null && !tracing) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true).setSnapshots(true).setSources(false));
            tracing = true;
        }
    }

    public void stopTracing() {
        if (context != null && tracing) {
            tracePath = Utils.resultDirectory()
                    .resolve(TestContext.getTestClassName() == null ? "unknown" : TestContext.getTestClassName())
                    .resolve("traces")
                    .resolve(safeName(TestContext.getTestCaseName()) + ".zip");
            try {
                Files.createDirectories(tracePath.getParent());
                context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to create trace directory", exception);
            } finally {
                tracing = false;
            }
            Utils.attachFile("Playwright trace", "application/zip", tracePath);
        }
    }

    public Page getPage(String contextName) { return page; }
    public BrowserContext getContext(String contextName) { return context; }
    public Page getPage() { return page; }

    public void closeBrowserAndStopAllRecords() {
        Path videoPath = null;
        if (context != null) {
            if (tracing) {
                stopTracing();
            }
            if (page != null && !page.isClosed()) {
                if (page.video() != null) {
                    videoPath = page.video().path();
                }
                page.close();
            }
            if (videoPath != null) {
                Utils.attachFile("Playwright video", "video/webm", videoPath);
            }
            context.close();
            context = null;
            page = null;
        }
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
        logger.info("Closed browser and Playwright");
    }

    public void attachScreenshot(String name) {
        if (page == null || page.isClosed()
                || !Allure.getLifecycle().getCurrentTestCase().isPresent()) {
            return;
        }
        Allure.addAttachment(name, "image/png",
                new java.io.ByteArrayInputStream(page.screenshot(new Page.ScreenshotOptions().setFullPage(true))),
                "png");
    }

    private static String safeName(String value) {
        if (value == null || value.trim().isEmpty()) return "test";
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
