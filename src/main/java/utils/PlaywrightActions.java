package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.MouseButton;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import io.qameta.allure.Allure;
import io.qameta.allure.model.StepResult;
import utils.Locators;
import utils.Loggers;

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

    public void switchToSpecificContextWithFirstPage(BrowserContext context) {
        switchToSpecificContextWithPageIndex(context, 0);
    }

    public void switchToSpecificContextWithLastPage(BrowserContext context) {
        switchToSpecificContextWithPageIndex(context, context.pages().size() - 1);
    }

    public void takeFullPageScreenshot() {
        boolean testCaseRunning = Allure.getLifecycle().getCurrentTestCase().isPresent();
        String stepId = UUID.randomUUID().toString();
        if (testCaseRunning) {
            Allure.getLifecycle().startStep(stepId, new StepResult().setName("Screenshot"));
        }
        try {
            Path screenshotPath = Utils.getScreenshotPath();
            String imageName = screenshotPath.toString().substring(screenshotPath.toString().lastIndexOf('\\') + 1);
            Page.ScreenshotOptions screenshotOptions = new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true);
            Allure.addAttachment(imageName.replace(".png", ""),
                    new ByteArrayInputStream(this.page.screenshot(screenshotOptions)));
            logger.objectInfo(String.format("A full page screenshot was taken - [%s]", imageName));
        } catch (Exception e) {
            logger.failed("Failed to take screenshot: " + e.getMessage());
        } finally {
            if (testCaseRunning) {
                Allure.getLifecycle().stopStep(stepId);
            }
        }
    }

    public void takeLocatorScreenshot(String locator) {
        boolean testCaseRunning = Allure.getLifecycle().getCurrentTestCase().isPresent();
        String stepId = UUID.randomUUID().toString();
        if (testCaseRunning) {
            Allure.getLifecycle().startStep(stepId, new StepResult().setName("Screenshot"));
        }
        try {
            Path screenshotPath = Utils.getScreenshotPath();
            String imageName = screenshotPath.toString().substring(screenshotPath.toString().lastIndexOf('\\') + 1);
            byte[] screenshotOptions = page.locator(locator)
                    .screenshot(new Locator.ScreenshotOptions().setPath(screenshotPath));
            Allure.addAttachment(imageName.replace(".png", ""), new ByteArrayInputStream(screenshotOptions));
            logger.objectInfo(String.format("A screenshot of locator [%s] was taken - [%s]", Locators.getLocatorName(),
                    imageName));
        } catch (Exception e) {
            logger.failed("Failed to take screenshot: " + e.getMessage());
        } finally {
            if (testCaseRunning) {
                Allure.getLifecycle().stopStep(stepId);
            }
        }
    }

    public String getRecordPath() {
        return (page != null && page.video() != null) ? page.video().path().toString() : "";
    }

    public void authenticate(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        page.route("**/*", route -> {
            route.resume(new Route.ResumeOptions().setHeaders(Map.of("Authorization", "Basic " + encodedCredentials)));
        });
    }

    public void navigateToUrl(String url) {
        try {
            page.navigate(url);
            waitForPageLoad(30);
        } catch (Exception e) {
            logger.failed("Failed to navigate to url [" + url + "]: " + e.getMessage());
            throw e;
        }
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

    public boolean checkLocatorIsVisible(String locator, double timeout) {
        int times = (int) (timeout * 10);
        for (int i = 1; i <= times; i++) {
            if (page.locator(locator).isVisible()) {
                logger.objectInfo(String.format("Locator '%s' is visible after '%.1f' seconds", Locators.getLocatorName(), i / 10.0));
                return true;
            }
            page.waitForTimeout(100);
        }
        logger.objectInfo(String.format("Locator '%s' is NOT visible after '%.1f' seconds", Locators.getLocatorName(), timeout));
        return false;
    }

    public boolean checkLocatorIsVisible(String locator, int timeout) {
        return checkLocatorIsVisible(locator, (double) timeout);
    }

    public boolean checkLocatorIsVisible(String locator) {
        return checkLocatorIsVisible(locator, defaultTimeout);
    }

    public void verifyLocatorNotVisible(String locator, int timeout) {
        long start = System.currentTimeMillis();
        try {
            page.waitForSelector(locator, new Page.WaitForSelectorOptions().setTimeout(timeout * 1000).setState(WaitForSelectorState.HIDDEN));
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.passed(String.format("Locator '%s' is NOT visible after '%.1f' seconds", Locators.getLocatorName(), duration)); 
        } catch (Exception ex) {
            double duration = (System.currentTimeMillis() - start) / 1000.0;
            logger.failed(String.format("Locator '%s' is still visible after '%.1f' seconds", Locators.getLocatorName(), duration));
        }
    }

    public void verifyLocatorNotVisible(String locator) {
        verifyLocatorNotVisible(locator, defaultTimeout);
    }

    public boolean checkLocatorNotVisible(String locator, int timeout) {
        for (int second = 1; second < timeout*10; second ++) {
            if (!page.locator(locator).isVisible()) {
                logger.objectInfo(String.format("Locator '%s' is NOT visible after '%.1f' seconds", Locators.getLocatorName(), second/10.0));
                return true;
            }
            page.waitForTimeout(100);
        }
        logger.objectInfo(String.format("String '%s' - [%s] is Visible after '%.1f' seconds", Locators.getLocatorName(), locator, timeout));
        return false;
    }

    public String getDateValue(String locator) {
        try {
            String dateValue = page.locator(locator).evaluate("el => el.value").toString();
            logger.objectInfo(String.format("Date value of Locator '%s' is: '%s'", Locators.getLocatorName(), dateValue));
            return dateValue;
        } catch (Exception ex) {
            logger.failed(String.format("Get Date value of Locator '%s' - [%s] failed. Details: %s",
                    Locators.getLocatorName(), locator, ex));
        }
        return null;
    }

    public String getText(String locator) {
        try {
            String text = page.locator(locator).textContent().trim();
            logger.objectInfo(String.format("Text of Locator '%s' is: '%s'", Locators.getLocatorName(), text));
            return text;
        } catch (Exception ex) {
            logger.failed(String.format("Get Text of Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
            return null;
        }
    }

    public List<String> getAllTextContents(String locator) {
        try {
            waitForLocatorPresent(locator);
            List<String> allTextContents = page.locator(locator).allTextContents();
            allTextContents = allTextContents.stream().map(String::trim).collect(Collectors.toList());
            logger.objectInfo(String.format("All Text Contents of Locator '%s' is: '[%s]'", Locators.getLocatorName(),
                    String.join(", ", allTextContents)));
            return allTextContents;
        } catch (Exception ex) {
            logger.failed(String.format("Get All Text Contents of Locator '%s' - [%s] failed. Details: %s",
                    Locators.getLocatorName(), locator, ex));
            return null;
        }
    }

    public int getNumberOfLocator(String locator) {
        try {
            int numberOfLocator = page.locator(locator).count();
            logger.objectInfo(String.format("Number of Locator '%s' is: '[%s]'", Locators.getLocatorName(), numberOfLocator));
            return numberOfLocator;
        } catch (Exception ex) {
            logger.failed(String.format("Get Number of Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
            return 0;
        }
    }

    public Map<String, Double> getSizeOfLocator(String locator) {
        try {
            Map<String, Double> locatorSize = new HashMap<>();
            BoundingBox box = page.locator(locator).boundingBox();
            if (box != null) {
                locatorSize.put("width", box.width);
                locatorSize.put("height", box.height);
            } else {
                locatorSize.put("width", Double.parseDouble(getCssValue(locator, "width").replace("px", "")));
                locatorSize.put("height", Double.parseDouble(getCssValue(locator, "height").replace("px", "")));
            }
            logger.objectInfo(String.format("Size of Locator '%s' is: ' [W=%s x H=%s]'",
            Locators.getLocatorName(), locatorSize.get("width"), locatorSize.get("height")));
            return locatorSize;
        } catch (Exception ex) {
            logger.failed(String.format("Get Size of Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
            return null; 
        }
    }

    public String getCssValue(String locator, String property) {
        try {
            String cssValue = page.locator(locator).evaluate("el => window.getComputedStyle(el).getPropertyValue('" + property + "')").toString();
            logger.objectInfo(String.format("CSS value of property '%s' for Locator '%s' is: '%s'", property, Locators.getLocatorName(), cssValue));
            return cssValue;
        } catch (Exception ex) {
            logger.failed(String.format("Get CSS value of property '%s' for Locator '%s' - [%s] failed. Details: %s", property, Locators.getLocatorName(), locator, ex));
            return null;
        }
    }

    public String getAttribute(String locator, String cssName) {
        try {
            String attributeValue = page.locator(locator).getAttribute(cssName);
            logger.objectInfo(String.format("Attribute '%s' for Locator '%s' is: '%s'", cssName, Locators.getLocatorName(), attributeValue));
            return attributeValue;
        } catch (Exception ex) {
            logger.failed(String.format("Get Attribute '%s' for Locator '%s' - [%s] failed. Details: %s", cssName, Locators.getLocatorName(), locator, ex));
            return null;
        }
    }

    public String getInputValue(String locator) {
        try {
            String inputValue = (String)page.locator(locator).inputValue();
            logger.objectInfo(String.format("Input value of Locator '%s' is: '%s'", Locators.getLocatorName(), inputValue));
            return inputValue;
        } catch (Exception ex) {
            logger.failed(String.format("Get Input value of Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
            return null;
        }
    }

    public String getProperty(String locator, String propertyName) {
        try {
            String propertyValue = page.locator(locator).evaluate(String.format("element => element.%s", propertyName)).toString();
            logger.objectInfo(String.format("Property '%s' of Locator '%s' is: '%s'", propertyName, Locators.getLocatorName(), propertyValue));
            return propertyValue;
        } catch (Exception ex) {
            logger.failed(String.format("Get Property '%s' of Locator '%s' - [%s] failed. Details: %s", propertyName, Locators.getLocatorName(), locator, ex));
            return null;
        }
    }

    public boolean clickIfExists(String locator, int timeout) {
        try {
            if (checkLocatorIsVisible(locator, timeout)) {
                page.locator(locator).click();
                logger.objectInfo(String.format("Clicked on Locator '%s' as it exists.", Locators.getLocatorName()));
                return true;
            } 
            return false;
        } catch (Exception ex) {
            logger.failed(String.format("Click action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
            return false;
        }
    }

    public void enhancedClick(String locator) {
        try {
            page.locator(locator).evaluate("el => el.click()");
            logger.objectInfo(String.format("Enhanced click on Locator '%s' was successful.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Enhanced click action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void click(String locator) {
        try {
            page.locator(locator).click();
            logger.objectInfo(String.format("Clicked on Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Click action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void click(String locator, int timeout) {
        try {
            Locator.ClickOptions options = new Locator.ClickOptions().setTimeout(timeout * 1000);
            page.locator(locator).first().click(options);
            logger.objectInfo(String.format("Clicked on Locator '%s' successfully within timeout of %d seconds.", Locators.getLocatorName(), timeout));
        } catch (Exception ex) {
            logger.failed(String.format("Click action on Locator '%s' - [%s] failed within timeout of %d seconds. Details: %s", Locators.getLocatorName(), locator, timeout, ex));
        }
    }

    public void doubleClick(String locator) {
        try {
            page.locator(locator).dblclick();
            logger.objectInfo(String.format("Double clicked on Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Double click action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void rightClick(String locator) {
        try {
            page.locator(locator).click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
            logger.objectInfo(String.format("Right clicked on Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Right click action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void clearText(String locator) {
        try {
            page.locator(locator).fill("");
            String value = page.locator(locator).inputValue();
            if (value != null && !value.isEmpty()) {
                page.locator(locator).click();
                String os = System.getProperty("os.name").toLowerCase();
                String selectAll = os.contains("mac") ? "Meta+A" : "Control+A";
                page.keyboard().press(selectAll);
                page.keyboard().press("Backspace");
            }
            logger.objectInfo(String.format("Cleared text of Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Clear text action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void setText(String locator, String text) {
        try {
            page.locator(locator).fill(text);
            logger.objectInfo(String.format("Set text of Locator '%s' to '%s' successfully.", Locators.getLocatorName(), text));
        } catch (Exception ex) {
            logger.failed(String.format("Set text action on Locator '%s' - [%s] to '%s' failed. Details: %s", Locators.getLocatorName(), locator, text, ex));
        }
    }

    public void setMaskedText(String locator, String text) {
        try {
            page.locator(locator).fill(text);
            logger.objectInfo(String.format("Set mask text of Locator '%s' to '%s' successfully.", Locators.getLocatorName(), "*".repeat(text.length())));
        } catch (Exception ex) {
            logger.failed(String.format("Set mask text action on Locator '%s' - [%s] to '%s' failed. Details: %s", Locators.getLocatorName(), locator, "*".repeat(text.length()), ex));
        }
    }

    public void setTextOneByOne(String locator, String text, boolean clearText) {
        try {
            if (clearText) {
                clearText(locator);
            }
            int clickTime = 0;
            for (char c : text.toCharArray()) {
                page.locator(locator).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
                page.locator(locator).type(String.valueOf(c));
                if (clickTime < 3) {
                    page.locator(locator).evaluate("el => el.click()");
                    clickTime++;
                }
            }
            logger.objectPassed(String.format("Set text of Locator '%s' to '%s' one by one successfully.", Locators.getLocatorName(), text));
        } catch (Exception ex) {
            logger.failed(String.format("Set text one by one action on Locator '%s' - [%s] to '%s' failed. Details: %s", Locators.getLocatorName(), locator, text, ex));
        }
    }

    public void setTextOneByOne(String locator, String text) {
        setTextOneByOne(locator, text, true);
    }

    public void setDateTime(String locator, String dateTime) {
        try {
            page.locator(locator).click();
            page.keyboard().press("Control+A");
            page.locator(locator).press(dateTime);
            logger.objectPassed(String.format("Set date/time of Locator '%s' to '%s' successfully.", Locators.getLocatorName(), dateTime));
        } catch (Exception ex) {
            logger.failed(String.format("Set date/time action on Locator '%s' - [%s] to '%s' failed. Details: %s", Locators.getLocatorName(), locator, dateTime, ex));
        }
    }

    public void selectOptionByValue(String locator, String value) {
        try {
            page.locator(locator).selectOption(new SelectOption().setValue(value));
            logger.objectPassed(String.format("Selected option by value '%s' for Locator '%s' successfully.", value, Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Select option by value action on Locator '%s' - [%s] with value '%s' failed. Details: %s", Locators.getLocatorName(), locator, value, ex));
        }
    }

    public void selectOptionByLabel(String locator, String label) {
        try {
            page.locator(locator).selectOption(new SelectOption().setLabel(label));
            logger.objectPassed(String.format("Selected option by label '%s' for Locator '%s' successfully.", label, Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Select option by label action on Locator '%s' - [%s] with label '%s' failed. Details: %s", Locators.getLocatorName(), locator, label, ex));
        }
    }

    public void selectOptionByIndex(String locator, int index) {
        try {
            page.locator(locator).selectOption(new SelectOption().setIndex(index));
            logger.objectPassed(String.format("Selected option by index '%d' for Locator '%s' successfully.", index, Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Select option by index action on Locator '%s' - [%s] with index '%d' failed. Details: %s", Locators.getLocatorName(), locator, index, ex));
        }
    }

    public void hover(String locator) {
        try {
            page.locator(locator).hover(new Locator.HoverOptions().setForce(true));
            logger.objectPassed(String.format("Hovered over Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Hover action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void moveMouseToElement(String locator) {
        try {
            BoundingBox box = page.locator(locator).boundingBox();
            if (box != null) {
                page.mouse().move(box.x + box.width / 2, box.y + box.height / 2);
                logger.objectPassed(String.format("Moved mouse to Locator '%s' successfully.", Locators.getLocatorName()));
            }
        } catch (Exception ex) {
            logger.failed(String.format("Move mouse to Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void scrollToLocator(String locator, boolean ignoreException) {
        try {
            page.locator(locator).scrollIntoViewIfNeeded();
            logger.objectPassed(String.format("Scrolled to Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            if (ignoreException) {
                logger.objectInfo(String.format("Failed to scroll to Locator '%s' - [%s]. Details: %s", Locators.getLocatorName(), locator, ex));
            } else {
                logger.failed(String.format("Scroll to Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
            }
        }
    }

    public void scrollToLocator(String locator) {
        scrollToLocator(locator, false);
    }

    public void scrollToView(String locator) {
        try {
            page.locator(locator).evaluate("el => el.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'center' })");
            logger.objectPassed(String.format("Scrolled to view Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Scroll to view Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void pressKey(String locator, String key) {
        try {
            page.locator(locator).press(key);
            logger.objectPassed(String.format("Pressed key '%s' on Locator '%s' successfully.", key, Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Press key '%s' on Locator '%s' - [%s] failed. Details: %s", key, Locators.getLocatorName(), locator, ex));
        }
    }

    public void check(String locator) {
        try {
            page.locator(locator).check();
            logger.objectPassed(String.format("Checked Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Check action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void uncheck(String locator) {
        try {
            page.locator(locator).uncheck();
            logger.objectPassed(String.format("Unchecked Locator '%s' successfully.", Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Uncheck action on Locator '%s' - [%s] failed. Details: %s", Locators.getLocatorName(), locator, ex));
        }
    }

    public void switchToFrame(String frameLocator) {
        try {
            Frame frame = page.frame(frameLocator);
            if (frame != null) {
                this.page = frame.page();
                logger.objectPassed(String.format("Switched to frame '%s' successfully.", frameLocator));
            } else {
                logger.failed(String.format("Frame '%s' not found.", frameLocator));
            }
        } catch (Exception ex) {
            logger.failed(String.format("Switch to frame '%s' failed. Details: %s", frameLocator, ex));
        }
    }

    public Object executeJavaScript(String script) {
        return page.evaluate(script);
    }

    public void acceptAlert() {
        try {
            page.onDialog(dialog -> {
                dialog.accept();
                logger.objectPassed("Alert accepted successfully.");
            });
        } catch (Exception ex) {
            logger.failed(String.format("Accept alert failed. Details: %s", ex));
        }
    }

    public void dismissAlert() {
        try {
            page.onDialog(dialog -> {
                dialog.dismiss();
                logger.objectPassed("Alert dismissed successfully.");
            });
        } catch (Exception ex) {
            logger.failed(String.format("Dismiss alert failed. Details: %s", ex));
        }
    }

    public String getTextAlert() {
        try {
            final String[] alertText = new String[1];
            page.onDialog(dialog -> {
                alertText[0] = dialog.message();
                dialog.dismiss();
                logger.objectPassed("Alert text retrieved successfully.");
            });
            return alertText[0];
        } catch (Exception ex) {
            logger.failed(String.format("Get alert text failed. Details: %s", ex));
            return null;
        }
    }

    public void setTextAlert(String text) {
        try {
            page.onDialog(dialog -> {
                dialog.accept(text);
                logger.objectPassed("Alert text set successfully.");
            });
        } catch (Exception ex) {
            logger.failed(String.format("Set alert text failed. Details: %s", ex));
        }
    }

    public void uploadFile(String locator, String filePath) {
        try {
            page.locator(locator).setInputFiles(Paths.get(filePath));
            logger.objectPassed(String.format("Uploaded file '%s' to Locator '%s' successfully.", filePath, Locators.getLocatorName()));
        } catch (Exception ex) {
            logger.failed(String.format("Upload file '%s' to Locator '%s' - [%s] failed. Details: %s", filePath, Locators.getLocatorName(), locator, ex));
        }
    }

    public void verifyLocatorEnabledorNot(String locator, String locatorName, int timeout, boolean verifyEnabled) {
        try {
            for (int waitTime = 1; waitTime <= timeout * 10; waitTime++) {
                boolean isChecked = page.locator(locator).isEnabled();
                if (isChecked && verifyEnabled || !isChecked && !verifyEnabled) {
                    if (verifyEnabled) {
                        logger.passed(String.format("Locator '%s - %s' is enabled after '%s' second(s)", locatorName,
                                Locators.getLocatorName(), waitTime / 10.0));
                    } else {
                        logger.passed(String.format("Locator '%s - %s' is disabled after '%s' second(s)", locatorName,
                                Locators.getLocatorName(), waitTime / 10.0));
                    }
                    return;
                }
                page.waitForTimeout(100);
            }

            if (verifyEnabled) {
                logger.failed(String.format("Locator '%s - %s' is disabled after '%s' second(s)", locatorName,
                        Locators.getLocatorName(), timeout));
            } else {
                logger.failed(String.format("Locator '%s - %s' is enabled after '%s' second(s)", locatorName,
                        Locators.getLocatorName(), timeout));
            }
        } catch (Exception ex) {
            logger.error(String.format("Verify Locator '%s - %s' - [%s] is enabled or not failed. Details : %s",
                    locatorName, Locators.getLocatorName(), locator, ex));
        }
    }

    public void verifyLocatorEnabled(String locator, String locatorName, int timeout) {
        verifyLocatorEnabledorNot(locator, locatorName, timeout, true);
    }

    public void verifyLocatorEnabled(String locator, String locatorName) {
        verifyLocatorEnabledorNot(locator, locatorName, defaultTimeout, true);
    }

    public void verifyLocatorDisabled(String locator, String locatorName, int timeout) {
        verifyLocatorEnabledorNot(locator, locatorName, timeout, false);
    }

    public void waitForPageLoad() {
        waitForPageLoad(30);
    }

    public void scrollElementToPosition(String locator, int x, int y) {
        try {
            page.locator(locator).evaluate(String.format("el => el.scrollLeft += %d; el.scrollTop += %d", x, y));
            logger.objectPassed(String.format("Scrolled Locator '%s' to position (%d, %d) successfully.", Locators.getLocatorName(), x, y));
        } catch (Exception ex) {
            logger.failed(String.format("Scroll Locator '%s' to position (%d, %d) failed. Details: %s", Locators.getLocatorName(), x, y, ex));
        }
    }

    public void refresh() {
        try {
            page.reload();
            logger.objectPassed("Page refreshed successfully.");
        } catch (Exception ex) {
            logger.failed(String.format("Page refresh failed. Details: %s", ex));
        }
    }

    public Path downloadFile(String locator, String downloadPath) {
        try {
            Download download = page.waitForDownload(() -> {if (locator != null) page.locator(locator).click();});
            Path downloadFilePath = Paths.get(downloadPath, download.suggestedFilename());
            download.saveAs(downloadFilePath);
            logger.objectPassed(String.format("File downloaded successfully to '%s'.", downloadFilePath.toString()));
            return downloadFilePath;
        } catch (Exception ex) {
            logger.failed(String.format("File download failed. Details: %s", ex));
            return null;
        }
    }

    





    

}