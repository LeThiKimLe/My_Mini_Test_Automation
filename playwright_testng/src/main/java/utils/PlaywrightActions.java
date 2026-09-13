package utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import config.TestConfig;
import io.qameta.allure.Allure;
import io.qameta.allure.model.StepResult;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.UUID;

/** Shared, report-friendly Playwright operations used by every page object. */
public class PlaywrightActions {
    private final Page page;
    private final BrowserContext context;
    private final Loggers logger = new Loggers();

    public PlaywrightActions(BrowserContext context, Page page) {
        this.context = context;
        this.page = page;
    }

    public void navigateToUrl(String url) {
        try {
            page.navigate(url);
            waitForPageLoad(TestConfig.getTimeoutSeconds());
        } catch (RuntimeException exception) {
            logger.failed("Failed to navigate to url [" + url + "]: " + exception.getMessage());
            throw exception;
        }
    }

    public Locator findLocator(String selector) { return page.locator(selector); }
    public void click(String selector) {
        try {
            findLocator(selector).click();
            logger.objectInfo(String.format("Clicked on Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (RuntimeException exception) {
            logger.failed(String.format("Click action on Locator '%s' - [%s] failed. Details: %s",
                    Locators.getLocatorName(), selector, exception));
            throw exception;
        }
    }

    public void setText(String selector, String text) {
        try {
            findLocator(selector).fill(text);
            logger.objectInfo(String.format("Set text of Locator '%s' to '%s' successfully.",
                    Locators.getLocatorName(), text));
        } catch (RuntimeException exception) {
            logger.failed(String.format("Set text action on Locator '%s' - [%s] to '%s' failed. Details: %s",
                    Locators.getLocatorName(), selector, text, exception));
            throw exception;
        }
    }

    public String getText(String selector) {
        try {
            String text = findLocator(selector).innerText().trim();
            logger.objectInfo(String.format("Text of Locator '%s' is: '%s'", Locators.getLocatorName(), text));
            return text;
        } catch (RuntimeException exception) {
            logger.failed(String.format("Get Text of Locator '%s' - [%s] failed. Details: %s",
                    Locators.getLocatorName(), selector, exception));
            throw exception;
        }
    }

    public void verifyLocatorVisible(String selector) {
        long start = System.currentTimeMillis();
        try {
            findLocator(selector).waitFor();
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.passed(String.format("Locator [%s] is visible in %.2f seconds",
                    Locators.getLocatorName(), duration));
        } catch (RuntimeException exception) {
            logger.failed("Locator [" + Locators.getLocatorName() + "] is not visible: " + exception.getMessage());
            throw exception;
        }
    }
    public String getPageTitle() { return page.title(); }
    public String getPageUrl() { return page.url(); }
    public Page getPage() { return page; }
    public BrowserContext getContext() { return context; }

    public void waitForPageLoad(int timeoutSeconds) {
        try {
            long start = System.currentTimeMillis();
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(timeoutSeconds * 1000.0));
            page.waitForLoadState(LoadState.LOAD,
                    new Page.WaitForLoadStateOptions().setTimeout(timeoutSeconds * 1000.0));
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.objectInfo(String.format("Page loaded in %.2f seconds", duration));
        } catch (RuntimeException exception) {
            logger.failed("Failed to wait for page load: " + exception.getMessage());
        }
    }

    public void takeFullPageScreenshot() {
        takeFullPageScreenshot("Screenshot");
    }

    public void takeFullPageScreenshot(String name) {
        boolean testCaseRunning = Allure.getLifecycle().getCurrentTestCase().isPresent();
        String stepId = UUID.randomUUID().toString();
        if (testCaseRunning) {
            Allure.getLifecycle().startStep(stepId, new StepResult().setName("Screenshot"));
        }
        try {
            Path screenshotPath = Utils.getScreenshotPath();
            Utils.createDirectoryIfNotExists(screenshotPath.getParent());
            String imageName = screenshotPath.getFileName().toString();
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));
            Allure.addAttachment(imageName.replace(".png", ""), "image/png",
                    new ByteArrayInputStream(screenshot), "png");
            logger.objectInfo(String.format("A full page screenshot was taken - [%s]", imageName));
        } catch (RuntimeException exception) {
            logger.failed("Failed to take screenshot: " + exception.getMessage());
        } finally {
            if (testCaseRunning) {
                Allure.getLifecycle().stopStep(stepId);
            }
        }
    }

    public void close() {
        if (!page.isClosed()) page.close();
    }
}
