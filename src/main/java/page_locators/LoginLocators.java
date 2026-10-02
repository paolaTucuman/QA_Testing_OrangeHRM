package page_locators;

import org.openqa.selenium.By;

public final class LoginLocators {

    public static final By USERNAME = By.name("username");
    public static final By PASSWORD = By.name("password");
    public static final By LOGIN_BUTTON = By.xpath("//button[@type='submit']");
    public static final By ERROR_MESSAGE = By.xpath(
            "//p[contains(@class, 'oxd-alert-content-text')]"
    );

    private LoginLocators() {
    }
}
