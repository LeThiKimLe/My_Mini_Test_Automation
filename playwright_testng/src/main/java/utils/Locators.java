package utils;

import java.util.Properties;

/** Adds a readable locator name to the thread context while returning its selector. */
public final class Locators {
    private static final ThreadLocal<Properties> threadLocalProperties = ThreadLocal.withInitial(Properties::new);

    private Locators() {
    }

    public static String create(String selector) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame frame = walker.walk(frames -> frames.skip(1).findFirst().get());
        threadLocalProperties.get().setProperty("LOCATOR",
                String.format("%s/%s", frame.getDeclaringClass().getSimpleName(), frame.getMethodName()));
        return selector;
    }

    public static String create(String selector, Object... args) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        StackWalker.StackFrame frame = walker.walk(frames -> frames.skip(1).findFirst().get());
        threadLocalProperties.get().setProperty("LOCATOR",
                String.format("%s/%s", frame.getDeclaringClass().getSimpleName(), frame.getMethodName()));
        return String.format(selector, args);
    }

    public static String getLocatorName() {
        return threadLocalProperties.get().getProperty("LOCATOR", "No Locator Value");
    }

    public static void clearProperties() {
        threadLocalProperties.get().clear();
    }
}
