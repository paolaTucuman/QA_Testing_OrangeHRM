# Plan de automatización — OrangeHRM

## 1. Propósito

Definir el alcance, la arquitectura, los criterios de calidad y el estado del
framework de automatización utilizado para validar los flujos seleccionados de
OrangeHRM.

## 2. Objetivos

- Validar flujos críticos de autenticación, empleados y cierre de sesión.
- Ejecutar los casos en Chrome, Firefox y Edge según la matriz de Excel.
- Separar datos, pasos funcionales, interacción web, localizadores y validaciones.
- Registrar comparaciones entre resultado esperado y resultado actual.
- Generar capturas útiles como evidencia, sin usarlas como sustituto de las aserciones.
- Integrar Jira solamente para los casos expresamente habilitados.
- Mantener pruebas legibles, reutilizables y resistentes a cambios de idioma.

## 3. Alcance automatizado

| Caso | Suite | Escenario principal |
|---|---|---|
| TC01 | POSITIVE | Inicio de sesión con credenciales válidas |
| TC02 | NEGATIVE | Rechazo de contraseña inválida |
| TC06 | POSITIVE | Registro de empleado con datos válidos |
| TC07 | POSITIVE | Búsqueda del empleado creado por ID |
| TC08 | NEGATIVE | Validación de First Name obligatorio |
| TC11 | SECURITY | Logout y revisión del contenido protegido al usar Back |

Los casos exclusivamente manuales no se ejecutan desde esta suite. Los defectos
manuales se registran directamente en Jira.

## 4. Arquitectura

El proyecto aplica Page Object Model con una capa adicional de pasos funcionales:

- page_locators: selectores centralizados por página.
- page_objects: acciones e interacción con Selenium.
- test_steps: pasos funcionales y validaciones expresados con nombres legibles.
- page_tests: orquestación mínima de TestNG y DataProviders.
- page_utils: Excel, configuración, evidencias, capturas y Jira.
- page_base: ciclo de vida del WebDriver y comportamiento común.

Flujo general:

    Excel -> TestNG -> Test Steps -> Page Objects -> Locators -> OrangeHRM
                         |-> EvidenceReporter
                         |-> ScreenshotHelper
                         |-> JiraReporter

### Decisiones principales

1. Page Object Model evita duplicar acciones y separa la lógica web del caso de prueba.
2. La capa test_steps permite leer los tests casi como lenguaje natural sin añadir Cucumber ni complejidad innecesaria.
3. Los localizadores se concentran en clases específicas para conocer cuáles usa el proyecto y mantenerlos en un solo lugar por página.
4. Los datos y la selección de ejecución se administran desde Excel para aplicar un enfoque Data-Driven.
5. Se usan esperas explícitas basadas en estados de la aplicación; no se agregan pausas fijas para ocultar problemas de sincronización.
6. Las validaciones críticas prefieren URL, estructura, atributos y estado funcional antes que textos traducibles.
7. Los datos dinámicos evitan colisiones en una aplicación demo compartida.
8. La ejecución por navegador es secuencial para reducir interferencia entre datos y conservar evidencia clara.

## 5. Control desde Excel

La hoja settings controla la suite solicitada mediante runSuite:

- ALL
- POSITIVE
- NEGATIVE
- SECURITY

La hoja execution define por caso:

- caseId: identificador trazable.
- enabled: habilita o deshabilita el caso.
- suite: clasificación funcional.
- jiraReport: autoriza el reporte automático del fallo.
- chrome, firefox y edge: seleccionan navegadores.
- dataSheet: hoja que contiene los datos del caso.

Las hojas login, employee y session contienen entradas y resultados esperados. Excel no debe almacenar tokens, contraseñas personales ni otros secretos.

## 6. Validación y evidencia

Las aserciones de TestNG determinan si una prueba pasa o falla. Las capturas son evidencia visual complementaria y no reemplazan las validaciones por localizadores.

EvidenceReporter registra:

- caso de prueba;
- navegador;
- criterio validado;
- valor esperado;
- valor actual;
- resultado PASS o FAIL.

ScreenshotHelper espera un estado visual representativo antes de capturar y distingue una evidencia lista de una incompleta. Las carpetas target, test-output y screenshots son artefactos generados y no deben versionarse.

## 7. Integración con Jira

El reporte automático requiere simultáneamente:

- jira.enabled=true;
- jira.auto.create=true;
- jiraReport=TRUE para el caso en Excel.

Cuando un caso autorizado falla, JiraReporter busca primero un issue abierto asociado al caseId o a su etiqueta. Si existe, lo reutiliza y agrega únicamente el navegador o la evidencia que falta; si no existe, crea uno. Este comportamiento evita un bug duplicado por navegador o por cada nueva ejecución.

El issue debe incluir caso, precondición, pasos, resultado esperado, resultado actual, navegador, URL y captura relevante. Las credenciales y el token de Jira pertenecen al archivo local ignorado por Git o a variables de entorno; nunca al repositorio.

## 8. Ejecución

Desde terminal:

    mvn clean test

También puede ejecutarse testing.xml mediante Run As > TestNG Suite en Eclipse.

Orden de ejecución:

1. Cargar configuración y matriz de Excel.
2. Filtrar suite, casos habilitados y navegadores.
3. Crear el WebDriver del navegador seleccionado.
4. Ejecutar pasos funcionales y aserciones.
5. Registrar comparaciones y capturas.
6. Reportar en Jira solamente los fallos autorizados.
7. Cerrar el navegador y continuar con la siguiente combinación.

## 9. Criterios de finalización

La automatización se considera lista cuando:

- los casos configurados pueden ejecutarse desde testing.xml;
- la selección de suite, casos, Jira y navegadores funciona desde Excel;
- cada validación muestra esperado, actual y resultado;
- las capturas representan el estado relevante del escenario;
- TC11 conserva evidencia del Login y del resultado de Back;
- Jira reutiliza el issue del caso y no crea duplicados por navegador;
- los métodos de prueba delegan pasos legibles en test_steps;
- no hay secretos ni resultados de ejecución preparados para commit.

## 10. Estado actual

### Resultado de la regresión final

- 18 ejecuciones: seis casos automatizados en Chrome, Firefox y Edge.
- 16 resultados PASS, 2 FAIL y 0 SKIP.
- Los dos fallos corresponden a TC11 en Chrome y Edge; Firefox conservó el
  comportamiento esperado.

### Trazabilidad de defectos

- **KAN-1 / TC05:** mensaje de recuperación exitoso para un username inexistente;
  documentado y ejecutado manualmente.
- **KAN-5 / TC11:** contenido PIM cacheado visible después del logout al usar
  Back en Chrome y Edge. Una URL protegida continúa exigiendo autenticación, por
  lo que no se presenta como sesión activa ni como vulnerabilidad de autorización
  confirmada.

### Completado

- Page Object Model y localizadores centralizados.
- Capa de pasos funcionales legibles.
- Seis casos automatizados.
- Ejecución Data-Driven y matriz de suites.
- Selección por Chrome, Firefox y Edge.
- Evidencia esperada/actual y capturas de pantalla.
- Integración con Jira con búsqueda y reutilización de issues.
- Exclusión de secretos y artefactos mediante .gitignore.

### Próximas mejoras

- Integrar GitHub Actions para ejecutar una regresión controlada.
- Publicar el reporte de TestNG como artefacto del pipeline.
- Gestionar credenciales mediante GitHub Secrets.
- Añadir nuevos casos solamente cuando aporten cobertura relevante.
- Revisar periódicamente dependencias y localizadores.
- Evaluar paralelismo únicamente con datos independientes y drivers aislados.

## 11. Riesgos y controles

- OrangeHRM es una demo pública: puede presentar lentitud, cambios o datos de otros usuarios.
- La versión del navegador debe ser compatible con Selenium y su soporte CDP.
- Una captura correcta no prueba por sí sola el comportamiento; siempre debe existir una aserción funcional.
- El texto visible puede variar por idioma; solo debe validarse literalmente cuando el texto sea el requisito.
- Antes de cada commit debe revisarse git status para impedir la inclusión de secretos o evidencias temporales.

## 12. Características de la solución

La solución es Data-Driven, multibrowser y mantenible. La separación entre
pruebas, pasos funcionales, páginas y localizadores reduce la duplicación y
mantiene la trazabilidad mediante Excel, evidencia técnica y Jira.
