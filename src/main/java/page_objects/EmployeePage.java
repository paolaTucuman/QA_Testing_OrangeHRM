package page_objects;

import page_base.BasePage;
import page_locators.CommonLocators;
import page_locators.EmployeeLocators;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EmployeePage extends BasePage {

    public EmployeePage(WebDriver driver) {
        super(driver);
    }

    public void goToPIM() {
        waitUntilClickable(CommonLocators.PIM_MENU).click();
        waitUntilClickable(EmployeeLocators.SEARCH_BUTTON);
        waitUntilInvisible(CommonLocators.LOADING_SPINNER);
    }

    public void clickAddEmployee() {
        waitUntilClickable(EmployeeLocators.ADD_EMPLOYEE_BUTTON).click();
        waitUntilVisible(EmployeeLocators.ADD_EMPLOYEE_TITLE);
        waitUntilInvisible(CommonLocators.LOADING_SPINNER);
    }

    public void goToEmployeeList() {
        waitUntilClickable(EmployeeLocators.EMPLOYEE_LIST_TAB).click();
        waitUntilClickable(EmployeeLocators.SEARCH_BUTTON);
        waitUntilInvisible(CommonLocators.LOADING_SPINNER);
    }

    public void fillEmployeeForm(String firstName, String middleName, String lastName) {
        clearAndType(EmployeeLocators.FIRST_NAME, firstName);
        clearAndType(EmployeeLocators.MIDDLE_NAME, middleName);
        clearAndType(EmployeeLocators.LAST_NAME, lastName);
    }

    public void saveEmployee() {
        waitUntilClickable(EmployeeLocators.SAVE_BUTTON).click();
        waitUntilVisible(EmployeeLocators.PERSONAL_DETAILS_TITLE);
        waitUntilHasValue(EmployeeLocators.EMPLOYEE_ID);
        waitUntilInvisible(CommonLocators.LOADING_SPINNER);
    }

    public boolean isPersonalDetailsDisplayed() {
        return isDisplayed(EmployeeLocators.PERSONAL_DETAILS_TITLE);
    }

    public String getEmployeeId() {
        return waitUntilHasValue(EmployeeLocators.EMPLOYEE_ID);
    }

    public String getSavedFirstName() {
        return getInputValue(EmployeeLocators.FIRST_NAME);
    }

    public String getSavedMiddleName() {
        return getInputValue(EmployeeLocators.MIDDLE_NAME);
    }

    public String getSavedLastName() {
        return getInputValue(EmployeeLocators.LAST_NAME);
    }

    public boolean preparePersonalDetailsEvidence() {
        waitUntilHasValue(EmployeeLocators.EMPLOYEE_ID);
        waitUntilInvisible(CommonLocators.LOADING_SPINNER);
        scrollIntoView(EmployeeLocators.FIRST_NAME);
        return waitUntilVisualStateIsReady();
    }

    public String searchEmployeeById(String employeeId) {
        goToEmployeeList();
        clearAndType(EmployeeLocators.EMPLOYEE_ID, employeeId);
        waitUntilClickable(EmployeeLocators.SEARCH_BUTTON).click();
        waitUntilInvisible(CommonLocators.LOADING_SPINNER);

        WebElement resultRow = waitUntilVisible(EmployeeLocators.employeeResultRow(employeeId));
        scrollIntoView(EmployeeLocators.employeeResultRow(employeeId));
        return resultRow.getText();
    }

    public String getSearchResultSummary() {
        return waitUntilVisible(EmployeeLocators.SEARCH_RESULT_SUMMARY).getText().trim();
    }

    public void attemptAddEmployeeWithoutFirstName(String lastName) {
        goToPIM();
        clickAddEmployee();
        clearAndType(EmployeeLocators.FIRST_NAME, "");
        clearAndType(EmployeeLocators.MIDDLE_NAME, "");
        clearAndType(EmployeeLocators.LAST_NAME, lastName);
        waitUntilClickable(EmployeeLocators.SAVE_BUTTON).click();
        waitUntilVisible(EmployeeLocators.FIRST_NAME_REQUIRED);
    }

    public String getFirstNameRequiredMessage() {
        return waitUntilVisible(EmployeeLocators.FIRST_NAME_REQUIRED).getText().trim();
    }

    public boolean isOnAddEmployeePage() {
        return isDisplayed(EmployeeLocators.ADD_EMPLOYEE_TITLE)
                && driver.getCurrentUrl().contains("/pim/addEmployee");
    }

    public boolean isPersonalDetailsDisplayedNow() {
        return isDisplayedNow(EmployeeLocators.PERSONAL_DETAILS_TITLE);
    }

    public boolean prepareSearchEvidence(String employeeId) {
        waitUntilVisible(EmployeeLocators.employeeResultRow(employeeId));
        scrollIntoView(EmployeeLocators.employeeResultRow(employeeId));
        return waitUntilVisualStateIsReady();
    }

    public boolean prepareFirstNameValidationEvidence() {
        waitUntilVisible(EmployeeLocators.FIRST_NAME_REQUIRED);
        scrollIntoView(EmployeeLocators.FIRST_NAME);
        return waitUntilVisualStateIsReady();
    }

    public String addEmployee(String firstName, String middleName, String lastName) {
        goToPIM();
        clickAddEmployee();
        fillEmployeeForm(firstName, middleName, lastName);
        String generatedEmployeeId = waitUntilHasValue(EmployeeLocators.EMPLOYEE_ID);
        saveEmployee();
        return generatedEmployeeId;
    }
}
