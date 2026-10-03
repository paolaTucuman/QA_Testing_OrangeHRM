package page_locators;

import org.openqa.selenium.By;

public final class CommonLocators {

    public static final By PIM_MENU = By.cssSelector(
            "a[href*='/pim/viewPimModule']"
    );
    public static final By USER_DROPDOWN = By.cssSelector(".oxd-userdropdown-tab");
    public static final By LOGOUT_LINK = By.cssSelector(
            "a.oxd-userdropdown-link[href*='/auth/logout']"
    );
    public static final By LOADING_SPINNER = By.cssSelector(".oxd-loading-spinner");
    public static final By LOADING_INDICATORS = By.cssSelector(
            ".oxd-loading-spinner, .oxd-form-loader"
    );

    private CommonLocators() {
    }
}
