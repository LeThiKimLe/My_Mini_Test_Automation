package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import config.TestConfig;

public class AllureReportHelper {

    
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

        if (ensureAllureCliAvailable()) {
            return getAllureCommand();
        }

        // Fallback to system PATH
        return os.contains("win") ? "allure.bat" : "allure";
    }

    private static boolean ensureAllureCliAvailable() {
        String os = System.getProperty("os.name").toLowerCase();
        String projectDir = System.getProperty("user.dir");
        File allureDir = new File(projectDir, ".allure");
        String version = "2.30.0";
        String binaryName = os.contains("win") ? "allure.bat" : "allure";
        File existingBinary = new File(allureDir, "allure-" + version + "/bin/" + binaryName);
        if (existingBinary.exists()) {
            return true;
        }

        try {
            Files.createDirectories(allureDir.toPath());
            File archive = new File(allureDir, "allure-" + version + ".zip");
            String archiveUrl = "https://github.com/allure-framework/allure2/releases/download/" + version + "/allure-" + version + ".zip";
            try (InputStream in = new URL(archiveUrl).openStream(); FileOutputStream out = new FileOutputStream(archive)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archive))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path target = Paths.get(allureDir.getAbsolutePath(), entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = zis.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }

            return existingBinary.exists();
        } catch (Exception e) {
            System.err.println("[AllureReportHelper] Unable to download Allure CLI automatically: " + e.getMessage());
            return false;
        }
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

    public static void cleanAllureResults() {
        File resultsDir = new File(TestConfig.getProperty("allureResultsDirectory", "target/allure-results"));
        if (resultsDir.exists() && resultsDir.isDirectory()) {
            deleteDirectory(resultsDir);
            System.out.println("[AllureReportHelper] Cleaned allure-results directory.");
        }
    }
}
