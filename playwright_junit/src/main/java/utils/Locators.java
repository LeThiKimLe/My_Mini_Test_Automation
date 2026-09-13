package utils;

import java.util.Properties;

public class Locators {
    private static final ThreadLocal<Properties> threadLocalProperties = ThreadLocal.withInitial(Properties::new);
    
    public static String create(String locator) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame frame = walker.walk(frames -> frames.skip(1).findFirst().get());
        threadLocalProperties.get().setProperty("LOCATOR", String.format("%s/%s", frame.getDeclaringClass().getSimpleName(), frame.getMethodName()));
        return locator;
    }

    public static String create(String locator, Object... args) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame frame = walker.walk(frames -> frames.skip(1).findFirst().get());
        threadLocalProperties.get().setProperty("LOCATOR", String.format("%s/%s", frame.getDeclaringClass().getSimpleName(), frame.getMethodName()));
        return String.format(locator, args);
    }

    public static String getLocatorName() {
        return threadLocalProperties.get().getProperty("LOCATOR", "No Locator Value");
    }

    public static void clearProperties() {
        threadLocalProperties.get().clear();
    }
}
