# aAWS_RaulGalera

Práctica aAWS (Acceso a Datos, 2º DAM): API REST *biblioteca* con Spring Boot + MySQL desplegada en una EC2 de AWS.

**Variante 3:** máximo 5 préstamos abiertos, 30 días de préstamo, informe `GET /api/libros/{id}/historial`.

## Estructura

| Carpeta | Contenido |
|---|---|
| `biblioteca-api/` | Proyecto Spring Boot |
| `sql/` | Scripts de esquema, datos y verificación (sin contraseñas) |
| `infra/` | Configuración de MySQL (`.cnf`), unidad `biblioteca.service`, plantilla `.env` y scripts |

> Las contraseñas y la clave `labsuser.pem` no se suben nunca (ver `.gitignore`).
