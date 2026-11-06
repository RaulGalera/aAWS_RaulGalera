-- 01_esquema.sql
-- Crea la base de datos `biblioteca` y sus tablas (MySQL 8.0).
-- Ejecutar en la instancia:  sudo mysql < ~/sql/01_esquema.sql
-- Es idempotente: borra la base de datos si ya existía.

DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE biblioteca;

CREATE TABLE autor (
  id               INT          NOT NULL AUTO_INCREMENT,
  nombre           VARCHAR(120) NOT NULL,
  nacionalidad     VARCHAR(60)  NULL,
  anio_nacimiento  SMALLINT     NULL,
  CONSTRAINT pk_autor PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE libro (
  id                INT          NOT NULL AUTO_INCREMENT,
  isbn              CHAR(13)     NOT NULL,
  titulo            VARCHAR(200) NOT NULL,
  anio_publicacion  SMALLINT     NULL,
  genero            VARCHAR(40)  NOT NULL,
  ejemplares        SMALLINT     NOT NULL DEFAULT 1,
  autor_id          INT          NOT NULL,
  CONSTRAINT pk_libro PRIMARY KEY (id),
  CONSTRAINT uq_libro_isbn UNIQUE (isbn),
  CONSTRAINT ck_libro_ejemplares CHECK (ejemplares >= 0),
  CONSTRAINT fk_libro_autor FOREIGN KEY (autor_id) REFERENCES autor (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE socio (
  id          INT          NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(120) NOT NULL,
  email       VARCHAR(160) NOT NULL,
  fecha_alta  DATE         NOT NULL,
  CONSTRAINT pk_socio PRIMARY KEY (id),
  CONSTRAINT uq_socio_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE prestamo (
  id                INT   NOT NULL AUTO_INCREMENT,
  libro_id          INT   NOT NULL,
  socio_id          INT   NOT NULL,
  fecha_prestamo    DATE  NOT NULL,
  fecha_limite      DATE  NOT NULL,
  fecha_devolucion  DATE  NULL,          -- NULL = préstamo abierto
  CONSTRAINT pk_prestamo PRIMARY KEY (id),
  CONSTRAINT ck_prestamo_limite CHECK (fecha_limite >= fecha_prestamo),
  CONSTRAINT ck_prestamo_devolucion CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo),
  CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libro (id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_prestamo_socio FOREIGN KEY (socio_id) REFERENCES socio (id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Índices para las consultas más frecuentes (préstamos abiertos por libro y por socio)
CREATE INDEX ix_prestamo_libro_abierto ON prestamo (libro_id, fecha_devolucion);
CREATE INDEX ix_prestamo_socio_abierto ON prestamo (socio_id, fecha_devolucion);
CREATE INDEX ix_libro_genero ON libro (genero);
