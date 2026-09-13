package pages;

import io.qameta.allure.Step;
import utils.Loggers;
import utils.PlaywrightActions;

public abstract class BasePage {
    protected final PlaywrightActions webUI;
    protected final Loggers logger = new Loggers();

    protected BasePage(PlaywrightActions webUI) {
        this.webUI = webUI;
    }

    @Step("Open page [{url}]")
    public void open(String url) {
        webUI.navigateToUrl(url);
    }
}
