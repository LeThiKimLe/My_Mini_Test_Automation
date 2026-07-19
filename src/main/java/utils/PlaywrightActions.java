package utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class PlaywrightActions {

    private Page page = null;
    private Page previousPage = null;
    private BrowserContext context = null;
    private Map<Integer, Integer> numberOfPages = new HashMap<>();


    public PlarwrightActions() {

    }

    public PlaywrightActions(Page page) {
        this.page = page;
    }

    public PlaywrightActions(BrowserContext context, Page page) {
        this.context = context;
        this.page = page;
    }

    // Keyword used to switch between multi browser
    public void switchToSpecificContextWithPageIndex(BrowserContext context, int pageIndex) {
        this.page = context.pages().get(pageIndex);
        this.context = context;
        this.page = context.pages().get(pageIndex);
        this.page.bringToFront();
        this.page.bringToFront();
        this.page.bringToFront();
        
    }

    public void waitForVisible(String selector) {
        page.locator(selector).waitFor(
            new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
        );
    }
    
}
