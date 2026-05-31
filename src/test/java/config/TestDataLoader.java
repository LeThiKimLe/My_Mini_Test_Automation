package config;

import java.io.InputStream;
import java.util.Properties;

public class TestDataLoader {

    public static Properties load(String path) {
        Properties props = new Properties();
        try (InputStream is =
            TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return props;
    }
}