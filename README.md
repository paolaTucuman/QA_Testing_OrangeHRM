# Automatizacion web de OrangeHRM

Proyecto de QA para automatizar casos funcionales sobre la instancia publica de demostracion de OrangeHRM. Utiliza Java, Selenium WebDriver, TestNG, Maven y el patron Page Object Model.

Los ejecutables de ChromeDriver, GeckoDriver y EdgeDriver se resuelven mediante
Selenium Manager, incluido oficialmente en Selenium. No se requiere instalar ni
configurar los drivers manualmente.

## Estado actual

Actualmente el proyecto incluye:

- TC01: login con credenciales validas.
- TC02: login con password invalida.
- TC06: registro de un empleado con datos validos.
- TC07: busqueda por ID de un empleado creado por la propia prueba.
- TC08: validacion de First Name obligatorio.
- TC11: logout, exposicion visual de contenido protegido mediante Back y
  verificacion de que una URL protegida vuelva a exigir autenticacion.
- Datos funcionales y matriz de ejecucion dirigidos desde `TestData.xlsx`.
- Evidencias de exito y capturas automaticas ante fallos.
- Regresion controlada por caso y navegador desde Excel; actualmente solo Chrome.
- Integracion opcional con Jira, desactivada por defecto.

El avance y las siguientes fases se encuentran en `PLAN_AUTOMATIZACION.md`.

## Requisitos

- Java 25.
- Maven 3.9 o posterior.
- Google Chrome, Mozilla Firefox y Microsoft Edge.
- Acceso a Internet para descargar dependencias y utilizar OrangeHRM Demo.

## Configuracion local

1. Copiar `config.example.properties` como `src/test/resources/config.properties`.
2. Completar las credenciales de la instancia publica de OrangeHRM.
3. Mantener `jira.enabled=false` mientras no se quiera crear incidencias.
4. Si se habilita Jira, completar sus valores solo en `config.properties`.

En PowerShell:

```powershell
Copy-Item config.example.properties src/test/resources/config.properties
```

`config.properties` esta excluido mediante `.gitignore` y no debe incorporarse al repositorio.

La configuracion sigue esta prioridad: propiedad Java (`-Dclave=valor`), variable
de entorno, archivo local y valor por defecto. Los nombres de entorno se obtienen
reemplazando puntos por guiones bajos y usando mayusculas. Por ejemplo:

```text
ADMIN_USERNAME
ADMIN_PASSWORD
JIRA_URL
JIRA_EMAIL
JIRA_API_TOKEN
JIRA_PROJECT_KEY
```

`jira.enabled` permite usar la integracion. `jira.auto.create` autoriza la
creacion automatica y solo se aplica a pruebas etiquetadas `jira-candidate`.
Ambas propiedades permanecen en `false` por defecto.

## Ejecucion

Desde la raiz del proyecto:

```powershell
mvn clean test
```

Maven ejecuta la suite definida en `testing.xml`. Los reportes, capturas y archivos generados se guardan en directorios ignorados por Git, como `target/` y `test-output/`.

La suite contempla Chrome, Firefox y Edge, en ese orden y sin paralelismo. Antes de
abrir un navegador, `ExecutionFilter` consulta la hoja `execution` y conserva
solo las combinaciones caso/navegador marcadas con `TRUE`. La configuracion
actual activa solo Chrome, por lo que se esperan 6 ejecuciones. Los otros
navegadores pueden reactivarse desde Excel sin modificar codigo.

Desde Eclipse tambien se puede ejecutar haciendo clic derecho en `testing.xml` y
seleccionando `Run As > TestNG Suite`. Para validar la regresion completa no se
debe ejecutar una clase individual.

Durante la ejecucion no se debe interactuar con las ventanas de los navegadores.
Las capturas identifican el caso y el navegador para conservar la trazabilidad de
la evidencia. El resumen final se consulta en la consola y en el reporte TestNG.

Antes de cada captura de exito, los Page Objects esperan que el documento, las
fuentes, los indicadores de carga y los ciclos de renderizado esten listos. Las
imagenes se conservan sin modificaciones. El reporte TestNG registra por cada
validacion el caso, navegador, valor esperado, valor actual y resultado, sin
incluir passwords.

`EvidenceReporter` no decide si una prueba aprueba o falla: esa responsabilidad
permanece en las aserciones TestNG. El componente agrega trazabilidad a la consola
y al Reporter Output. Si vence solamente la preparacion visual, la prueba conserva
su resultado funcional y la captura queda identificada como `INCOMPLETA`; cuando
la interfaz queda estable se registra como `LISTA`.

## Datos de prueba

`src/test/resources/TestData.xlsx` contiene tres hojas:

- `execution`: una fila por caso. `enabled=FALSE` excluye el caso completo;
  `chrome`, `firefox` y `edge` permiten seleccionar navegadores por separado;
  `dataSheet` indica de que hoja se obtienen los inputs.
- `login`: credenciales de demostracion y resultados esperados de TC01 y TC02.
- `employee`: datos no sensibles, modos de generacion y resultados esperados de
  TC06, TC07 y TC08.
- `session`: seccion protegida y resultados esperados de TC11.

Solo deben utilizarse `TRUE` o `FALSE` en las columnas de control. Los IDs de
caso y los encabezados no deben renombrarse, porque se validan antes de ejecutar.
Los nombres `AUTO` reciben un sufijo de fecha/hora para evitar registros
duplicados; `BLANK` representa un valor intencionalmente vacio.

El libro contiene datos destinados exclusivamente a la instancia publica de
demostracion. No se deben agregar credenciales personales, corporativas, tokens
de Jira ni otros secretos. Las credenciales administrativas y de Jira pertenecen
a la configuracion local ignorada por Git o, en CI, a variables/secretos del
entorno.

## Seguridad

- No confirmar `src/test/resources/config.properties`.
- No imprimir contraseñas ni tokens en la consola.
- No habilitar Jira durante pruebas locales salvo que se quiera crear una incidencia real.
- No activar `jira.auto.create` durante la regresion general.
- Reservar el grupo `jira-candidate` para defectos funcionales que deban evaluarse.
- Revisar `git status` y el contenido preparado antes de cada commit.

## Estructura principal

```text
src/main/java/page_base/       Base reutilizable para Page Objects
src/main/java/page_locators/   Localizadores organizados por pagina
src/main/java/page_objects/    Page Objects de Login y empleados
src/test/java/page_base/       Preparacion y cierre del navegador
src/test/java/page_tests/      Casos automatizados
src/test/java/page_utils/      Configuracion, Excel, Jira y capturas
src/test/resources/            Datos y configuracion local
testing.xml                    Suite de regresion TestNG
```

Los localizadores se almacenan como constantes `By` en `page_locators`. Los Page
Objects consumen esos selectores y conservan las acciones y validaciones; los
tests no acceden directamente a los localizadores.
