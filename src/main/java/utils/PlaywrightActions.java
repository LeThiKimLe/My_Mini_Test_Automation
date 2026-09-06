package utils;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import io.qameta.allure.Allure;
import io.qameta.allure.model.StepResult;

public class PlaywrightActions {

    private static final Loggers logger = new Loggers();
    private Page page = null;
    private Page previousPage = null;
    private BrowserContext context = null;
    private Map<Integer, Integer> numberOfPages = new HashMap<>();
    private int defaultTimeout = 5; // Default timeout in seconds

    public PlaywrightActions() {

    }

    public PlaywrightActions(Page page) {
        this.page = page;
    }

    public PlaywrightActions(BrowserContext context, Page page) {
        this.context = context;
        this.page = page;
    }

    // Keyword used to switch between multi browser
    public void switchToSpecificContextWithPageIndex(BrowserContext context, int pageIndex) {
        this.page = context.pages().get(pageIndex);
        this.context = context;
        this.page = context.pages().get(pageIndex);
        this.page.bringToFront();
        this.page.bringToFront();
        this.page.bringToFront();
    }

    public void takeFullPageScreenshot() {
        Allure.getLifecycle().startStep(UUID.randomUUID().toString(), new StepResult().setName("Screenshot"));
        try {
            Path screenshotPath = Utils.getScreenshotPath();
            String imageName = screenshotPath.toString().substring(screenshotPath.toString().lastIndexOf('\\') + 1);
            Page.ScreenshotOptions screenshotOptions = new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true);
            byte[] screenshot = this.page.screenshot(screenshotOptions);
            Allure.addAttachment(imageName.replace(".png", ""), "image/png", new ByteArrayInputStream(screenshot), ".png");
            logger.objectInfo(String.format("A full page screenshot was taken - [%s]", imageName));
        } catch (Exception e) {
            logger.failed("Failed to take screenshot: " + e.getMessage());
        } finally {
            Allure.getLifecycle().stopStep();
        }
    }

    public void takeLocatorScreenshot(String locator) {
        Allure.getLifecycle().startStep(UUID.randomUUID().toString(), new StepResult().setName("Screenshot"));
        try {
            Path screenshotPath = Utils.getScreenshotPath();
            String imageName = screenshotPath.toString().substring(screenshotPath.toString().lastIndexOf('\\') + 1);
            byte[] screenshotBytes = page.locator(locator)
                    .screenshot(new Locator.ScreenshotOptions().setPath(screenshotPath));
            Allure.addAttachment(imageName.replace(".png", ""), "image/png", new ByteArrayInputStream(screenshotBytes), ".png");
            logger.objectInfo(String.format("A screenshot of locator [%s] was taken - [%s]", Locators.getLocatorName(),
                    imageName));
        } catch (Exception e) {
            logger.failed("Failed to take screenshot: " + e.getMessage());
        } finally {
            Allure.getLifecycle().stopStep();
        }
    }

    public String getRecordPath() {
        return page.video() != null ? page.video().path().toString() : null;
    }

    public void click(String locator) {
        page.locator(locator).click();
    }

    public void authenticate(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        page.route("**/*", route -> {
            route.fetch(new Route.FetchOptions().setHeaders(Map.of("Authorization", "Basic " + encodedCredentials)));
        });
    }

    public void navigateToUrl(String url) throws Exception {
        page.navigate(url);
        waitForPageLoad(30);
    }

    public Locator findLocator(String locator) {
        return page.locator(locator);
    }

    public List<Locator> findLocators(String locator) {
        return page.locator(locator).all();
    }

    public Locator findLocatorFromLocator(Locator locator, String childLocator) {
        return locator.locator(childLocator);
    }

    public String getPageTitle() {
        return page.title();
    }

    public String getPageUrl() {
        return page.url();
    }

    public String backToPreviousPageFromBrowserBackBtn() {
        page.goBack();
        return page.title();
    }

    public int getPageIndex() {
        List<Page> pages = context.pages();
        return pages.indexOf(page);
    }

    public void goBack() {
        page.goBack();
    }

    public void delay(int timeout) {
        page.waitForTimeout(timeout * 1000);
    }

    public void delay(Double timeout) {
        page.waitForTimeout(timeout * 1000);
    }

    public void waitForPageLoad(int timeout) {
        long start = System.currentTimeMillis();
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(timeout * 1000));
            page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(timeout * 1000));
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.objectInfo(String.format("Page loaded in %.2f seconds", duration));
        } catch (Exception e) {
            logger.failed("Failed to wait for page load: " + e.getMessage());
        }
    }

    public void switchToPageIndex(int index) {
        List<Page> pages = context.pages();
        for (int second = 0; second < 3; second++) {
            pages = context.pages();
            if (pages.size() > index) {
                break;
            }
            page.waitForTimeout(1000);
        }
        if (index >= 0 && index < pages.size()) {
            previousPage = this.page;
            this.page = pages.get(index);
            this.page.bringToFront();
        } else {
            logger.failed("Invalid page index: " + index);
        }
    }

    public void switchToPageIndex(int index, boolean closePrevious) {
        List<Page> pages = context.pages();
        for (int second = 0; second < 3; second++) {
            pages = context.pages();
            if (pages.size() > index) {
                break;
            }
            page.waitForTimeout(1000);
        }
        if (index >= 0 && index < pages.size()) {
            previousPage = this.page;
            this.page = pages.get(index);
            this.page.bringToFront();
            if (closePrevious && previousPage != null) {
                previousPage.close();
                previousPage = null;
            }
        } else {
            logger.failed("Invalid page index: " + index);
        }
    }

    public int getNUmberOfTab() {
        return context.pages().size();
    }

    public void switchToPageTitle(String title) {
        List<Page> pages = context.pages();
        for (Page p : pages) {
            if (p.title().equals(title)) {
                previousPage = this.page;
                this.page = p;
                this.page.bringToFront();
                logger.passed("Switched to page with title: " + title);
                return;
            }
        }
        logger.failed("No page found with title: " + title);
    }

    public void switchToPreviousPage(boolean closeNewPage) {
        if (previousPage != null && page != previousPage) {
            Page newPage = page;
            this.page = previousPage;
            this.page.bringToFront();
            previousPage = null;
            logger.passed("Switched back to the previous page.");
            if (closeNewPage) {
                newPage.close();
                numberOfPages.put(page.context().hashCode(),
                        numberOfPages.getOrDefault(page.context().hashCode(), 1) - 1);
            }
        } else {
            logger.failed("No previous page to switch to.");
        }
    }

    public void switchToNewPage(boolean closeOldPage) {
        int currentNumberOfPages = numberOfPages.get(page.context().hashCode());
        List<Page> pages = new ArrayList<>();
        for (int second = 0; second < 100; second++) {
            pages = context.pages();
            if (pages.size() > currentNumberOfPages) {
                break;
            }
            page.waitForTimeout(100);
        }
        if (pages.size() == 1) {
            logger.failed("No new page found to switch to.");
            return;
        }
        numberOfPages.put(page.context().hashCode(), currentNumberOfPages + 1);
        previousPage = this.page;
        this.page = pages.get(pages.size() - 1);
        this.page.bringToFront();
        logger.passed("Switched to the new page.");
        if (closeOldPage && previousPage != null) {
            previousPage.close();
            previousPage = null;
        }
    }

    public void switchToNewPage() {
        switchToNewPage(false);
    }

    public void switchToNewPageAndClosePreviousPage() {
        switchToPreviousPage(true);
    }

    public void waitForLocatorVisible(String selector, int timeout) {
        long start = System.currentTimeMillis();
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeout * 1000));
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.objectPassed(
                    String.format("Locator [%s] became visible in %.2f seconds", Locators.getLocatorName(), duration));
        } catch (Exception e) {
            logger.failed("Failed to wait for locator to become visible: " + e.getMessage());
        }
    }

    public void waitForLocatorVisible(String selector) {
        waitForLocatorVisible(selector, defaultTimeout);
    }

    public void verifyLocatorVisible(String selector, int timeout) {
        long start = System.currentTimeMillis();
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeout * 1000));
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.passed(
                    String.format("Locator [%s] is visible in %.2f seconds", Locators.getLocatorName(), duration));
        } catch (Exception e) {
            logger.failed("Locator [" + Locators.getLocatorName() + "] is not visible: " + e.getMessage());
        }
    }

    public void verifyLocatorVisible(String selector) {
        verifyLocatorVisible(selector, defaultTimeout);
    }

    public void waitForLocatorNotPresent(String selector, int timeout, boolean showlog) {
        for (int second = 1; second <= timeout * 10; second++) {
            if (page.locator(selector).count() == 0) {
                if (showlog) {
                    logger.passed(String.format("Locator [%s] is not present on the page.", Locators.getLocatorName()));
                } else {
                    logger.objectPassed(
                            String.format("Locator [%s] is not present on the page.", Locators.getLocatorName()));
                }
                return;
            }
            page.waitForTimeout(100);
        }
        logger.failed(String.format("Locator [%s] is still present on the page after %d seconds.",
                Locators.getLocatorName(), timeout));
    }

    public void waitForLocatorNotPresent(String selector, int timeout) {
        waitForLocatorNotPresent(selector, timeout, true);
    }

    public void waitForLocatorNotPresent(String selector) {
        waitForLocatorNotPresent(selector, defaultTimeout, true);
    }

    public boolean checkLocatorPresent(String selector, int timeout, boolean verifyPresent) {
        for (int second = 1; second <= timeout * 10; second++) {
            if (page.locator(selector).count() > 0) {
                if (!verifyPresent) {
                    logger.objectPassed(String.format("Locator [%s] is present on the page.", Locators.getLocatorName()));
                    return true;
                }
                logger.objectInfo(String.format("Locator [%s] is present on the page.", Locators.getLocatorName()));
                return true;
            }
            page.waitForTimeout(100);
        }
        if (verifyPresent) {
            logger.failed(String.format("Locator [%s] is not present on the page.", Locators.getLocatorName()));
        } 
        logger.objectInfo(String.format("Locator [%s] is not present on the page.", Locators.getLocatorName()));
        return false;
    }

    public boolean checkLocatorPresent(String locator) {
        return checkLocatorPresent(locator, defaultTimeout);
    }

    public boolean checkLocatorPresent(String locator, int timeout) {
        return checkLocatorPresent(locator, timeout, true);
    }

    public void waitForLocatorPresent(String locator, int timeout) {
        checkLocatorPresent(locator, timeout, true);
    }

    public void waitForLocatorPresent(String locator) {
        waitForLocatorPresent(locator, defaultTimeout);
    }

    


    







    
}
