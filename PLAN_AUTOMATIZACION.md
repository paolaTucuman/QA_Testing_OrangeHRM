# Plan de mejora de las automatizaciones

El objetivo es alinear el proyecto con los casos automatizados TC01, TC02, TC06,
TC07, TC08 y TC11 documentados para OrangeHRM. Cada fase debe revisarse y
aprobarse antes de comenzar la siguiente.

## Alcance del plan

Este plan cubre exclusivamente la automatizacion de TC01, TC02, TC06, TC07, TC08
y TC11. Los casos manuales TC03, TC04, TC05, TC09 y TC10, asi como la actualizacion
del informe, la matriz de trazabilidad y las metricas, quedan fuera del trabajo de
codigo y seran realizados por la autora del proyecto.

## Fase 1 Seguridad y control de ejecucion

Estado: completada y subida a GitHub en el commit `b9a7830`.

- Desactivar la creación automática de incidencias Jira por defecto.
- Evitar que la regresión incluya pruebas deliberadamente fallidas.
- Configurar Maven para ejecutar únicamente la suite TestNG declarada.
- Ignorar credenciales, resultados, capturas y archivos generados.
- Crear una plantilla de configuración sin secretos.
- Verificar la estructura de la suite sin abrir el navegador.

## Fase 2 Base estable de Selenium

Estado: completada y subida a GitHub en el commit `b84e820`.

- Sustituir la espera implícita por esperas explícitas.
- Hacer que el inicio de cada prueba falle inmediatamente si Login no carga.
- Centralizar navegación, esperas y cierre seguro del navegador.
- Hacer más segura la captura de evidencias cuando el driver no está disponible.
- Verificar compilación sin ejecutar pruebas funcionales.

## Fase 3 Login TC01 y TC02

Estado: implementada y validada en Chrome el 2026-10-01: 2 pruebas aprobadas,
0 fallidas y 0 omitidas.

- Alinear nombres y descripciones de pruebas con TC01 y TC02.
- Mejorar las validaciones del Dashboard, permanencia en Login y mensaje de error.
- Eliminar duplicaciones entre LoginTest y el flujo Data Driven.
- Ajustar TestData.xlsx para relacionar datos con IDs de caso.
- Ejecutar únicamente las pruebas de Login con Jira desactivado.

## Fase 4 Registro de empleado TC06

Estado: validada funcional y visualmente en Chrome el 2026-10-01: 1 prueba
aprobada, 0 fallidas y 0 omitidas. La evidencia muestra los datos guardados y
el Employee ID sin indicador de carga.

- Generar datos ficticios únicos para evitar duplicados.
- Capturar el Employee ID y los datos realmente guardados.
- Validar nombre, apellido y pantalla de perfil.
- Guardar las evidencias definidas para TC06.
- Ejecutar TC06 de forma aislada.

## Fase 5 Búsqueda de empleado TC07

Estado: validada funcional y visualmente en Chrome el 2026-10-01: 1 prueba
aprobada, 0 fallidas y 0 omitidas. La evidencia muestra una unica fila con el
Employee ID, nombre y apellido creados.

- Añadir acciones de Employee List y Search al Page Object.
- Preparar datos independientes dentro de la prueba.
- Buscar por Employee ID o por un identificador único.
- Validar que el resultado corresponde al empleado creado.
- Guardar la evidencia de búsqueda y ejecutar TC07 de forma aislada.

## Fase 6 Validación de First Name TC08

Estado: validada funcional y visualmente en Chrome el 2026-10-01: 1 prueba
aprobada, 0 fallidas y 0 omitidas. La evidencia muestra First Name vacio, el
mensaje Required y la permanencia en Add Employee.

- Añadir el localizador del mensaje Required asociado a First Name.
- Intentar guardar con First Name vacío y Last Name válido.
- Comprobar que no se creó el empleado y que se muestra la validación.
- Guardar la evidencia y ejecutar TC08 de forma aislada.

## Fase 7 Evidencias y regresión completa

Estado: completada y validada en Chrome, Firefox y Edge el 2026-10-02:
15 pruebas aprobadas, 0 fallidas y 0 omitidas. Se revisaron las 15 capturas con
estado visual `LISTA` y el registro Esperado/Actual de cada validacion.

- Unificar nombres de capturas usando el ID de caso.
- Mantener capturas de éxito solicitadas y capturas automáticas de fallos.
- Organizar los casos automatizados en grupos positivo, negativo y automated.
- Ejecutar TC01, TC02, TC06, TC07 y TC08 en Chrome, Firefox y Edge: 15
  ejecuciones en total.
- Ejecutar inicialmente los navegadores de forma secuencial para facilitar el
  diagnostico y evitar interferencias entre drivers.
- Confirmar que Jira permanece desactivado durante la validación.
- Revisar el reporte TestNG final.

Decision de implementacion: se reutilizan las mismas cinco pruebas y se configura
la matriz de navegadores en `testing.xml`. La ejecucion es secuencial y filtra el
grupo `automated`, de modo que las pruebas auxiliares no formen parte de la
regresion oficial. Las capturas de fallos incluyen tambien el navegador. Tras la
primera ejecucion cross-browser se sustituyo WebDriverManager por Selenium Manager
y se actualizo Selenium para corregir la incompatibilidad con los navegadores
instalados. Las pruebas omitidas muestran ahora su causa en la consola. Las
capturas esperan la desaparicion de loaders, la carga de fuentes y ciclos de
renderizado. El reporte TestNG conserva cada comparacion Esperado/Actual y la ruta
de su captura sin registrar passwords. Un timeout exclusivo de preparacion visual
no modifica el resultado funcional: clasifica la captura como `INCOMPLETA` para
su revision, mientras una captura estable queda como `LISTA`.

## Fase 8 Centralizacion de localizadores

Estado: completada y validada el 2026-10-02 mediante la regresion de 15 pruebas
en Chrome, Firefox y Edge.

- Crear un paquete unico `page_locators` organizado por pagina.
- Guardar selectores como constantes `By`, no como `WebElement` globales.
- Separar localizadores, acciones de Page Objects y escenarios de prueba.
- Centralizar elementos compartidos y selectores dinamicos.
- Confirmar que no queden `@FindBy` o selectores directos fuera del paquete.
- Ejecutar la regresion completa sin modificar datos ni comportamiento.

## Fase 9 Control de ejecucion y datos desde Excel

Estado: implementada y validada el 2026-10-02 mediante la regresion controlada
en Eclipse: 5 pruebas aprobadas, 0 fallidas y 0 omitidas en Chrome.

- Agregar las hojas `execution` y `employee` a `TestData.xlsx`.
- Mantener `login` para los datos de TC01 y TC02.
- Controlar por caso si se ejecuta en Chrome, Firefox y Edge.
- Mover los inputs no sensibles de TC06, TC07 y TC08 al Excel.
- Validar columnas, casos desconocidos y configuraciones incompletas.

Decision de implementacion: TestNG conserva una seccion por navegador para
ejecutar toda la tanda de casos antes de pasar al siguiente. Un
`IMethodInterceptor` consulta la hoja `execution` antes de `@BeforeMethod`, por
lo que las combinaciones desactivadas no abren WebDriver. Las hojas de datos
contienen inputs funcionales y valores esperados; las aserciones TestNG siguen
determinando el resultado. Los secretos no se trasladan al Excel.

## Fase 10 Configuracion local y Jira

Estado: implementada y validada el 2026-10-02. La regresion obtuvo 5 pruebas
aprobadas, 0 fallidas y 0 omitidas en Chrome; TC08 confirmo evidencia visual
`LISTA` despues del ajuste de estabilidad.

- Organizar URL, credenciales OrangeHRM y Jira en `config.properties`.
- Mantener secretos fuera del Excel y del repositorio.
- Actualizar `config.example.properties` con placeholders seguros.
- Documentar que variables de entorno o GitHub Secrets seran la opcion para CI.

Decision de implementacion: las propiedades Java y variables de entorno tienen
prioridad sobre el archivo local. Jira utiliza dos controles independientes:
`jira.enabled` habilita la integracion y `jira.auto.create` autoriza la creacion
automatica. La regresion general no genera incidencias; solo una prueba marcada
como `jira-candidate` podra hacerlo cuando ambos controles esten activos. El tipo
de incidencia predeterminado es `Bug`.

## Fase 11 Logout y sesion cerrada TC11

Estado: ajustada para validacion. La primera ejecucion confirmo que Logout mostro
Login y que la URL protegida no recupero acceso funcional. Falta ejecutar el
alcance ampliado que comprueba tambien la exposicion visual por cache o historial.

- Agregar localizadores del menu de usuario y Logout.
- Iniciar sesion y acceder a una pagina protegida.
- Cerrar sesion y validar que Login vuelva a mostrarse.
- Navegar hacia atras y comprobar inmediatamente que Login siga visible y que no
  se muestre PIM ni informacion protegida almacenada en cache o historial.
- Solicitar directamente la URL protegida y comprobar que vuelva a exigir Login.
- Registrar comparaciones Esperado/Actual y dos evidencias visuales.
- Marcar TC11 como unico candidato a reporte automatico en Jira.
- Ejecutar inicialmente en Chrome desde la matriz de Excel.

Decision de implementacion: TC11 separa dos riesgos. La captura
`TC11_Logout_Back` se toma inmediatamente despues de Back para detectar una
debilidad de cache aunque la pagina no permita operar. Luego se solicita la URL
protegida para validar la invalidacion funcional de la sesion. El caso falla si
se expone PIM en el historial o si se recupera acceso sin credenciales.

## Fase 12 Regresion final y cierre de automatizacion

Estado: pendiente.

- Ejecutar TC01, TC02, TC06, TC07, TC08 y TC11 en Chrome.
- Confirmar 6 pruebas aprobadas, 0 fallidas y 0 omitidas cuando no existan defectos.
- Analizar cualquier fallo antes de crear una incidencia.
- Mantener TC03, TC04, TC05, TC09 y TC10 como pruebas manuales.

## Regla de revisión

Al terminar cada fase se presentarán los archivos modificados, la verificación realizada y cualquier decisión pendiente. La fase siguiente no comenzará hasta recibir aprobación.
