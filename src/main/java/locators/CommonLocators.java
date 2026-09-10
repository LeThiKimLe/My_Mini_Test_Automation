package locators;

import utils.Locators;

public class CommonLocators {

    public static String specificButton(String buttonText) {
        return Locators.create("//button[text()='%s']", buttonText);
    }
    
}
