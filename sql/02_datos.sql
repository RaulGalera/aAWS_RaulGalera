-- 02_datos.sql
-- Datos ficticios de partida: 10 autores, 20 libros, 8 socios y 15 préstamos.
-- Las fechas se calculan respecto al día de ejecución (CURRENT_DATE), así que
-- el reparto 7 devueltos / 5 activos / 3 vencidos se cumple cualquier día.
-- Ejecutar en la instancia:  sudo mysql biblioteca < ~/sql/02_datos.sql

INSERT INTO autor (id, nombre, nacionalidad, anio_nacimiento) VALUES
  (1,  'Miguel de Cervantes',     'Española',    1547),
  (2,  'Gabriel García Márquez',  'Colombiana',  1927),
  (3,  'Isabel Allende',          'Chilena',     1942),
  (4,  'Carmen Laforet',          'Española',    1921),
  (5,  'Jorge Luis Borges',       'Argentina',   1899),
  (6,  'Miguel Delibes',          'Española',    1920),
  (7,  'Ana María Matute',        'Española',    1925),
  (8,  'Julio Cortázar',          'Argentina',   1914),
  (9,  'Rosa Montero',            'Española',    1951),
  (10, 'Autor Anónimo',           NULL,          NULL);

INSERT INTO libro (id, isbn, titulo, anio_publicacion, genero, ejemplares, autor_id) VALUES
  (1,  '9788420412146', 'Don Quijote de la Mancha',              1605, 'Novela',      3, 1),
  (2,  '9788437604183', 'Novelas ejemplares',                    1613, 'Novela',      2, 1),
  (3,  '9788497592208', 'Cien años de soledad',                  1967, 'Novela',      4, 2),
  (4,  '9788497592451', 'Crónica de una muerte anunciada',       1981, 'Novela',      2, 2),
  (5,  '9788401352836', 'La casa de los espíritus',              1982, 'Novela',      3, 3),
  (6,  '9788401341915', 'Paula',                                 1994, 'Biografía',   1, 3),
  (7,  '9788423342389', 'Nada',                                  1945, 'Novela',      1, 4),
  (8,  '9788420633114', 'Ficciones',                             1944, 'Relato',      2, 5),
  (9,  '9788420633121', 'El Aleph',                              1949, 'Relato',      2, 5),
  (10, '9788423342600', 'Los santos inocentes',                  1981, 'Novela',      2, 6),
  (11, '9788423341047', 'El camino',                             1950, 'Novela',      2, 6),
  (12, '9788423342877', 'Cinco horas con Mario',                 1966, 'Novela',      1, 6),
  (13, '9788423340149', 'Primera memoria',                       1960, 'Novela',      2, 7),
  (14, '9788498384345', 'Olvidado rey Gudú',                     1996, 'Fantasía',    1, 7),
  (15, '9788437604572', 'Rayuela',                               1963, 'Novela',      3, 8),
  (16, '9788437604589', 'Historias de cronopios y de famas',     1962, 'Relato',      2, 8),
  (17, '9788432217753', 'La loca de la casa',                    2003, 'Ensayo',      1, 9),
  (18, '9788432232404', 'Lágrimas en la lluvia',                 2011, 'Ciencia ficción', 2, 9),
  (19, '9788420650388', 'Lazarillo de Tormes',                   1554, 'Novela',      3, 10),
  (20, '9788420650395', 'Cantar de mio Cid',                     1200, 'Poesía',      2, 10);

INSERT INTO socio (id, nombre, email, fecha_alta) VALUES
  (1, 'Lucía Fernández Ruiz',   'lucia.fernandez@example.com', CURRENT_DATE - INTERVAL 400 DAY),
  (2, 'Javier Moreno Sanz',     'javier.moreno@example.com',   CURRENT_DATE - INTERVAL 380 DAY),
  (3, 'Marta Gil Navarro',      'marta.gil@example.com',       CURRENT_DATE - INTERVAL 300 DAY),
  (4, 'Pablo Ortega Ramos',     'pablo.ortega@example.com',    CURRENT_DATE - INTERVAL 250 DAY),
  (5, 'Elena Castro Vidal',     'elena.castro@example.com',    CURRENT_DATE - INTERVAL 200 DAY),
  (6, 'Sergio Blanco Prieto',   'sergio.blanco@example.com',   CURRENT_DATE - INTERVAL 150 DAY),
  (7, 'Nuria Domínguez León',   'nuria.dominguez@example.com', CURRENT_DATE - INTERVAL 120 DAY),
  (8, 'Adrián Herrero Molina',  'adrian.herrero@example.com',  CURRENT_DATE - INTERVAL 90 DAY);

-- 7 DEVUELTOS (tienen fecha_devolucion)
INSERT INTO prestamo (id, libro_id, socio_id, fecha_prestamo, fecha_limite, fecha_devolucion) VALUES
  (1,  1,  1, CURRENT_DATE - INTERVAL 90 DAY, CURRENT_DATE - INTERVAL 76 DAY, CURRENT_DATE - INTERVAL 80 DAY),
  (2,  3,  1, CURRENT_DATE - INTERVAL 70 DAY, CURRENT_DATE - INTERVAL 56 DAY, CURRENT_DATE - INTERVAL 60 DAY),
  (3,  5,  2, CURRENT_DATE - INTERVAL 65 DAY, CURRENT_DATE - INTERVAL 51 DAY, CURRENT_DATE - INTERVAL 50 DAY),
  (4,  8,  3, CURRENT_DATE - INTERVAL 60 DAY, CURRENT_DATE - INTERVAL 46 DAY, CURRENT_DATE - INTERVAL 47 DAY),
  (5,  3,  4, CURRENT_DATE - INTERVAL 50 DAY, CURRENT_DATE - INTERVAL 36 DAY, CURRENT_DATE - INTERVAL 40 DAY),
  (6,  15, 5, CURRENT_DATE - INTERVAL 45 DAY, CURRENT_DATE - INTERVAL 31 DAY, CURRENT_DATE - INTERVAL 30 DAY),
  (7,  1,  6, CURRENT_DATE - INTERVAL 35 DAY, CURRENT_DATE - INTERVAL 21 DAY, CURRENT_DATE - INTERVAL 25 DAY);

-- 5 ACTIVOS (abiertos y con fecha_limite >= hoy)
-- El libro 7 («Nada») tiene 1 ejemplar y queda prestado: sirve para probar el 409 por falta de ejemplares.
INSERT INTO prestamo (id, libro_id, socio_id, fecha_prestamo, fecha_limite, fecha_devolucion) VALUES
  (8,  7,  2, CURRENT_DATE - INTERVAL 5 DAY, CURRENT_DATE + INTERVAL 9 DAY,  NULL),
  (9,  3,  3, CURRENT_DATE - INTERVAL 4 DAY, CURRENT_DATE + INTERVAL 10 DAY, NULL),
  (10, 10, 4, CURRENT_DATE - INTERVAL 3 DAY, CURRENT_DATE + INTERVAL 11 DAY, NULL),
  (11, 15, 5, CURRENT_DATE - INTERVAL 2 DAY, CURRENT_DATE + INTERVAL 12 DAY, NULL),
  (12, 1,  2, CURRENT_DATE - INTERVAL 1 DAY, CURRENT_DATE + INTERVAL 13 DAY, NULL);

-- 3 VENCIDOS (abiertos y con fecha_limite < hoy)
INSERT INTO prestamo (id, libro_id, socio_id, fecha_prestamo, fecha_limite, fecha_devolucion) VALUES
  (13, 5,  6, CURRENT_DATE - INTERVAL 30 DAY, CURRENT_DATE - INTERVAL 16 DAY, NULL),
  (14, 9,  7, CURRENT_DATE - INTERVAL 25 DAY, CURRENT_DATE - INTERVAL 11 DAY, NULL),
  (15, 13, 8, CURRENT_DATE - INTERVAL 20 DAY, CURRENT_DATE - INTERVAL 6 DAY,  NULL);

-- Los ids se han insertado a mano; en MySQL el AUTO_INCREMENT se ajusta solo
-- al siguiente valor libre (en PostgreSQL haría falta setval()).
