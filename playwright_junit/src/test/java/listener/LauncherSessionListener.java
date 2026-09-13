package listener;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import utils.Utils;

/**
 * JUnit Platform {@link TestExecutionListener} that hooks into the test plan lifecycle:
 *
 * <ul>
 *   <li>{@link #testPlanExecutionStarted(TestPlan)} — cleans the Allure results directory
 *       exactly once before any tests run.</li>
 *   <li>{@link #testPlanExecutionFinished(TestPlan)} — writes the {@code environment.properties}
 *       file to the Allure results folder and generates the Allure report after all tests finish.</li>
 * </ul>
 *
 * <p>Registered automatically via the ServiceLoader mechanism:
 * {@code META-INF/services/org.junit.platform.launcher.TestExecutionListener}
 */
public class LauncherSessionListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        Utils.cleanAllureResults();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        Utils.setupEnvironment();
        Utils.generateAllureResult();
    }
}
