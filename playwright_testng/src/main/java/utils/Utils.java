package utils;

import config.TestConfig;
import io.qameta.allure.Allure;
import io.qameta.allure.model.StepResult;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Utils {
    private Utils() {
    }

    private static final Loggers logger = new Loggers();

    /** Ensures the Allure results folder is cleaned at most once per JVM session. */
    private static final AtomicBoolean allureResultsCleaned = new AtomicBoolean(false);

    public static Path resultDirectory() {
        return Paths.get(TestConfig.getProperty("resultsDirectory", "target/test-results"));
    }

    public static Path allureDirectory() {
        return Paths.get(TestConfig.getProperty("allureResultsDirectory", "target/allure-results"));
    }

    public static void cleanDirectory(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Unable to clean " + path, exception);
                }
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to clean " + directory, exception);
        }
    }

    public static void prepareTestDirectory() {
        try {
            Files.createDirectories(resultDirectory());
            Files.createDirectories(allureDirectory());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create result directories", exception);
        }
    }

    public static void attachFile(String name, String type, Path file) {
        if (file != null && Files.exists(file)
                && io.qameta.allure.Allure.getLifecycle().getCurrentTestCase().isPresent()) {
            try (InputStream input = Files.newInputStream(file)) {
                Allure.addAttachment(name, type, input, extension(type));
            } catch (IOException exception) {
                new Loggers().warn("Unable to attach " + file + ": " + exception.getMessage());
            }
        }
    }

    private static String extension(String contentType) {
        if (contentType.contains("png")) return "png";
        if (contentType.contains("zip")) return "zip";
        return "txt";
    }

    public static void writeEnvironment() {
        prepareTestDirectory();
        Properties properties = new Properties();
        properties.setProperty("Browser", TestConfig.getBrowser());
        properties.setProperty("Environment", System.getProperty("env", "test"));
        properties.setProperty("Headless", String.valueOf(TestConfig.isHeadless()));
        try (java.io.OutputStream output = Files.newOutputStream(allureDirectory().resolve("environment.properties"))) {
            properties.store(output, "Playwright TestNG environment");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Allure environment", exception);
        }
    }

    /**
     * Cleans the Allure results directory exactly once per JVM session.
     * Safe to call from multiple locations; subsequent calls are no-ops.
     */
    public static void cleanAllureResults() {
        if (allureResultsCleaned.compareAndSet(false, true)) {
            AllureReportHelper.cleanAllureResults();
        }
    }

    public static Path getScreenshotPath() {
        String testCaseName = TestContext.getTestCaseName();
        String testClassName = TestContext.getTestClassName();
        int numberOfScreenshots = TestContext.getNumberOfScreenshots();
        String numberOfScreenshotsString = String.valueOf(numberOfScreenshots);
        if (numberOfScreenshots < 10) {
            numberOfScreenshotsString = String.format("0%s", numberOfScreenshots);
        }
        String screenshotName = String.format("Screenshot_%s-%s_%s_SS%s.png", TestContext.getREQ(), TestContext.getNumberOfTestCases(), testCaseName, numberOfScreenshotsString);
        return Paths.get(TestConfig.getProperty("resultsDirectory"), testClassName, "screeshots", testCaseName, screenshotName);
    }

    public static void takeWindowsScreenshot() {
        Allure.getLifecycle().startStep(UUID.randomUUID().toString(), new StepResult().setName("Windows Screenshot"));
        try {
            Path screenshotPath = Utils.getScreenshotPath();
            String imageName = screenshotPath.toString().substring(screenshotPath.toString().lastIndexOf('\\') + 1);
            Utils.createDirectoryIfNotExists(screenshotPath.getParent());
            BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            ImageIO.write(screenshot, "png", screenshotPath.toFile());
            Allure.addAttachment(imageName, new FileInputStream(screenshotPath.toFile()));
            logger.info(String.format("A windows screenshot was taken - [%s]", imageName));
        } catch (Exception e) {
            logger.failed("Take windows screenshot faild: " + e.getMessage());
        } finally {
            Allure.getLifecycle().stopStep();
        }
    }

    public static void createDirectoryIfNotExists(Path directoryPath) {
        if (!directoryPath.toFile().exists()) {
            directoryPath.toFile().mkdirs();
        }
    }

    public static void cleanAndPrepareResultDirectories(String testCaseName, String testClassName) {
        List<Path> resultsDirectories = new ArrayList<>();
        try {
            resultsDirectories.add(Paths.get(TestConfig.getProperty("resultsDirectory"), testClassName,
                    "screeshots", testCaseName));
            resultsDirectories
                    .add(Paths.get(TestConfig.getProperty("resultsDirectory"), testClassName, "videos"));
            resultsDirectories.add(Paths.get(TestConfig.getProperty("resultsDirectory"), testClassName,
                    "traces", testCaseName));
            for (Path directory : resultsDirectories) {
                if (directory.toFile().exists()) {
                    FileUtils.deleteDirectory(directory.toFile());
                }
                directory.toFile().mkdirs();
            }
            logger.info(String.format(
                    "Cleaned and prepared result directories for test case [%s] in class [%s]",
                    testCaseName, testClassName));
        } catch (Exception e) {
            logger.error(String.format(
                    "Error occurred while cleaning and preparing result directories for test case [%s] in class [%s]: %s",
                    testCaseName, testClassName, e.getMessage()));
        }
    }

    public static void setupEnvironment() {
        try {
            File file = new File(String.format("%s/environment.properties", TestConfig.getProperty("allureResultsDirectory", "target/allure-results")));
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(String.format("OS = %s\n", System.getProperty("os.name")));
            writer.write(String.format("Java_Version = %s\n", System.getProperty("java.version")));
            writer.write(String.format("Browser-Version = %s\n", String.format("%s - %s", TestConfig.getProperty("browser"), System.getProperty("browserVersion"))));
            writer.write(String.format("Headless = %s\n", TestConfig.getProperty("headless")));
            writer.write(String.format("Environment = %s\n", TestConfig.getSystemEnvironment()));
            writer.close();
        } catch (IOException e) {
            logger.error(String.format("Setup environment fail. Details %s", e.getMessage()), true);
        }
    }

    public static String getCurrentTimeWithSpecificFormat(String format) {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return currentTime.format(formatter);
    }

    /**
     * Convenience wrapper called by test listeners.
     * Generates the Allure report after tests finish.
     */
    public static void generateAllureResult() {
        String suiteName = TestContext.getTestSuiteName();
        if (suiteName == null || suiteName.trim().isEmpty()) {
            suiteName = "test-suite";
        }
        generateAllureReport(suiteName);
    }

    public static void generateAllureReport(String suiteName) {
        try {
            if (suiteName == null || suiteName.trim().isEmpty()) {
                suiteName = "test-suite";
            }
            String allureResultsPath = createAllureResultsFolderForSpecificSuite(suiteName);
            if (allureResultsPath == null) {
                return;
            }
            String absoluteAllureResultsPath = Paths.get(allureResultsPath).toAbsolutePath().normalize().toString();
            Path reportDirectory = Paths.get(TestConfig.getProperty("resultsDirectory", "results"), "reports");
            createDirectoryIfNotExist(reportDirectory);
            Path currentReportDirectory = Paths.get(TestConfig.getProperty("resultsDirectory", "results"), "reports", getCurrentTimeWithSpecificFormat("yyyy-MM-dd"));
            createDirectoryIfNotExist(currentReportDirectory);
            String reportName = String.format("AllureReport_%s_%s.html", suiteName, getCurrentTimeWithSpecificFormat("yy-MM-dd-HHmmss"));
            String projectDir = System.getProperty("user.dir");
            File projectDirectory = new File(projectDir);
            String allureReportPath = Paths.get(System.getProperty("user.dir"), "target", String.format("allure-report_%s",
                                        suiteName.replaceAll("\\s+", "-"))).toAbsolutePath().normalize().toString();
            String os = System.getProperty("os.name").toLowerCase();
            String mavenCommand = os.contains("win") ? "mvn.cmd" : "mvn";
            ProcessBuilder processBuilder = new ProcessBuilder(
                    mavenCommand,
                    "io.qameta.allure:allure-maven:2.15.0:report",
                    "-Dallure.results.directory=" + absoluteAllureResultsPath,
                    "-Dallure.report.directory=" + allureReportPath);
            processBuilder.directory(projectDirectory);
            processBuilder.redirectErrorStream(true);
            File reportLog = new File(reportDirectory.toFile(), "allure-report-generation.log");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(reportLog));
            Process process = processBuilder.start();
            long timeoutSeconds = Long.parseLong(System.getProperty("allure.report.timeout.seconds", "120"));
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                logger.error("Timed out while generating Allure report after "
                        + timeoutSeconds + " seconds.", true);
                return;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                logger.info("Allure report was generated successfully.", true);
                File sourceFile = new File(String.format("%s/index.html", allureReportPath));
                File destinationFile = new File(String.format("%s/%s", currentReportDirectory, reportName));
                File destinationDir = destinationFile.getParentFile();
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }
                if (sourceFile.exists()) {
                    org.apache.commons.io.FileUtils.copyFile(sourceFile, destinationFile);
                    logger.info(String.format("Allure report was copied to %s/%s", currentReportDirectory, reportName), true);
                    deleteFolder(Paths.get(absoluteAllureResultsPath));
                    deleteFolder(Paths.get(allureReportPath));
                } else {
                    logger.error("Allure report file not found.", true);
                }
            } else {
                logger.error("Failed to generate Allure report. See log: "
                        + reportLog.getAbsolutePath(), true);
            }
        } catch (IOException e) {
            logger.error(e.toString(), true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(e.toString(), true);
        }
    }

    public static String createAllureResultsFolderForSpecificSuite(String suiteName) {
        if (suiteName == null || suiteName.trim().isEmpty()) {
            suiteName = "test-suite";
        }
        String baseDir = TestConfig.getProperty("allureResultsBaseDir",
                TestConfig.getProperty("allureResultsDirectory", "target/allure-results"));
        String allureResultFolder = String.format("%s_%s", Paths.get(baseDir).getFileName().toString(),
                suiteName.replaceAll("\\s+", "-"));
        String defaultAllureResultsPath = baseDir;
        String allureResultsPath = String.format("target/%s", allureResultFolder);
        copyFolder(defaultAllureResultsPath, allureResultsPath);
        File folder = new File(allureResultsPath);
        if (!folder.exists() || !folder.isDirectory()) {
            logger.error("The Allure Results directory does not exist or is invalid: " + allureResultsPath, true);
            return null;
        }
        rewriteAllureSuiteNames(folder, suiteName);
        return allureResultsPath;
    }

    private static void rewriteAllureSuiteNames(File folder, String suiteName) {
        ObjectMapper objectMapper = new ObjectMapper();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            logger.error("No JSON files found in the Allure Results directory: " + folder, true);
            return;
        }
        for (File file : files) {
            try {
                JsonNode root = objectMapper.readTree(file);
                if (!(root instanceof ObjectNode)) {
                    continue;
                }
                ObjectNode rootNode = (ObjectNode) root;
                JsonNode labelsNode = rootNode.get("labels");
                boolean modified = false;
                if (labelsNode != null && labelsNode.isArray()) {
                    for (JsonNode labelNode : labelsNode) {
                        if (!(labelNode instanceof ObjectNode) || labelNode.get("name") == null
                                || labelNode.get("value") == null) {
                            continue;
                        }
                        String labelName = labelNode.get("name").asText();
                        if ("parentSuite".equals(labelName) || "suite".equals(labelName)) {
                            String labelValue = labelNode.get("value").asText();
                            if (!suiteName.equals(labelValue)
                                    && (labelValue.contains("Surefire") || labelValue.contains("TestSuite")
                                    || labelValue.contains("playwright-testng"))) {
                                ((ObjectNode) labelNode).put("value", suiteName);
                                modified = true;
                            }
                        }
                    }
                }
                if (rootNode.has("name")) {
                    String originalName = rootNode.get("name").asText();
                    if (originalName.contains("Surefire") || originalName.contains("TestSuite")) {
                        rootNode.put("name", originalName
                                .replace("Surefire", suiteName)
                                .replace("TestSuite", suiteName));
                        modified = true;
                    }
                }
                if (modified) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
                }
            } catch (IOException exception) {
                logger.error(String.format("Error processing file '%s'. Details: %s",
                        file.getName(), exception), true);
            }
        }
    }

    public static void copyFolder(String sourceDir, String targetDir) {
        File sourceFolder = new File(sourceDir);
        File targetFolder = new File(targetDir);
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            logger.error("Source directory does not exist or is invalid: " + sourceDir, true);
            return;
        }
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        try {
            Files.walk(Paths.get(sourceDir)).forEach(sourcePath -> {
                Path targetPath = Paths.get(targetDir, sourcePath.toString().substring(sourceDir.length()));
                try {
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    logger.error("Failed to copy file: " + e.getMessage(), true);
                }
            });
            logger.info(String.format("Successfully copied folder from '%s' to '%s'", sourceDir, targetDir), true);
        } catch (IOException e) {
            logger.error("Error during directory copy operation : " + e, true);
        }
    }

    public static void createDirectoryIfNotExist(Path path) {
        try {
            if (Files.notExists(path))
                Files.createDirectories(path);
        } catch (IOException e) {
            logger.error(String.format("Failed to create directory: %s", e.getMessage()), true);
        }
    }

    public static void deleteFolder(Path path) {
        if (path == null) return;
        try {
            File file = path.toFile();
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
        } catch (Exception e) {
            logger.error("Failed to delete folder: " + path + " - " + e.getMessage(), true);
        }
    }

    public static Path getTracePath(String contextName) {
        String testCaseName = TestContext.getTestCaseName() != null ? TestContext.getTestCaseName() : "default";
        String testClassName = TestContext.getTestClassName() != null ? TestContext.getTestClassName() : "default";
        Path dir = Paths.get(TestConfig.getProperty("resultsDirectory", "results"), testClassName, "traces", testCaseName);
        createDirectoryIfNotExist(dir);
        return dir.resolve(String.format("trace_%s_%s.zip", contextName, testCaseName));
    }

    public static void moveRecordFileToCorrectDirectory(Path sourceRecordPath, String contextName) {
        if (sourceRecordPath == null || !Files.exists(sourceRecordPath)) {
            return;
        }
        try {
            String testCaseName = TestContext.getTestCaseName() != null ? TestContext.getTestCaseName() : "default";
            String testClassName = TestContext.getTestClassName() != null ? TestContext.getTestClassName() : "default";
            Path videoDir = Paths.get(TestConfig.getProperty("resultsDirectory", "results"), testClassName, "videos");
            createDirectoryIfNotExist(videoDir);
            Path targetFile = videoDir.resolve(String.format("video_%s_%s.webm", contextName, testCaseName));
            Files.move(sourceRecordPath, targetFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info(String.format("Moved video record to: %s", targetFile.toString()), true);
        } catch (Exception e) {
            logger.error("Failed to move record file: " + e.getMessage(), true);
        }
    }
}
