package page_base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    protected WebElement waitUntilVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitUntilClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void clearAndType(WebElement element, String value) {
        WebElement visibleElement = waitUntilVisible(element);
        visibleElement.clear();
        visibleElement.sendKeys(value == null ? "" : value);
    }

    protected String waitUntilHasValue(WebElement element) {
        return wait.until(driver -> {
            String value = element.getAttribute("value");
            return value == null || value.isBlank() ? null : value.trim();
        });
    }

    protected String getInputValue(WebElement element) {
        String value = waitUntilVisible(element).getAttribute("value");
        return value == null ? "" : value.trim();
    }

    protected void waitUntilInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void scrollIntoView(WebElement element) {
        WebElement visibleElement = waitUntilVisible(element);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                visibleElement
        );
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return waitUntilVisible(element).isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isDisplayedNow(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isInvisible(WebElement element) {
        try {
            return wait.until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
