package page_locators;

import org.openqa.selenium.By;

public final class DashboardLocators {

    public static final By DASHBOARD_TITLE = By.xpath(
            "//h6[normalize-space()='Dashboard']"
    );

    private DashboardLocators() {
    }
}
