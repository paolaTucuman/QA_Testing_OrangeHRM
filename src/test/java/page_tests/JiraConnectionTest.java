package page_tests;

import page_utils.ConfigReader;
import page_utils.JiraReporter;
import org.testng.annotations.Test;

public class JiraConnectionTest {

    @Test
    public void testConexionJira() {
        System.out.println("Probando conexión con JIRA...");
        System.out.println("Project Key: "
                + ConfigReader.get("jira.project.key", "NO_CONFIGURADO"));

        String issueKey = JiraReporter.crearBug(
            "[AUTO] Prueba de conexión desde Selenium",
            "Este es un bug de prueba creado automáticamente."
        );

        if (issueKey != null) {
            System.out.println("Conexión exitosa. Bug creado: " + issueKey);
            JiraReporter.agregarComentario(issueKey, "Comentario de prueba desde automatización");
            System.out.println("JIRA está funcionando correctamente");
        } else {
            System.out.println("No se pudo conectar con JIRA. Verifica tus credenciales.");
        }
    }
}
