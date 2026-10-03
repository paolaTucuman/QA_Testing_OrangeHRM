package page_locators;

import org.openqa.selenium.By;

public final class EmployeeLocators {

    public static final By ADD_EMPLOYEE_BUTTON = By.xpath(
            "//button[normalize-space()='Add']"
    );
    public static final By EMPLOYEE_LIST_TAB = By.xpath(
            "//a[normalize-space()='Employee List']"
    );
    public static final By FIRST_NAME = By.name("firstName");
    public static final By MIDDLE_NAME = By.name("middleName");
    public static final By LAST_NAME = By.name("lastName");
    public static final By EMPLOYEE_ID = By.xpath(
            "//label[normalize-space()='Employee Id']" +
            "/ancestor::div[contains(@class,'oxd-input-group')]//input"
    );
    public static final By SAVE_BUTTON = By.xpath("//button[@type='submit']");
    public static final By SEARCH_BUTTON = By.xpath(
            "//button[@type='submit' and normalize-space()='Search']"
    );
    public static final By SEARCH_RESULT_SUMMARY = By.xpath(
            "//div[contains(@class,'orangehrm-horizontal-padding')]" +
            "//span[contains(normalize-space(),'Record Found')]"
    );
    public static final By PERSONAL_DETAILS_TITLE = By.xpath(
            "//h6[normalize-space()='Personal Details']"
    );
    public static final By ADD_EMPLOYEE_TITLE = By.xpath(
            "//h6[normalize-space()='Add Employee']"
    );
    public static final By FIRST_NAME_REQUIRED = By.xpath(
            "//input[@name='firstName']" +
            "/ancestor::div[contains(@class,'oxd-input-group')][1]" +
            "//span[normalize-space()='Required']"
    );

    public static By employeeResultRow(String employeeId) {
        return By.xpath(
                "//div[contains(@class,'oxd-table-card')]" +
                "[.//div[normalize-space()='" + employeeId + "']]"
        );
    }

    private EmployeeLocators() {
    }
}
