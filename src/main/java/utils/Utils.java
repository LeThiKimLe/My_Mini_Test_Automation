package main.java.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import base.BaseTest;
import config.TestConfig;

public class Utils {

    public static Path getScreenshotPath() {
        String testCaseName = BaseTest.getTestCaseName();
        String testClassName = BaseTest.getTestClassName();
        int numberOfScreenshots = BaseTest.getNumberOfScreenshots();
        if (numberOfScreenshots < 10) {
            numberOfScreenshotsString = String.format("0%s", numberOfScreenshots);
        }
        String screenshotName = String.format("Screenshot_%s-%s_%s_SS%s.png", BaseTest.getREQ(), BaseTest.getNumberOfTestCases(), testCaseName, numberOfScreenshotsString);
        return Paths.get(TestConfig.getProperty("resultsDirectory"), testClassName, "screeshots", testCaseName, screenshotName);
    }

}
