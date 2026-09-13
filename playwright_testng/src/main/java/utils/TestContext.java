package utils;

public class TestContext {
    private static final ThreadLocal<String> threadLocalTestCaseName = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalTestClassName = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalTestSuiteName = new ThreadLocal<>();
    private static final ThreadLocal<Integer> threadLocalNumberOfScreenshots = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> threadLocalNumberOfTestCases = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<String> threadLocalREQ = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> threadLocalNeedReplaceSuiteName = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<String> threadLocalCurrentPageName = new ThreadLocal<>();

    /**
     * Convenience method to set all test context values at once
     */
    public static void set(String suite, String className, String caseName, String req) {
        setTestSuiteName(suite);
        setTestClassName(className);
        setTestCaseName(caseName);
        setREQ(req);
    }

    public static String getTestCaseName() {
        return threadLocalTestCaseName.get();
    }

    public static void setTestCaseName(String testCaseName) {
        threadLocalTestCaseName.set(testCaseName);
    }

    public static String getTestClassName() {
        return threadLocalTestClassName.get();
    }

    public static void setTestClassName(String testClassName) {
        threadLocalTestClassName.set(testClassName);
    }

    public static String getTestSuiteName() {
        return threadLocalTestSuiteName.get();
    }

    public static void setTestSuiteName(String testSuiteName) {
        threadLocalTestSuiteName.set(testSuiteName);
    }

    public static Boolean getNeedReplaceSuiteName() {
        return threadLocalNeedReplaceSuiteName.get();
    }

    public static void setNeedReplaceSuiteName(Boolean needReplace) {
        threadLocalNeedReplaceSuiteName.set(needReplace);
    }

    public static String getCurrentPageName() {
        return threadLocalCurrentPageName.get();
    }

    public static void setCurrentPageName(String pageName) {
        threadLocalCurrentPageName.set(pageName);
    }

    public static int getNumberOfScreenshots() {
        int currentNumber = threadLocalNumberOfScreenshots.get();
        threadLocalNumberOfScreenshots.set(currentNumber + 1);
        return currentNumber;
    }

    public static void setNumberOfScreenshots(int number) {
        threadLocalNumberOfScreenshots.set(number);
    }

    public static int getNumberOfTestCases() {
        return threadLocalNumberOfTestCases.get();
    }

    public static void setNumberOfTestCases(int number) {
        threadLocalNumberOfTestCases.set(number);
    }

    public static String getREQ() {
        return threadLocalREQ.get();
    }

    public static void setREQ(String req) {
        threadLocalREQ.set(req);
    }

    public static void clear() {
        threadLocalTestCaseName.remove();
        threadLocalTestClassName.remove();
        threadLocalTestSuiteName.remove();
        threadLocalNumberOfScreenshots.remove();
        threadLocalNumberOfTestCases.remove();
        threadLocalREQ.remove();
        threadLocalNeedReplaceSuiteName.remove();
        threadLocalCurrentPageName.remove();
    }
}
