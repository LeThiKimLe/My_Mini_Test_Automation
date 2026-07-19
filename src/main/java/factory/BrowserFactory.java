package factory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import config.TestConfig;

public class BrowserFactory {
    private static Playwright playwright;
    private static Browser browser;
    public static Browser getBrowser() {
        if (browser == null) {
            createBrowserInstance();
        }
        return browser;
    }
    private static void createBrowserInstance() {
        playwright = Playwright.create();
        
        String browserName = TestConfig.getBrowser();
        if (browserName == null || browserName.trim().isEmpty()) {
            browserName = "chromium";
        } else {
            browserName = browserName.toLowerCase().trim();
        }

        boolean headless = TestConfig.getHeadless();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless);

        System.out.println("Initializing Playwright...");
        System.out.println("Launching browser: " + browserName + " (Headless: " + headless + ")");

        switch (browserName) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            case "chromium":
            default:
                browser = playwright.chromium().launch(launchOptions);
                break;
        }
    }
    public static void close() {
        if (browser != null) {
            System.out.println("Closing browser...");
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            System.out.println("Closing Playwright...");
            playwright.close();
            playwright = null;
        }
    }
}
