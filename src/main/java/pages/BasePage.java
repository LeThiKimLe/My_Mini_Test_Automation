package pages;

import io.qameta.allure.Step;
import utils.Loggers;
import utils.PlaywrightActions;


public abstract class BasePage {
    
    protected final PlaywrightActions WebUI;
    protected final Loggers logger;

    protected BasePage(PlaywrightActions WebUI) {
        this.WebUI = WebUI;
        this.logger = new Loggers();
    }

    @Step("Open page: [{path}]")
    public void open(String path) {
        WebUI.navigateToUrl(path);
        logger.info(String.format("Navigated to page: %s", path));
    }
}
