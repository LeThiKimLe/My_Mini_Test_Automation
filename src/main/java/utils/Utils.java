package utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static Path getScreenshotPath() {
        String filename = "screenshot-" + System.currentTimeMillis() + ".png";
        return Paths.get("target", "screenshots", filename);
    }

    /**
     * Prepare environment required for allure result generation.
     * Kept minimal so it's safe to call from the test runner listener.
     */
    public static void setupEnvironment() {
        try {
            Path traceDir = Paths.get("target", "trace");
            Path screenshotsDir = Paths.get("target", "screenshots");
            Path allureSingleDir = Paths.get("target", "allure-single");
            java.nio.file.Files.createDirectories(traceDir);
            java.nio.file.Files.createDirectories(screenshotsDir);
            java.nio.file.Files.createDirectories(allureSingleDir);
        } catch (Exception e) {
            System.err.println("[Utils] Failed to prepare environment: " + e.getMessage());
        }
    }

    /**
     * Delegate to AllureReportHelper to generate/move single-file Allure report.
     * Kept public so tests/listeners can call it via Utils.generateAllureResult().
     */
    public static void generateAllureResult() {
        try {
            utils.AllureReportHelper.generateSingleReports();
        } catch (Exception e) {
            System.err.println("[Utils] Error generating Allure result: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
