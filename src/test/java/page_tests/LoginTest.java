package page_tests;

import page_base.BaseTest;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test
    public void testLoginExitoso() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );

        Assert.assertTrue(loginPage.isDashboardDisplayed(),
                "No se pudo iniciar sesion correctamente");
    }

    @Test
    public void testLoginFallido() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("UsuarioInvalido", "ContraseñaInvalida");

        String errorMsg = loginPage.getErrorMessage();
        Assert.assertEquals(errorMsg, "Invalid credentials",
                "El mensaje de error no coincide");
    }
}