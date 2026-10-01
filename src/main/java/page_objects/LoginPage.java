package page_objects;

import page_base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends BasePage {

    @FindBy(name = "username")
    private WebElement usernameInput;

    @FindBy(name = "password")
    private WebElement passwordInput;

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement loginButton;

    @FindBy(xpath = "//p[contains(@class, 'oxd-alert-content-text')]")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void enterUsername(String username) {
        clearAndType(usernameInput, username);
    }

    public void enterPassword(String password) {
        clearAndType(passwordInput, password);
    }

    public void clickLoginButton() {
        waitUntilClickable(loginButton).click();
    }

    public String getErrorMessage() {
        return waitUntilVisible(errorMessage).getText();
    }

    public boolean isLoginFormDisplayed() {
        return isDisplayed(usernameInput) && isDisplayed(passwordInput) && isDisplayed(loginButton);
    }

    public boolean isLoginFormHidden() {
        return isInvisible(usernameInput);
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
}
