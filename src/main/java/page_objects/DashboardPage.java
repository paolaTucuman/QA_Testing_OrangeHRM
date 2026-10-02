package page_objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import page_base.BasePage;

public class DashboardPage extends BasePage {

    @FindBy(xpath = "//h6[normalize-space()='Dashboard']")
    private WebElement dashboardTitle;

    @FindBy(xpath = "//span[normalize-space()='PIM']")
    private WebElement pimMenu;

    public DashboardPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public boolean isDashboardDisplayed() {
        return isDisplayed(dashboardTitle);
    }

    public boolean isAuthenticatedMenuDisplayed() {
        return isDisplayed(pimMenu);
    }

    public boolean isDashboardDisplayedNow() {
        return isDisplayedNow(dashboardTitle);
    }

    public boolean prepareDashboardEvidence() {
        waitUntilVisible(dashboardTitle);
        waitUntilVisible(pimMenu);
        return waitUntilVisualStateIsReady();
    }
}
