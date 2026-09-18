package page_tests;

import page_base.BaseTest;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class LoginDataDrivenTest extends BaseTest {

    private static final String EXCEL_PATH = ConfigReader.get("excel.path");
    private static final String SHEET_NAME = ConfigReader.get("excel.sheet.name");

    @DataProvider(name = "datosLogin")
    public Object[][] obtenerDatosLogin() {
        List<Map<String, String>> datos = ExcelReader.leerDatos(EXCEL_PATH, SHEET_NAME);
        Object[][] dataProvider = new Object[datos.size()][1];

        for (int i = 0; i < datos.size(); i++) {
            dataProvider[i][0] = datos.get(i);
        }

        return dataProvider;
    }

    @Test(dataProvider = "datosLogin")
    public void testLoginConMultiplesDatos(Map<String, String> datos) {
        String username = datos.get("username");
        String password = datos.get("password");
        String expectedResult = datos.get("expectedResult");

        System.out.println("Probando usuario: " + username + " -> Esperado: " + expectedResult);

        String loginUrl = ConfigReader.get("base.url");
        driver.navigate().to(loginUrl);

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, password);

        if (expectedResult.equalsIgnoreCase("success")) {
            Assert.assertTrue(loginPage.isDashboardDisplayed(),
                    "Login fallo para: " + username);
            System.out.println("Login exitoso para: " + username);
        } else {
            String errorMsg = loginPage.getErrorMessage();
            Assert.assertEquals(errorMsg, "Invalid credentials",
                    "Mensaje de error incorrecto para: " + username);
            System.out.println("Error esperado para: " + username);
        }
    }
}
