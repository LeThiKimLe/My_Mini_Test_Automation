package utils;

import config.TestConfig;
import io.qameta.allure.Allure;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.stream.Stream;

public final class Utils {
    private Utils() {
    }

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
}
