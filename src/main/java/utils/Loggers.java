package utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;

public class Loggers {
    public static ThreadLocal<Map<String, StringBuilder>> threadLocalStepLogs = ThreadLocal.withInitial(HashMap::new);
    public static ThreadLocal<String> threadLocalCurrentSteps = ThreadLocal.withInitial(String::new);
    private static final Logger logger = LoggerFactory.getLogger(Loggers.class);
    private static Deque<String> testStepStack = new LinkedList<>();

    public void step(String stepTitle) {
        stopStep();
        String stepId = UUID.randomUUID().toString();
        Allure.getLifecycle().startStep(stepId, new StepResult().setName(stepTitle));
        Allure.getLifecycle().updateStep(stepId, stepResult -> {
            stepResult.setStatus(Status.FAILED);
        });
        testStepStack.push(stepId);
    }

    public static void stopStep(boolean testMethodFailed) {
        if (!testStepStack.isEmpty()) {
            String lastStepId = testStepStack.pop();
            if (testMethodFailed) {
                Allure.getLifecycle().updateStep(lastStepId, stepResult -> {
                    stepResult.setStatus(Status.FAILED);
                });
            } else {
                Allure.getLifecycle().updateStep(lastStepId, stepResult -> {
                    stepResult.setStatus(Status.PASSED);
                });
            }
            Allure.getLifecycle().stopStep(lastStepId);
        }
    }

    public static void stopStep() {
        stopStep(false);
    }

    public static void addStepLog(String msg) {
        if (!threadLocalCurrentSteps.get().isEmpty()) {
            threadLocalStepLogs.get().get(threadLocalCurrentSteps.get()).append(msg).append("\n");
        }
    }

    public static void addStepLogs(String msg) {
        if (!threadLocalCurrentSteps.get().isEmpty()) {
            addStepLogs(msg, threadLocalCurrentSteps.get());
        }
    }

    public static void addStepLogs(String msg, String stepName) {
        if (threadLocalStepLogs.get().containsKey(stepName)) {
            threadLocalStepLogs.get().get(stepName).append(msg).append("\n");
        }
    }

    private static String formatMessage(String msg, String msgType) {
        return String.format("[%s %s] - %s", DateUtils.getCurrentTimeWithSpecificFormat("yyyy-MM-dd HH:mm:ss"), msgType, msg);
    }

    public void documentation(String documentation) {
        stopStep();
        Allure.getLifecycle().startStep(UUID.randomUUID().toString(), new StepResult().setName(documentation));
        Allure.getLifecycle().stopStep();
    }

    public void trace(String msg) {
        String formattedMsg = formatMessage(msg, "TRACE");
        addStepLogs(formattedMsg);
        logger.trace(formattedMsg);
        Allure.addAttachment("Trace Log", "text/plain", formattedMsg);
    }

    public void debug(String msg) {
        String formattedMsg = formatMessage(msg, "DEBUG");
        addStepLogs(formattedMsg);
        logger.debug(formattedMsg);
        Allure.addAttachment("Debug Log", "text/plain", formattedMsg);
    }

    public void info(String msg) {
        String formattedMsg = formatMessage(msg, "INFO");
        addStepLogs(formattedMsg);
        logger.info(formattedMsg);
        Allure.addAttachment("Info Log", "text/plain", formattedMsg);
    }

    public void warn(String msg) {
        String formattedMsg = formatMessage(msg, "WARN");
        addStepLogs(formattedMsg);
        logger.warn(formattedMsg);
        Allure.addAttachment("Warn Log", "text/plain", formattedMsg);
    }

    public void error(String msg) {
        String formattedMsg = formatMessage(msg, "ERROR");
        addStepLogs(formattedMsg);
        logger.error(formattedMsg);
        Allure.addAttachment("Error Log", "text/plain", formattedMsg);
    }

    public void passed(String msg) {
        String formattedMsg = formatMessage(msg, "PASSED");
        addStepLogs(formattedMsg);
        logger.info(formattedMsg);
        Allure.step(formattedMsg);
    }

    public void failed(String msg) {
        String formattedMsg = formatMessage(msg, "FAILED");
        addStepLogs(formattedMsg);
        logger.error(formattedMsg);
        Allure.step(formattedMsg);
    }

    public void objectPassed(String msg) {
        String formattedMsg = formatMessage("Object passed: " + msg, "PASSED");
        addStepLogs(formattedMsg);
        logger.info(formattedMsg);
    }

    public void objectInfo(String msg) {
        String formattedMsg = formatMessage("Object info: " + msg, "INFO");
        addStepLogs(formattedMsg);
        logger.info(formattedMsg);
    }

    public void info(String msg, boolean disableAllure) {
        String formattedMsg = formatMessage(msg, "INFO");
        logger.info(formattedMsg);
    }

    public void error(String msg, boolean disableAllure) {
        String formattedMsg = formatMessage(msg, "ERROR");
        logger.error(formattedMsg);
    }

}
