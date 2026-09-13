package config;

/** Compatibility facade for framework components that read environment/global values. */
public final class ConfigReader {
    private ConfigReader() {
    }

    public static String getEnvironmentProperty(String key) {
        return TestConfig.getProperty(key);
    }

    public static String getGlobalVariable(String key) {
        return TestConfig.getProperty(key);
    }
}
