# Plan de mejora de las automatizaciones

El objetivo es alinear el proyecto con los casos automatizados TC01, TC02, TC06, TC07 y TC08 documentados para OrangeHRM. Cada fase debe revisarse y aprobarse antes de comenzar la siguiente.

## Alcance del plan

Este plan cubre exclusivamente la automatizacion de TC01, TC02, TC06, TC07 y TC08.
Los casos manuales TC03, TC04, TC05, TC09, TC10 y TC11, asi como la actualizacion
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

Estado: pendiente.

- Añadir el localizador del mensaje Required asociado a First Name.
- Intentar guardar con First Name vacío y Last Name válido.
- Comprobar que no se creó el empleado y que se muestra la validación.
- Guardar la evidencia y ejecutar TC08 de forma aislada.

## Fase 7 Evidencias y regresión completa

Estado: pendiente.

- Unificar nombres de capturas usando el ID de caso.
- Mantener capturas de éxito solicitadas y capturas automáticas de fallos.
- Organizar los casos automatizados en grupos positivo, negativo y automated.
- Ejecutar TC01, TC02, TC06, TC07 y TC08 en Chrome, Firefox y Edge: 15
  ejecuciones en total.
- Ejecutar inicialmente los navegadores de forma secuencial para facilitar el
  diagnostico y evitar interferencias entre drivers.
- Confirmar que Jira permanece desactivado durante la validación.
- Revisar el reporte TestNG final.

## Regla de revisión

Al terminar cada fase se presentarán los archivos modificados, la verificación realizada y cualquier decisión pendiente. La fase siguiente no comenzará hasta recibir aprobación.
