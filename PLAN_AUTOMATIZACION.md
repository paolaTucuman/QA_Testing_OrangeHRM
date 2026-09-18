# Plan de mejora de las automatizaciones

El objetivo es alinear el proyecto con los casos automatizados TC01, TC02, TC06, TC07 y TC08 documentados para OrangeHRM. Cada fase debe revisarse y aprobarse antes de comenzar la siguiente.

## Fase 1 Seguridad y control de ejecución

Estado: completada y confirmada en el commit `b9a7830`.

- Desactivar la creación automática de incidencias Jira por defecto.
- Evitar que la regresión incluya pruebas deliberadamente fallidas.
- Configurar Maven para ejecutar únicamente la suite TestNG declarada.
- Ignorar credenciales, resultados, capturas y archivos generados.
- Crear una plantilla de configuración sin secretos.
- Verificar la estructura de la suite sin abrir el navegador.

## Fase 2 Base estable de Selenium

Estado: implementada y pendiente de revisión.

- Sustituir la espera implícita por esperas explícitas.
- Hacer que el inicio de cada prueba falle inmediatamente si Login no carga.
- Centralizar navegación, esperas y cierre seguro del navegador.
- Hacer más segura la captura de evidencias cuando el driver no está disponible.
- Verificar compilación sin ejecutar pruebas funcionales.

## Fase 3 Login TC01 y TC02

- Alinear nombres y descripciones de pruebas con TC01 y TC02.
- Mejorar las validaciones del Dashboard, permanencia en Login y mensaje de error.
- Eliminar duplicaciones entre LoginTest y el flujo Data Driven.
- Ajustar TestData.xlsx para relacionar datos con IDs de caso.
- Ejecutar únicamente las pruebas de Login con Jira desactivado.

## Fase 4 Registro de empleado TC06

- Generar datos ficticios únicos para evitar duplicados.
- Capturar el Employee ID y los datos realmente guardados.
- Validar nombre, apellido y pantalla de perfil.
- Guardar las evidencias definidas para TC06.
- Ejecutar TC06 de forma aislada.

## Fase 5 Búsqueda de empleado TC07

- Añadir acciones de Employee List y Search al Page Object.
- Preparar datos independientes dentro de la prueba.
- Buscar por Employee ID o por un identificador único.
- Validar que el resultado corresponde al empleado creado.
- Guardar la evidencia de búsqueda y ejecutar TC07 de forma aislada.

## Fase 6 Validación de First Name TC08

- Añadir el localizador del mensaje Required asociado a First Name.
- Intentar guardar con First Name vacío y Last Name válido.
- Comprobar que no se creó el empleado y que se muestra la validación.
- Guardar la evidencia y ejecutar TC08 de forma aislada.

## Fase 7 Evidencias y regresión completa

- Unificar nombres de capturas usando el ID de caso.
- Mantener capturas de éxito solicitadas y capturas automáticas de fallos.
- Ejecutar la suite completa TC01, TC02, TC06, TC07 y TC08.
- Confirmar que Jira permanece desactivado durante la validación.
- Revisar el reporte TestNG final.

## Fase 8 Actualización de documentación

- Incorporar resultados obtenidos, estado y evidencias reales.
- Actualizar la matriz de trazabilidad si cambia la organización de las pruebas.
- Eliminar la página final vacía.
- Generar una versión final revisada del documento.

## Regla de revisión

Al terminar cada fase se presentarán los archivos modificados, la verificación realizada y cualquier decisión pendiente. La fase siguiente no comenzará hasta recibir aprobación.
