package base;

import java.io.ByteArrayInputStream;

import com.microsoft.playwright.Page;

import io.qameta.allure.Allure;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.StepResult;

public class AllureStepScreenshotListener implements StepLifecycleListener {

    @Override
    public void beforeStepStop(StepResult result) {
        Page page = PlaywrightTestWatcher.getPage();
        if (page == null || page.isClosed()) {
            return;
        }

        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Allure.addAttachment("Step screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
        } catch (RuntimeException ignored) {
            // Do not fail the test because reporting could not capture the page.
        }
    }
}
