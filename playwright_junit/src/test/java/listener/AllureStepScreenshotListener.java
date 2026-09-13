package listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Collections;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.Allure;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.listener.FixtureLifecycleListener;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import utils.Loggers;
import utils.Utils;

public class AllureStepScreenshotListener implements StepLifecycleListener, TestLifecycleListener,
        FixtureLifecycleListener, BeforeEachCallback, AfterEachCallback {

    private static final ThreadLocal<String> THREAD_LOCAL_TC_NAME = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isCapturingScreenshot = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Stack<String>> stepStack = ThreadLocal.withInitial(Stack::new);
    private static final Set<String> AUTO_TAKE_SCREENSHOT_PACKAGES =
            new HashSet<>(Arrays.asList("pages", "flow"));
    private static final ThreadLocal<Set<String>> TEST_TAGS = ThreadLocal.withInitial(HashSet::new);

    @Override
    public void beforeTestSchedule(TestResult result) {
        THREAD_LOCAL_TC_NAME.set(result.getName());
        addStartTimeToDescription(result);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        TEST_TAGS.get().addAll(context.getTags());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        
    }

    @Override
    public void afterTestStart(TestResult result) {
        TEST_TAGS.get().forEach(tag -> result.getLabels().add(
                new io.qameta.allure.model.Label().setName("tag").setValue(tag)));
        TEST_TAGS.remove();
    }

    @Override
    public void beforeFixtureStop(FixtureResult result) {
        if (!"testSetup".equals(result.getName()) || !result.getSteps().isEmpty()) {
            return;
        }
        addSetupSteps(result);
    }

    private void addSetupSteps(FixtureResult result) {
        String browser = TestConfig.getProperty("browser");
        if (browser == null || browser.trim().isEmpty()) {
            browser = "chromium";
        }
        result.getSteps().add(new StepResult()
                .setName("Create new [ " + browser + " ]")
                .setStatus(Status.PASSED));
        result.getSteps().add(new StepResult()
                .setName("Create new Page [ Default ]")
                .setStatus(Status.PASSED));
        result.getSteps().add(new StepResult()
                .setName("Resolve test metadata")
                .setStatus(Status.PASSED));
    }

    @Override
    public void beforeTestStop(TestResult result) {
        if (result.getStatus() == Status.FAILED){
            Loggers.stopStep(true);
        } else {
            Loggers.stopStep();
        }
    }

    @Override
    public void afterStepStart(StepResult result) {
        String stepName = result.getName();
        boolean isStepMethod = !isAllureStepMethod(result);
        String normalizedStepName = stepName == null ? "" : stepName.toLowerCase();
        if (isStepMethod
                && !normalizedStepName.equals("screenshot")
                && !normalizedStepName.equals("screen record")) {
            if (!stepStack.get().isEmpty()) {
                String previousStepName = stepStack.get().peek();
                Loggers.addStepLogs(String.format("[EXECUTE] - %s", stepName), previousStepName);
            }
            stepStack.get().push(stepName);
            Loggers.threadLocalStepLogs.get().put(stepName, new StringBuilder());
            Loggers.threadLocalCurrentSteps.set(stepName);
        }
    }

    @Override
    public void beforeStepStop(StepResult result) {
        if (isCapturingScreenshot.get()) {
            return;
        }
        String stepName = result.getName();
        boolean isStepMethod = !isAllureStepMethod(result);
        if (isStepMethod && ! stepName.toLowerCase().equals("screenshot") && !stepName.toLowerCase().equals("screen record")) {
            boolean isFromPageobject = Arrays.stream(Thread.currentThread().getStackTrace())
                                        .anyMatch( stackTraceElement ->
                                        AUTO_TAKE_SCREENSHOT_PACKAGES.stream().anyMatch(stackTraceElement.getClassName() :: startsWith));
            if (isFromPageobject) {
                isCapturingScreenshot.set(true);
                try {
                    if (Boolean.parseBoolean(TestConfig.getProperty("takeScreenshotByPlaywright", "true"))
                    && BaseTest.getPlaywrightActionsObject() != null) {
                        BaseTest.getPlaywrightActionsObject().takeFullPageScreenshot();
                    } else {
                        Utils.takeWindowsScreenshot();
                    }
                }
                finally {
                    isCapturingScreenshot.set(false);
                }
            }
            if (!stepStack.get().isEmpty()) {
                stepName = stepStack.get().peek();
                String logContent = Loggers.threadLocalStepLogs.get().get(stepName).toString();
                if (!logContent.isEmpty() && logContent.split("\\n").length > 1) {
                    Allure.addAttachment("Step Execution Logs - " + stepName, "text/plain", logContent);
                }
                stepStack.get().pop();
                if (!stepStack.get().isEmpty()) {
                    Loggers.threadLocalCurrentSteps.set(stepStack.get().peek());
                } else {
                    Loggers.threadLocalCurrentSteps.remove();
                }
            }
        }
    }

    public static synchronized String getTestCaseName() {
        return THREAD_LOCAL_TC_NAME.get();
    }

    private boolean isAllureStepMethod(StepResult result) {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getMethodName().equals( "step")) {
            return true;
            }
        }
        return false;
    }

    private void addStartTimeToDescription(TestResult result) {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String originalDescription = result.getDescription() != null ? result.getDescription() : "";
        String updatedDescription = String.format("[Test started at: %s]%s", startTime, (originalDescription. isEmpty() ? "" : String.format("\n\n%s", originalDescription)));
        result.setDescription(updatedDescription);
    }

}
