package base;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;

import io.qameta.allure.Allure;

/**
 * Extension used by tests to take a screenshot and stop tracing when a test fails.
 *
 * We implement {@link AfterTestExecutionCallback} instead of {@link org.junit.jupiter.api.extension.TestWatcher}
 * because the latter is invoked **after** the {@code @AfterEach} methods in the test class,
 * which leads to a closed browser context/page and a {@code TargetClosedError} when the
 * watcher tries to capture a screenshot. The callback below runs before the test's
 * {@code @AfterEach}, ensuring the page is still open.
 */
public class PlaywrightTestWatcher implements AfterTestExecutionCallback {

    private static Page page;
    private static BrowserContext context;

    public static void setPage(Page p) {
        page = p;
    }

    public static Page getPage() {
        return page;
    }
    
    public static void setContext(BrowserContext ctx) {
        context = ctx;
    }
    
    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        // if the test threw an exception, it is considered failed
        if (extensionContext.getExecutionException().isPresent()) {
            try {
                String testName = extensionContext.getDisplayName();
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get("target/screenshots/" + testName + ".png"))
                        .setFullPage(true));
                Allure.addAttachment(
                        "Failure screenshot",
                        "image/png",
                        new ByteArrayInputStream(screenshot),
                        ".png");
                System.out.println("Screenshot taken for failed test: " + testName);
                Path tracePath = Paths.get("target/trace/" + testName + ".zip");
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(tracePath));
                try (InputStream trace = Files.newInputStream(tracePath)) {
                    Allure.addAttachment("Playwright trace", "application/zip", trace, ".zip");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
