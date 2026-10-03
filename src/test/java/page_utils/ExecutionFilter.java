package page_utils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

public class ExecutionFilter implements IMethodInterceptor {

    private static final Pattern CASE_ID_PATTERN = Pattern.compile("^tc(\\d{2})", Pattern.CASE_INSENSITIVE);

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        String browser = context.getCurrentXmlTest().getParameter("browser");
        if (browser == null || browser.isBlank()) {
            throw new IllegalStateException("El parametro browser no esta definido en testing.xml");
        }

        return methods.stream()
                .filter(method -> shouldRun(method, browser))
                .toList();
    }

    private boolean shouldRun(IMethodInstance method, String browser) {
        String methodName = method.getMethod().getMethodName();
        Matcher matcher = CASE_ID_PATTERN.matcher(methodName);
        if (!matcher.find()) {
            throw new IllegalStateException(
                    "El metodo automatizado no comienza con un caseId valido: " + methodName);
        }

        String caseId = "TC" + matcher.group(1);
        boolean run = ExecutionControl.shouldRun(caseId, browser);
        System.out.printf(
                "CONTROL | Caso: %s | Navegador: %s | Ejecutar: %s | Hoja: %s%n",
                caseId,
                browser.toLowerCase(Locale.ROOT),
                run,
                ExecutionControl.getDataSheet(caseId)
        );
        return run;
    }
}
