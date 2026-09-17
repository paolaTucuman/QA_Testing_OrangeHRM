package page_tests;

import page_base.BaseTest;
import page_objects.EmployeePage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmployeeTest extends BaseTest {

    @Test
    public void testAgregarEmpleado() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );
        Assert.assertTrue(loginPage.isDashboardDisplayed(), "Login fallo");

        EmployeePage employeePage = new EmployeePage(driver);
        employeePage.addEmployee("Paola", "Maria", "Test");

        Assert.assertTrue(employeePage.isPersonalDetailsDisplayed(),
                "No se pudo crear el empleado");
    }
}