package page_objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EmployeePage {

    private WebDriver driver;

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
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void goToPIM() {
        pimMenu.click();
    }

    public void clickAddEmployee() {
        addEmployeeButton.click();
    }

    public void fillEmployeeForm(String firstName, String middleName, String lastName) {
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);

        middleNameInput.clear();
        middleNameInput.sendKeys(middleName);

        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
    }

    public void saveEmployee() {
        saveButton.click();
    }

    public boolean isPersonalDetailsDisplayed() {
        try {
            return personalDetailsTitle.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void addEmployee(String firstName, String middleName, String lastName) {
        goToPIM();
        clickAddEmployee();
        fillEmployeeForm(firstName, middleName, lastName);
        saveEmployee();
    }
}