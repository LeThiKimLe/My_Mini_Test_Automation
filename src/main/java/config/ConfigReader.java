package config;

public class ConfigReader {

    public static String getEnvironmentProperty(String key) {
        return TestConfig.getProperty(key, "");
    }

    public static String getGlobalVariable(String key) {
        return TestConfig.getProperty(key, "");
    }
}