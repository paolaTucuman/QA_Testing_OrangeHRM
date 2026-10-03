package page_utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

public class JiraReporter {

    private static final String JIRA_URL = ConfigReader.get("jira.url", "");
    private static final String EMAIL = ConfigReader.get("jira.email", "");
    private static final String API_TOKEN = ConfigReader.get("jira.api.token", "");
    private static final String PROJECT_KEY = ConfigReader.get("jira.project.key", "");
    private static final String ISSUE_TYPE = ConfigReader.get("jira.issue.type", "Bug");
    private static final boolean JIRA_ENABLED = ConfigReader.getBoolean("jira.enabled");
    private static final boolean AUTO_CREATE_ENABLED =
            ConfigReader.getBoolean("jira.auto.create");

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isAutomaticCreationEnabled() {
        return JIRA_ENABLED && AUTO_CREATE_ENABLED;
    }

    public static String crearOReutilizarIncidencia(
            String caseId, String titulo, String descripcion) {
        if (!JIRA_ENABLED) {
            System.out.println("JIRA deshabilitado por configuracion");
            return null;
        }

        try {
            validateConfiguration();
            String existingIssueKey = findOpenIssue(caseId);
            if (existingIssueKey != null) {
                System.out.println("Incidencia existente reutilizada en JIRA: " + existingIssueKey);
                return existingIssueKey;
            }

            String auth = EMAIL + ":" + API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(
                    auth.getBytes(StandardCharsets.UTF_8));

            ObjectNode root = mapper.createObjectNode();
            ObjectNode fields = root.putObject("fields");

            fields.putObject("project").put("key", PROJECT_KEY);
            fields.put("summary", titulo);
            fields.putObject("issuetype").put("name", ISSUE_TYPE);
            ArrayNode labels = fields.putArray("labels");
            labels.add("automation");
            labels.add(toJiraLabel(caseId));

            ObjectNode description = fields.putObject("description");
            description.put("type", "doc");
            description.put("version", 1);

            ArrayNode content = description.putArray("content");
            ObjectNode paragraph = content.addObject();
            paragraph.put("type", "paragraph");

            ArrayNode paragraphContent = paragraph.putArray("content");
            ObjectNode textNode = paragraphContent.addObject();
            textNode.put("type", "text");
            textNode.put("text", descripcion);

            String jsonBody = mapper.writeValueAsString(root);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JIRA_URL + "/rest/api/3/issue"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                ObjectNode responseJson = (ObjectNode) mapper.readTree(response.body());
                String issueKey = responseJson.get("key").asText();
                System.out.println("Incidencia creada en JIRA: " + issueKey);
                return issueKey;
            } else {
                System.out.println("Error al crear incidencia: " + response.statusCode());
                System.out.println("Detalle devuelto por JIRA: " + response.body());
            }

        } catch (Exception e) {
            System.out.println("Excepcion al conectar con JIRA: " + e.getMessage());
        }
        return null;
    }

    private static String findOpenIssue(String caseId) throws Exception {
        String auth = EMAIL + ":" + API_TOKEN;
        String encodedAuth = Base64.getEncoder().encodeToString(
                auth.getBytes(StandardCharsets.UTF_8));

        String normalizedCaseId = caseId.toUpperCase(Locale.ROOT);
        String jql = "project = \"" + PROJECT_KEY + "\" " +
                "AND (labels = \"" + toJiraLabel(normalizedCaseId) + "\" " +
                "OR summary ~ \"" + normalizedCaseId + "\") " +
                "AND statusCategory != Done ORDER BY created DESC";

        ObjectNode searchBody = mapper.createObjectNode();
        searchBody.put("jql", jql);
        searchBody.put("maxResults", 1);
        searchBody.putArray("fields").add("key");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(JIRA_URL + "/rest/api/3/search/jql"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(searchBody)))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "No se pudo comprobar duplicados en JIRA. HTTP " + response.statusCode());
        }

        JsonNode issues = mapper.readTree(response.body()).path("issues");
        if (!issues.isArray() || issues.isEmpty()) {
            return null;
        }
        return issues.get(0).path("key").asText(null);
    }

    private static String toJiraLabel(String caseId) {
        return "auto-" + caseId.toLowerCase(Locale.ROOT);
    }

    public static boolean adjuntarCaptura(String issueKey, String screenshotPath) {
        if (!JIRA_ENABLED || issueKey == null) return false;

        try {
            validateConfiguration();
            File file = new File(screenshotPath);
            if (!file.exists()) {
                System.out.println("No se encontro el archivo: " + screenshotPath);
                return false;
            }

            String auth = EMAIL + ":" + API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(
                    auth.getBytes(StandardCharsets.UTF_8));

            String boundary = "----JiraBoundary" + System.currentTimeMillis();

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] headerBytes = ("--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                    "Content-Type: image/png\r\n\r\n").getBytes();
            byte[] footerBytes = ("\r\n--" + boundary + "--\r\n").getBytes();

            byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
            System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
            System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
            System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JIRA_URL + "/rest/api/3/issue/" + issueKey + "/attachments"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("X-Atlassian-Token", "no-check")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Captura adjuntada en JIRA: " + file.getName());
                return true;
            }
            System.out.println("JIRA rechazo la captura. HTTP " + response.statusCode());
            System.out.println("Detalle devuelto por JIRA: " + response.body());

        } catch (Exception e) {
            System.out.println("Error al adjuntar captura: " + e.getMessage());
        }
        return false;
    }

    public static boolean agregarComentario(String issueKey, String comentario) {
        if (!JIRA_ENABLED || issueKey == null) return false;

        try {
            validateConfiguration();
            String auth = EMAIL + ":" + API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(
                    auth.getBytes(StandardCharsets.UTF_8));

            ObjectNode root = mapper.createObjectNode();
            ObjectNode body = root.putObject("body");
            body.put("type", "doc");
            body.put("version", 1);

            ArrayNode content = body.putArray("content");
            ObjectNode paragraph = content.addObject();
            paragraph.put("type", "paragraph");

            ArrayNode paragraphContent = paragraph.putArray("content");
            ObjectNode textNode = paragraphContent.addObject();
            textNode.put("type", "text");
            textNode.put("text", comentario);

            String jsonBody = mapper.writeValueAsString(root);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(JIRA_URL + "/rest/api/3/issue/" + issueKey + "/comment"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                System.out.println("Comentario agregado en JIRA: " + issueKey);
                return true;
            }
            System.out.println("JIRA rechazo el comentario. HTTP " + response.statusCode());
            System.out.println("Detalle devuelto por JIRA: " + response.body());

        } catch (Exception e) {
            System.out.println("Error al agregar comentario: " + e.getMessage());
        }
        return false;
    }

    private static void validateConfiguration() {
        requireValue("jira.url", JIRA_URL);
        requireValue("jira.email", EMAIL);
        requireValue("jira.api.token", API_TOKEN);
        requireValue("jira.project.key", PROJECT_KEY);
        requireValue("jira.issue.type", ISSUE_TYPE);
    }

    private static void requireValue(String key, String value) {
        if (value == null || value.isBlank() || value.startsWith("REEMPLAZAR")) {
            throw new IllegalStateException("Falta configurar " + key);
        }
    }
}
