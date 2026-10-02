package page_base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private static final By LOADING_INDICATORS = By.cssSelector(
            ".oxd-loading-spinner, .oxd-form-loader"
    );

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    protected WebElement waitUntilVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitUntilVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
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

    protected boolean waitUntilVisualStateIsReady() {
        try {
            wait.until(currentDriver -> "complete".equals(
                    ((JavascriptExecutor) currentDriver).executeScript(
                            "return document.readyState"
                    )
            ));

            wait.until(currentDriver -> currentDriver.findElements(LOADING_INDICATORS)
                    .stream()
                    .noneMatch(this::isDisplayedSafely));

            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5));
            ((JavascriptExecutor) driver).executeAsyncScript(
                    "const done = arguments[arguments.length - 1];" +
                    "const afterFonts = () => requestAnimationFrame(() => " +
                    "requestAnimationFrame(done));" +
                    "if (document.fonts && document.fonts.ready) {" +
                    "document.fonts.ready.then(afterFonts, afterFonts);" +
                    "} else { afterFonts(); }"
            );
            return true;
        } catch (TimeoutException | ScriptTimeoutException e) {
            System.out.println(
                    "Advertencia: no se confirmo la estabilidad visual de la evidencia: " +
                    e.getMessage()
            );
            return false;
        }
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

    private boolean isDisplayedSafely(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
