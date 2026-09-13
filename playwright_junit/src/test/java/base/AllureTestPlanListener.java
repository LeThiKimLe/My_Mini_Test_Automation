package base;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Listener that runs when the whole test-plan finishes. It prepares environment
 * and triggers Allure single-file generation using Utils helper.
 * Registered via ServiceLoader so JUnit Platform / Surefire picks it up automatically.
 */
public class AllureTestPlanListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            // prepare environment and generate final Allure single-file report
            utils.Utils.setupEnvironment();
            utils.Utils.generateAllureResult();
        } catch (Exception e) {
            System.err.println("[AllureTestPlanListener] Error running generateAllureResult: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
