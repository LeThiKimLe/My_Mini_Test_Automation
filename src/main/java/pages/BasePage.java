package pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Page;

import io.qameta.allure.Step;

public abstract class BasePage {
    
    protected Page page;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected BasePage(Page page) {
        this.page = page;
    }

    @Step("Open page: [{path}]")
    public void open(String path) {
        page.navigate(path);
    }
}
