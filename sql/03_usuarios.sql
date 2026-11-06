-- 03_usuarios.sql
-- Crea las dos cuentas de MySQL con privilegio mínimo.
-- ANTES DE EJECUTARLO sustituye los dos marcadores por contraseñas propias de
-- al menos 12 caracteres. Nunca subas al repositorio la versión con contraseñas.
-- Ejecutar en la instancia:
--   nano ~/sql/03_usuarios.sql
--   sudo mysql < ~/sql/03_usuarios.sql
--   shred -u ~/sql/03_usuarios.sql

-- Cuenta de la API: solo puede leer y modificar datos (no DROP, no CREATE).
DROP USER IF EXISTS 'biblioteca_app'@'localhost';
CREATE USER 'biblioteca_app'@'localhost' IDENTIFIED BY '<CONTRASENA_APP>';
GRANT SELECT, INSERT, UPDATE, DELETE ON biblioteca.* TO 'biblioteca_app'@'localhost';

-- Cuenta de administración: todos los privilegios, pero solo sobre el esquema biblioteca.
DROP USER IF EXISTS 'biblioteca_admin'@'localhost';
CREATE USER 'biblioteca_admin'@'localhost' IDENTIFIED BY '<CONTRASENA_ADMIN>';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'biblioteca_admin'@'localhost';

FLUSH PRIVILEGES;
