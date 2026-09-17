package page_tests;

import page_base.BaseTest;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FailureTest extends BaseTest {

    @Test
    public void testLoginQueFallaAdrede() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );

        Assert.assertTrue(
                loginPage.getErrorMessage().contains("Esto no existe"),
                "Prueba disenada para fallar y capturar pantalla"
        );
    }
}