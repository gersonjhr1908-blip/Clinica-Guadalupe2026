# Backend Clínica Guadalupe

Requiere Java 21 y MySQL con la base `clinica_guadalupe` en localhost:3306.
Antes de iniciar, configura las variables de entorno:

- `DB_PASSWORD`: contraseña de MySQL (usuario configurado: `root`).
- `JWT_SECRET`: clave privada de firma JWT, de al menos 32 caracteres, generada para tu entorno.

Iniciar: `mvnw.cmd spring-boot:run`.
Ejecutar pruebas: `mvnw.cmd test`.
La prueba `contextLoads` requiere una conexión válida a MySQL.
Los scripts en `database/` incluyen datos de desarrollo.
