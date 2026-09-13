package listener;

import base.BaseTest;
import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.TestContext;
import utils.Utils;

/** TestNG equivalent of the reference execution/listener report hooks. */
public class FrameworkTestListener implements IExecutionListener, ITestListener {
    private static final ThreadLocal<String> LAST_SUITE_NAME = new ThreadLocal<>();

    @Override
    public void onExecutionStart() {
        Utils.cleanAllureResults();
        Utils.cleanDirectory(Utils.allureDirectory());
        Utils.prepareTestDirectory();
    }

    @Override
    public void onExecutionFinish() {
        Utils.setupEnvironment();
        if (Boolean.parseBoolean(System.getProperty("generateAllureReport", "false"))) {
            String suiteName = LAST_SUITE_NAME.get();
            if (suiteName == null || suiteName.trim().isEmpty()) {
                Utils.generateAllureResult();
            } else {
                Utils.generateAllureReport(suiteName);
            }
        }
        LAST_SUITE_NAME.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            ((BaseTest) instance).captureFailureArtifacts();
        }
    }

    @Override public void onTestStart(ITestResult result) {
        LAST_SUITE_NAME.set(TestContext.getTestSuiteName());
    }
    @Override public void onStart(ITestContext context) { }
    @Override public void onFinish(ITestContext context) { }
    @Override public void onTestSuccess(ITestResult result) { }
    @Override public void onTestSkipped(ITestResult result) { }
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
    @Override public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }
}

