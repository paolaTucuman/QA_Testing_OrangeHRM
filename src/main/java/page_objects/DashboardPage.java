package page_objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import page_base.BasePage;
import page_locators.CommonLocators;
import page_locators.DashboardLocators;
import page_locators.LoginLocators;

public class DashboardPage extends BasePage {

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDashboardDisplayed() {
        try {
            return wait.until(currentDriver ->
                    currentDriver.getCurrentUrl().contains("/dashboard/index")
                            && isDisplayedNow(CommonLocators.USER_DROPDOWN));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isAuthenticatedMenuDisplayed() {
        return isDisplayed(CommonLocators.PIM_MENU);
    }

    public boolean isDashboardDisplayedNow() {
        return driver.getCurrentUrl().contains("/dashboard/index")
                && isDisplayedNow(CommonLocators.USER_DROPDOWN);
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
        try {
            wait.until(currentDriver -> isCompleteLoginFormDisplayedNow()
                    || isDisplayedNow(CommonLocators.PIM_MENU));
            return waitUntilVisualStateIsReady();
        } catch (TimeoutException e) {
            System.out.println(
                    "Advertencia: no se completo el estado de Login o sesion autenticada"
            );
            return false;
        }
    }

    private boolean isCompleteLoginFormDisplayedNow() {
        return isDisplayedNow(LoginLocators.USERNAME)
                && isDisplayedNow(LoginLocators.PASSWORD)
                && isDisplayedNow(LoginLocators.LOGIN_BUTTON);
    }

    public boolean prepareDashboardEvidence() {
        waitUntilVisible(DashboardLocators.MODULE_HEADER);
        waitUntilVisible(CommonLocators.USER_DROPDOWN);
        waitUntilVisible(CommonLocators.PIM_MENU);
        return waitUntilVisualStateIsReady();
    }
}
