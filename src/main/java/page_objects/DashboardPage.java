package page_objects;

import org.openqa.selenium.WebDriver;
import page_base.BasePage;
import page_locators.CommonLocators;
import page_locators.DashboardLocators;

public class DashboardPage extends BasePage {

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDashboardDisplayed() {
        return isDisplayed(DashboardLocators.DASHBOARD_TITLE);
    }

    public boolean isAuthenticatedMenuDisplayed() {
        return isDisplayed(CommonLocators.PIM_MENU);
    }

    public boolean isDashboardDisplayedNow() {
        return isDisplayedNow(DashboardLocators.DASHBOARD_TITLE);
    }

    public boolean prepareDashboardEvidence() {
        waitUntilVisible(DashboardLocators.DASHBOARD_TITLE);
        waitUntilVisible(CommonLocators.PIM_MENU);
        return waitUntilVisualStateIsReady();
    }
}
