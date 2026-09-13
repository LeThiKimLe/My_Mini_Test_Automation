package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Resolves environment properties first, then global defaults, then JVM overrides. */
public final class TestConfig {
    private static final Properties ENVIRONMENT = new Properties();
    private static final Properties GLOBAL = new Properties();

    static {
        String environment = System.getProperty("env", "test");
        load("config-" + environment + ".properties", ENVIRONMENT, true);
        load("config-global.properties", GLOBAL, true);
    }

    private TestConfig() {
    }

    private static void load(String resource, Properties target, boolean required) {
        try (InputStream stream = TestConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                if (required) {
                    throw new IllegalStateException("Missing configuration resource: " + resource);
                }
                return;
            }
            target.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read configuration resource: " + resource, exception);
        }
    }

    public static String getProperty(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        String environmentValue = ENVIRONMENT.getProperty(key);
        return environmentValue != null ? environmentValue : GLOBAL.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    public static String getBaseUrl() {
        String value = getProperty("base.url");
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Property 'base.url' is required");
        }
        return value;
    }

    public static String getBrowser() {
        return getProperty("browser", "chromium");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "true"));
    }

    public static int getTimeoutSeconds() {
        return Integer.parseInt(getProperty("timeout", "30"));
    }

    public static String getSystemEnvironment() {
        return System.getProperty("env");
    }
}
