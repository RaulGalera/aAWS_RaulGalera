-- 04_verificacion.sql
-- Comprueba el estado de la base de datos.
-- Ejecutar en la instancia:  sudo mysql biblioteca < ~/sql/04_verificacion.sql
-- Esperado tras cargar 02_datos.sql: 10 autores, 20 libros, 8 socios y
-- 15 préstamos (7 devueltos, 5 activos y 3 vencidos).

SELECT
  (SELECT COUNT(*) FROM autor)    AS autores,
  (SELECT COUNT(*) FROM libro)    AS libros,
  (SELECT COUNT(*) FROM socio)    AS socios,
  (SELECT COUNT(*) FROM prestamo) AS prestamos;

-- Reparto de préstamos por estado (mismas definiciones que la API)
SELECT
  CASE
    WHEN fecha_devolucion IS NOT NULL    THEN 'DEVUELTO'
    WHEN fecha_limite < CURRENT_DATE     THEN 'VENCIDO'
    ELSE                                      'ACTIVO'
  END      AS estado,
  COUNT(*) AS total
FROM prestamo
GROUP BY estado
ORDER BY estado;

-- Ejemplares disponibles por libro (ejemplares - préstamos abiertos); nunca debe ser negativo
SELECT l.id, l.titulo, l.ejemplares,
       l.ejemplares - COUNT(p.id) AS disponibles
FROM libro l
LEFT JOIN prestamo p ON p.libro_id = l.id AND p.fecha_devolucion IS NULL
GROUP BY l.id, l.titulo, l.ejemplares
ORDER BY l.id;

-- Cuentas creadas y sus privilegios
SELECT user, host, plugin FROM mysql.user WHERE user LIKE 'biblioteca%';
SHOW GRANTS FOR 'biblioteca_app'@'localhost';
SHOW GRANTS FOR 'biblioteca_admin'@'localhost';
