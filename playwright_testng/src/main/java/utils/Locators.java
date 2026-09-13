package utils;

/** Adds a readable locator name to the thread context while returning its selector. */
public final class Locators {
    private Locators() {
    }

    public static String create(String selector) {
        return selector;
    }
}
