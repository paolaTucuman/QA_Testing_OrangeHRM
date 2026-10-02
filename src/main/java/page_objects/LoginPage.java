package page_objects;

import page_base.BasePage;
import page_locators.LoginLocators;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        clearAndType(LoginLocators.USERNAME, username);
    }

    public void enterPassword(String password) {
        clearAndType(LoginLocators.PASSWORD, password);
    }

    public void clickLoginButton() {
        waitUntilClickable(LoginLocators.LOGIN_BUTTON).click();
    }

    public String getErrorMessage() {
        return waitUntilVisible(LoginLocators.ERROR_MESSAGE).getText();
    }

    public boolean isLoginFormDisplayed() {
        return isDisplayed(LoginLocators.USERNAME)
                && isDisplayed(LoginLocators.PASSWORD)
                && isDisplayed(LoginLocators.LOGIN_BUTTON);
    }

    public boolean isLoginFormHidden() {
        return isInvisible(LoginLocators.USERNAME);
    }

    public boolean prepareInvalidLoginEvidence() {
        wait.until(currentDriver -> {
            String message = waitUntilVisible(LoginLocators.ERROR_MESSAGE).getText();
            return message == null || message.isBlank() ? null : message.trim();
        });
        waitUntilVisible(LoginLocators.USERNAME);
        waitUntilVisible(LoginLocators.PASSWORD);
        waitUntilVisible(LoginLocators.LOGIN_BUTTON);
        return waitUntilVisualStateIsReady();
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
}
