package config;

import java.util.Properties;

public class TestConfig {
    private static final Properties envProperties = new Properties();
    private static final Properties globalProperties = new Properties();

    static {
        String env = System.getProperty("env", "test");
        String fileEnvName = "config-" + env + ".properties";
        String fileGlobalName = "config-global.properties";
        try {
            envProperties.load(TestConfig.class.getClassLoader().getResourceAsStream(fileEnvName));
            globalProperties.load(TestConfig.class.getClassLoader().getResourceAsStream(fileGlobalName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getBaseUrl() {
        return getProperty("base.url");
    }

    public static String getBrowser() {
        return getProperty("browser");
    }

    public static String getTimeout() {
        return getProperty("timeout");
    }

    public static boolean getHeadless() {
        return Boolean.parseBoolean(getProperty("headless"));
    }

    public static String getProperty(String key) {
        String value = envProperties.getProperty(key);
        if (value == null) {
            value = globalProperties.getProperty(key);
        }
        return value;
    }

}