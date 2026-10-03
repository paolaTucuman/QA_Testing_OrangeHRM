package page_objects;

import org.openqa.selenium.WebDriver;
import page_base.BasePage;
import page_locators.CommonLocators;
import page_locators.DashboardLocators;
import page_locators.LoginLocators;

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

    public void logout() {
        waitUntilClickable(CommonLocators.USER_DROPDOWN).click();
        waitUntilClickable(CommonLocators.LOGOUT_LINK).click();
        waitUntilVisible(LoginLocators.USERNAME);
    }

    public boolean isAuthenticatedNavigationDisplayedNow() {
        return isDisplayedNow(CommonLocators.PIM_MENU);
    }

    public boolean waitForSessionOrLoginState() {
        wait.until(currentDriver -> isDisplayedNow(LoginLocators.USERNAME)
                || isDisplayedNow(CommonLocators.PIM_MENU));
        return waitUntilVisualStateIsReady();
    }

    public boolean prepareDashboardEvidence() {
        waitUntilVisible(DashboardLocators.DASHBOARD_TITLE);
        waitUntilVisible(CommonLocators.PIM_MENU);
        return waitUntilVisualStateIsReady();
    }
}
