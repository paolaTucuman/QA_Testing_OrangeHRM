package page_locators;

import org.openqa.selenium.By;

public final class EmployeeLocators {

    public static final By ADD_EMPLOYEE_BUTTON = By.xpath(
            "//div[contains(@class,'orangehrm-header-container')]" +
            "//button[@type='button' and .//i[contains(@class,'bi-plus')]]"
    );
    public static final By FIRST_NAME = By.name("firstName");
    public static final By MIDDLE_NAME = By.name("middleName");
    public static final By LAST_NAME = By.name("lastName");
    public static final By EMPLOYEE_ID = By.cssSelector(
            "form.oxd-form input.oxd-input:not([name])"
    );
    public static final By SEARCH_EMPLOYEE_ID = By.cssSelector(
            ".oxd-table-filter form .oxd-grid-4 " +
            ".oxd-grid-item:nth-child(2) input"
    );
    public static final By SAVE_BUTTON = By.xpath("//button[@type='submit']");
    public static final By SEARCH_BUTTON = By.cssSelector(
            ".oxd-table-filter button[type='submit']"
    );
    public static final By FIRST_NAME_REQUIRED = By.xpath(
            "//input[@name='firstName']" +
            "/ancestor::div[contains(@class,'oxd-input-group')][1]" +
            "//span[contains(@class,'oxd-input-field-error-message')]"
    );
    public static final By RESULT_ROWS = By.cssSelector(".oxd-table-card");

    public static By employeeResultRow(String employeeId) {
        return By.xpath(
                "//div[contains(@class,'oxd-table-card')]" +
                "[.//div[normalize-space()='" + employeeId + "']]"
        );
    }

    private EmployeeLocators() {
    }
}
