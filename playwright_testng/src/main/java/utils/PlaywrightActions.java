package utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import config.TestConfig;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;

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
        page.navigate(url);
        waitForPageLoad(TestConfig.getTimeoutSeconds());
    }

    public Locator findLocator(String selector) { return page.locator(selector); }
    public void click(String selector) { findLocator(selector).click(); }
    public void setText(String selector, String text) { findLocator(selector).fill(text); }
    public String getText(String selector) { return findLocator(selector).innerText(); }
    public void verifyLocatorVisible(String selector) { findLocator(selector).waitFor(); }
    public String getPageTitle() { return page.title(); }
    public String getPageUrl() { return page.url(); }
    public Page getPage() { return page; }
    public BrowserContext getContext() { return context; }

    public void waitForPageLoad(int timeoutSeconds) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED,
                    new Page.WaitForLoadStateOptions().setTimeout(timeoutSeconds * 1000.0));
        } catch (RuntimeException exception) {
            logger.warn("Page did not reach DOMContentLoaded: " + exception.getMessage());
        }
    }

    public void takeFullPageScreenshot(String name) {
        Allure.addAttachment(name, "image/png",
                new ByteArrayInputStream(page.screenshot(new Page.ScreenshotOptions().setFullPage(true))),
                "png");
    }

    public void close() {
        if (!page.isClosed()) page.close();
    }
}
