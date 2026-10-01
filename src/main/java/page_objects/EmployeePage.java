package page_objects;

import page_base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EmployeePage extends BasePage {

    private static final By LOADING_SPINNER = By.cssSelector(".oxd-loading-spinner");

    @FindBy(xpath = "//span[normalize-space()='PIM']")
    private WebElement pimMenu;

    @FindBy(xpath = "//button[normalize-space()='Add']")
    private WebElement addEmployeeButton;

    @FindBy(xpath = "//a[normalize-space()='Employee List']")
    private WebElement employeeListTab;

    @FindBy(name = "firstName")
    private WebElement firstNameInput;

    @FindBy(name = "middleName")
    private WebElement middleNameInput;

    @FindBy(name = "lastName")
    private WebElement lastNameInput;

    @FindBy(xpath = "//label[normalize-space()='Employee Id']/ancestor::div[contains(@class,'oxd-input-group')]//input")
    private WebElement employeeIdInput;

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement saveButton;

    @FindBy(xpath = "//button[@type='submit' and normalize-space()='Search']")
    private WebElement searchButton;

    @FindBy(xpath = "//h6[normalize-space()='Personal Details']")
    private WebElement personalDetailsTitle;

    @FindBy(xpath = "//h6[normalize-space()='Add Employee']")
    private WebElement addEmployeeTitle;

    @FindBy(xpath = "//input[@name='firstName']/ancestor::div[contains(@class,'oxd-input-group')][1]//span[normalize-space()='Required']")
    private WebElement firstNameRequiredMessage;

    public EmployeePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void goToPIM() {
        waitUntilClickable(pimMenu).click();
    }

    public void clickAddEmployee() {
        waitUntilClickable(addEmployeeButton).click();
        waitUntilVisible(addEmployeeTitle);
        waitUntilInvisible(LOADING_SPINNER);
    }

    public void goToEmployeeList() {
        waitUntilClickable(employeeListTab).click();
        waitUntilClickable(searchButton);
        waitUntilInvisible(LOADING_SPINNER);
    }

    public void fillEmployeeForm(String firstName, String middleName, String lastName) {
        clearAndType(firstNameInput, firstName);
        clearAndType(middleNameInput, middleName);
        clearAndType(lastNameInput, lastName);
    }

    public void saveEmployee() {
        waitUntilClickable(saveButton).click();
        waitUntilVisible(personalDetailsTitle);
        waitUntilHasValue(employeeIdInput);
        waitUntilInvisible(LOADING_SPINNER);
    }

    public boolean isPersonalDetailsDisplayed() {
        return isDisplayed(personalDetailsTitle);
    }

    public String getEmployeeId() {
        return waitUntilHasValue(employeeIdInput);
    }

    public String getSavedFirstName() {
        return getInputValue(firstNameInput);
    }

    public String getSavedMiddleName() {
        return getInputValue(middleNameInput);
    }

    public String getSavedLastName() {
        return getInputValue(lastNameInput);
    }

    public void preparePersonalDetailsEvidence() {
        waitUntilHasValue(employeeIdInput);
        waitUntilInvisible(LOADING_SPINNER);
        scrollIntoView(firstNameInput);
    }

    public String searchEmployeeById(String employeeId) {
        goToEmployeeList();
        clearAndType(employeeIdInput, employeeId);
        waitUntilClickable(searchButton).click();
        waitUntilInvisible(LOADING_SPINNER);

        WebElement resultRow = waitUntilVisible(employeeResultRow(employeeId));
        scrollIntoView(resultRow);
        return resultRow.getText();
    }

    public void attemptAddEmployeeWithoutFirstName(String lastName) {
        goToPIM();
        clickAddEmployee();
        clearAndType(firstNameInput, "");
        clearAndType(middleNameInput, "");
        clearAndType(lastNameInput, lastName);
        waitUntilClickable(saveButton).click();
        waitUntilVisible(firstNameRequiredMessage);
    }

    public String getFirstNameRequiredMessage() {
        return waitUntilVisible(firstNameRequiredMessage).getText().trim();
    }

    public boolean isOnAddEmployeePage() {
        return isDisplayed(addEmployeeTitle)
                && driver.getCurrentUrl().contains("/pim/addEmployee");
    }

    public boolean isPersonalDetailsDisplayedNow() {
        return isDisplayedNow(personalDetailsTitle);
    }

    public void prepareFirstNameValidationEvidence() {
        waitUntilVisible(firstNameRequiredMessage);
        scrollIntoView(firstNameInput);
    }

    private By employeeResultRow(String employeeId) {
        return By.xpath(
                "//div[contains(@class,'oxd-table-card')]" +
                "[.//div[normalize-space()='" + employeeId + "']]"
        );
    }

    public String addEmployee(String firstName, String middleName, String lastName) {
        goToPIM();
        clickAddEmployee();
        fillEmployeeForm(firstName, middleName, lastName);
        String generatedEmployeeId = waitUntilHasValue(employeeIdInput);
        saveEmployee();
        return generatedEmployeeId;
    }
}
