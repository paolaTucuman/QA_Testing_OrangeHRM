package page_locators;

import org.openqa.selenium.By;

public final class CommonLocators {

    public static final By PIM_MENU = By.xpath("//span[normalize-space()='PIM']");
    public static final By LOADING_SPINNER = By.cssSelector(".oxd-loading-spinner");
    public static final By LOADING_INDICATORS = By.cssSelector(
            ".oxd-loading-spinner, .oxd-form-loader"
    );

    private CommonLocators() {
    }
}
