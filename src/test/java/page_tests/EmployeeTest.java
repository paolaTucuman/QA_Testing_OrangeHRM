package page_tests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

import page_base.BaseTest;
import page_objects.DashboardPage;
import page_objects.EmployeePage;
import page_objects.LoginPage;
import page_utils.ConfigReader;
import page_utils.ScreenshotHelper;

public class EmployeeTest extends BaseTest {

    private static final DateTimeFormatter UNIQUE_SUFFIX =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test(groups = {"automated", "positive"})
    public void tc06RegistrarEmpleadoConDatosValidos() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                ConfigReader.get("admin.username"),
                ConfigReader.get("admin.password")
        );

        DashboardPage dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(),
                "TC06: no fue posible iniciar sesion");

        String suffix = LocalDateTime.now().format(UNIQUE_SUFFIX);
        String firstName = "Auto" + suffix;
        String middleName = "QA";
        String lastName = "TC06" + suffix;

        EmployeePage employeePage = new EmployeePage(driver);
        String generatedEmployeeId = employeePage.addEmployee(firstName, middleName, lastName);

        Assert.assertFalse(generatedEmployeeId.isBlank(),
                "TC06: OrangeHRM no genero un Employee ID");
        Assert.assertTrue(employeePage.isPersonalDetailsDisplayed(),
                "TC06: no se mostro la pantalla Personal Details");
        Assert.assertEquals(employeePage.getEmployeeId(), generatedEmployeeId,
                "TC06: el Employee ID guardado no coincide con el generado");
        Assert.assertEquals(employeePage.getSavedFirstName(), firstName,
                "TC06: el First Name guardado no coincide");
        Assert.assertEquals(employeePage.getSavedMiddleName(), middleName,
                "TC06: el Middle Name guardado no coincide");
        Assert.assertEquals(employeePage.getSavedLastName(), lastName,
                "TC06: el Last Name guardado no coincide");

        employeePage.preparePersonalDetailsEvidence();
        ScreenshotHelper.takeScreenshot(driver,
                "TC06_Registro_Empleado_Exitoso_" + getCurrentBrowser());
    }
}
