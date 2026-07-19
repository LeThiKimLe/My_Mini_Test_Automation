package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllureReportHelper {

    /**
     * Finds all test results in target/allure-results, copies each test's JSON and attachments
     * to a temporary folder, runs Allure CLI to generate a single HTML report, and saves it
     * under target/allure-single/YYYY-MM-DD/test_case_name.html
     */
    public static void generateSingleReports() {
        File resultsDir = new File("target/allure-results");
        if (!resultsDir.exists() || !resultsDir.isDirectory()) {
            System.out.println("[AllureReportHelper] allure-results directory does not exist. Skipping single report generation.");
            return;
        }

        File[] resultFiles = resultsDir.listFiles((dir, name) -> name.endsWith("-result.json"));
        if (resultFiles == null || resultFiles.length == 0) {
            System.out.println("[AllureReportHelper] No test result files found in allure-results. Skipping single report generation.");
            return;
        }

        String allureCmd = getAllureCommand();
        if (allureCmd == null) {
            System.err.println("[AllureReportHelper] Allure CLI could not be found. Cannot generate single file reports.");
            return;
        }

        String todayStr = DateUtils.today();
        File finalOutputDir = new File("target/allure-single/" + todayStr);
        if (!finalOutputDir.exists()) {
            finalOutputDir.mkdirs();
        }

        System.out.println("[AllureReportHelper] Found " + resultFiles.length + " test result(s). Starting single report generation...");

        for (File resultFile : resultFiles) {
            try {
                String jsonContent = new String(Files.readAllBytes(resultFile.toPath()));
                String testName = getTestNameFromJson(jsonContent);
                if (testName == null || testName.isEmpty()) {
                    testName = resultFile.getName().replace("-result.json", "");
                }

                // Create temp dirs for isolating this test result
                File tempResultsDir = new File("target/allure-temp/results-" + testName);
                File tempOutputDir = new File("target/allure-temp/output-" + testName);
                
                // Cleanup any stale temp directories first
                deleteDirectory(tempResultsDir);
                deleteDirectory(tempOutputDir);
                
                tempResultsDir.mkdirs();
                tempOutputDir.mkdirs();

                // Copy the -result.json file
                Files.copy(resultFile.toPath(), new File(tempResultsDir, resultFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Find and copy attachments
                copyAttachments(jsonContent, resultsDir, tempResultsDir);

                // Run Allure CLI
                boolean success = runAllureGenerate(allureCmd, tempResultsDir, tempOutputDir);
                if (success) {
                    File generatedIndex = new File(tempOutputDir, "index.html");
                    if (generatedIndex.exists()) {
                        File targetReportFile = new File(finalOutputDir, testName + ".html");
                        Files.copy(generatedIndex.toPath(), targetReportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("[AllureReportHelper] Generated single report: " + targetReportFile.getAbsolutePath());
                    }
                }

                // Clean up temp directories
                deleteDirectory(tempResultsDir);
                deleteDirectory(tempOutputDir);

            } catch (Exception e) {
                System.err.println("[AllureReportHelper] Error generating report for " + resultFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Clean up base temp directory
        File baseTempDir = new File("target/allure-temp");
        if (baseTempDir.exists()) {
            baseTempDir.delete();
        }
    }

    /**
     * Parses the test name from the JSON. Tries "description" first (from annotations),
     * and falls back to "name" (method name).
     */
    private static String getTestNameFromJson(String jsonContent) {
        // 1. Try to find the description (holds custom annotation description)
        Pattern descPattern = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"");
        Matcher descMatcher = descPattern.matcher(jsonContent);
        if (descMatcher.find()) {
            String desc = descMatcher.group(1);
            return sanitizeFilename(desc);
        }

        // 2. Fallback to name (method name)
        Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher nameMatcher = namePattern.matcher(jsonContent);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            return sanitizeFilename(name);
        }
        return null;
    }

    private static String sanitizeFilename(String input) {
        // Replace invalid filename chars and spaces with underscores
        String sanitized = input.replaceAll("[\\\\/:*?\"<>|\\s]", "_");
        // Collapse multiple underscores
        sanitized = sanitized.replaceAll("_+", "_");
        // Strip leading/trailing underscores
        if (sanitized.startsWith("_")) {
            sanitized = sanitized.substring(1);
        }
        if (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }

    /**
     * Finds all "source" references in the JSON and copies those files to the temp dir.
     * This ensures screenshots, logs, and traces are bundled into the report.
     */
    private static void copyAttachments(String jsonContent, File sourceDir, File destDir) {
        Pattern pattern = Pattern.compile("\"source\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonContent);
        while (matcher.find()) {
            String attachmentName = matcher.group(1);
            File sourceAttachment = new File(sourceDir, attachmentName);
            if (sourceAttachment.exists()) {
                try {
                    Files.copy(sourceAttachment.toPath(), new File(destDir, attachmentName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("[AllureReportHelper] Failed to copy attachment: " + attachmentName + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * Executes the allure CLI command to generate a single-file report
     */
    private static boolean runAllureGenerate(String allureCmd, File resultsDir, File outputDir) {
        try {
            List<String> command = new ArrayList<>();
            command.add(allureCmd);
            command.add("generate");
            command.add(resultsDir.getAbsolutePath());
            command.add("--single-file");
            command.add("--clean");
            command.add("-o");
            command.add(outputDir.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("[AllureReportHelper] Failed to execute allure command: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resolves the Allure binary from the project's .allure directory or system PATH
     */
    private static String getAllureCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        String projectDir = System.getProperty("user.dir");

        File allureDir = new File(projectDir, ".allure");
        if (allureDir.exists() && allureDir.isDirectory()) {
            File[] files = allureDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && file.getName().startsWith("allure-")) {
                        String binaryName = os.contains("win") ? "allure.bat" : "allure";
                        File bin = new File(file, "bin/" + binaryName);
                        if (bin.exists()) {
                            return bin.getAbsolutePath();
                        }
                    }
                }
            }
        }

        // Fallback to system PATH
        return os.contains("win") ? "allure.bat" : "allure";
    }

    private static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
