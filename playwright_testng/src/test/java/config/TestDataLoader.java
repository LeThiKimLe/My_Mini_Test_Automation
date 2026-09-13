package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestDataLoader {
    private TestDataLoader() {
    }

    public static Properties load(String resourcePath) {
        String path = System.getProperty("testData", resourcePath);
        try (InputStream stream = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing test-data resource: " + path);
            }
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load test data: " + path, exception);
        }
    }
}
