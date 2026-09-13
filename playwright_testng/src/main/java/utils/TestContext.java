package utils;

/** Per-test metadata used for diagnostics and report naming. */
public final class TestContext {
    private static final ThreadLocal<String> testCaseName = new ThreadLocal<>();
    private static final ThreadLocal<String> testClassName = new ThreadLocal<>();
    private static final ThreadLocal<String> testSuiteName = new ThreadLocal<>();
    private static final ThreadLocal<String> requirement = new ThreadLocal<>();

    private TestContext() {
    }

    public static void set(String suite, String className, String caseName, String req) {
        testSuiteName.set(suite);
        testClassName.set(className);
        testCaseName.set(caseName);
        requirement.set(req);
    }

    public static String getTestCaseName() { return testCaseName.get(); }
    public static String getTestClassName() { return testClassName.get(); }
    public static String getTestSuiteName() { return testSuiteName.get(); }
    public static String getRequirement() { return requirement.get(); }

    public static void clear() {
        testCaseName.remove();
        testClassName.remove();
        testSuiteName.remove();
        requirement.remove();
    }
}
