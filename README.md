# Automatizacion web de OrangeHRM

Proyecto de QA para automatizar casos funcionales sobre la instancia publica de demostracion de OrangeHRM. Utiliza Java, Selenium WebDriver, TestNG, Maven y el patron Page Object Model.

## Estado actual

Actualmente el proyecto incluye:

- Login exitoso y login con credenciales invalidas.
- Login dirigido por datos desde `TestData.xlsx`.
- Registro basico de empleados.
- Capturas automaticas ante fallos.
- Integracion opcional con Jira, desactivada por defecto.

El avance y las siguientes fases se encuentran en `PLAN_AUTOMATIZACION.md`.

## Requisitos

- Java 25.
- Maven 3.9 o posterior.
- Google Chrome.
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

## Ejecucion

Desde la raiz del proyecto:

```powershell
mvn clean test
```

Maven ejecuta la suite definida en `testing.xml`. Los reportes, capturas y archivos generados se guardan en directorios ignorados por Git, como `target/` y `test-output/`.

## Datos de prueba

`src/test/resources/TestData.xlsx` contiene datos destinados exclusivamente a la instancia publica de demostracion. No se deben agregar credenciales personales, corporativas ni tokens a ese archivo.

## Seguridad

- No confirmar `src/test/resources/config.properties`.
- No imprimir contraseñas ni tokens en la consola.
- No habilitar Jira durante pruebas locales salvo que se quiera crear una incidencia real.
- Revisar `git status` y el contenido preparado antes de cada commit.

## Estructura principal

```text
src/main/java/page_base/       Base reutilizable para Page Objects
src/main/java/page_objects/    Page Objects de Login y empleados
src/test/java/page_base/       Preparacion y cierre del navegador
src/test/java/page_tests/      Casos automatizados
src/test/java/page_utils/      Configuracion, Excel, Jira y capturas
src/test/resources/            Datos y configuracion local
testing.xml                    Suite de regresion TestNG
```
