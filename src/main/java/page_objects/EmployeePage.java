package page_objects;

import page_base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EmployeePage extends BasePage {

    @FindBy(xpath = "//span[text()='PIM']")
    private WebElement pimMenu;

    @FindBy(xpath = "//button[text()=' Add ']")
    private WebElement addEmployeeButton;

    @FindBy(name = "firstName")
    private WebElement firstNameInput;

    @FindBy(name = "middleName")
    private WebElement middleNameInput;

    @FindBy(name = "lastName")
    private WebElement lastNameInput;

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement saveButton;

    @FindBy(xpath = "//h6[text()='Personal Details']")
    private WebElement personalDetailsTitle;

    public EmployeePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void goToPIM() {
        waitUntilClickable(pimMenu).click();
    }

    public void clickAddEmployee() {
        waitUntilClickable(addEmployeeButton).click();
    }

    public void fillEmployeeForm(String firstName, String middleName, String lastName) {
        clearAndType(firstNameInput, firstName);
        clearAndType(middleNameInput, middleName);
        clearAndType(lastNameInput, lastName);
    }

    public void saveEmployee() {
        waitUntilClickable(saveButton).click();
    }

    public boolean isPersonalDetailsDisplayed() {
        return isDisplayed(personalDetailsTitle);
    }

    public void addEmployee(String firstName, String middleName, String lastName) {
        goToPIM();
        clickAddEmployee();
        fillEmployeeForm(firstName, middleName, lastName);
        saveEmployee();
    }
}
