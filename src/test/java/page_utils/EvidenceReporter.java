package page_utils;

import java.util.Objects;

import org.testng.Reporter;

public final class EvidenceReporter {

    private EvidenceReporter() {
    }

    public static void recordComparison(
            String caseId,
            String browser,
            String validation,
            Object expected,
            Object actual
    ) {
        String expectedText = String.valueOf(expected);
        String actualText = String.valueOf(actual);
        String result = Objects.equals(expectedText, actualText) ? "PASS" : "FAIL";

        Reporter.log(String.format(
                "EVIDENCIA | Caso: %s | Navegador: %s | Validacion: %s | " +
                "Esperado: %s | Actual: %s | Resultado: %s",
                safe(caseId),
                safe(browser),
                safe(validation),
                safe(expectedText),
                safe(actualText),
                result
        ), true);
    }

    public static void recordScreenshot(
            String caseId,
            String browser,
            String screenshotPath,
            boolean visualStateReady
    ) {
        Reporter.log(String.format(
                "EVIDENCIA | Caso: %s | Navegador: %s | Estado visual: %s | Captura: %s",
                safe(caseId),
                safe(browser),
                visualStateReady ? "LISTA" : "INCOMPLETA",
                safe(screenshotPath)
        ), true);
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", " ").replace("\n", " ").trim();
    }
}
