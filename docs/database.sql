-- ============================================================================
-- Little Horizons ERP - Complete Database Script
-- Educational Resource Planning System
-- Motor: MySQL 8.0 or higher (REQUIRED for CHECK constraints)
-- ============================================================================

-- ============================================================================
-- SECTION 1: DDL (Data Definition Language)
-- ============================================================================

DROP DATABASE IF EXISTS erp_little_horizons;
CREATE DATABASE erp_little_horizons CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_little_horizons;
SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. MODULE: STUDENTS AND GUARDIANS
-- ----------------------------------------------------------------------------

CREATE TABLE grados (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_grado VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: Preescolar, Primero, Segundo, ... Once',
    nivel_numerico INT NOT NULL UNIQUE COMMENT 'Nivel numérico para ordenamiento (0 para preescolar, 1-11 para grados)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Catálogo de grados académicos del colegio';

CREATE TABLE estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('TI', 'CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'TI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero ENUM('M', 'F', 'Otro') NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    tipo_sangre VARCHAR(5),
    alergias TEXT,
    condiciones_medicas TEXT,
    fecha_ingreso DATE NOT NULL,
    estado ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido, primer_nombre)
) ENGINE=InnoDB COMMENT='Información completa de estudiantes';

CREATE TABLE acudientes (
    id_acudiente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    parentesco VARCHAR(50) NOT NULL COMMENT 'Padre, Madre, Abuelo, Tío, Tutor Legal, etc.',
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    ocupacion VARCHAR(100),
    es_responsable_financiero BOOLEAN DEFAULT TRUE COMMENT 'Indica si es responsable de pagos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Acudientes y tutores legales';

CREATE TABLE estudiante_acudiente (
    id_estudiante INT NOT NULL,
    id_acudiente INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Acudiente principal para comunicaciones',
    es_contacto_emergencia BOOLEAN DEFAULT TRUE COMMENT 'Disponible para emergencias',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_acudiente),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_acudiente (id_acudiente)
) ENGINE=InnoDB COMMENT='Relación entre estudiantes y sus acudientes';

CREATE TABLE matriculas (
    id_matricula INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grado INT NOT NULL,
    año_academico INT NOT NULL COMMENT 'Año académico (ej: 2026)',
    estado_matricula ENUM('Activa', 'Completada', 'Retirada') DEFAULT 'Activa',
    fecha_matricula DATE NOT NULL,
    fecha_retiro DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    UNIQUE KEY matricula_unica (id_estudiante, año_academico),
    INDEX idx_año_grado (año_academico, id_grado),
    INDEX idx_estado (estado_matricula)
) ENGINE=InnoDB COMMENT='Historial de matrículas de estudiantes por año y grado';

-- ----------------------------------------------------------------------------
-- 2. MODULE: ACADEMIC
-- ----------------------------------------------------------------------------

CREATE TABLE materias (
    id_materia INT AUTO_INCREMENT PRIMARY KEY,
    codigo_materia VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único de la materia (ej: MAT-5, ESP-3)',
    nombre_materia VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_grado INT NOT NULL COMMENT 'Grado al que pertenece la materia',
    horas_semanales INT DEFAULT 2 COMMENT 'Horas semanales asignadas',
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    INDEX idx_grado (id_grado),
    INDEX idx_activa (esta_activa),
    INDEX idx_codigo (codigo_materia)
) ENGINE=InnoDB COMMENT='Catálogo de materias por grado';

CREATE TABLE profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    especializacion VARCHAR(100) COMMENT 'Área de especialización (Matemáticas, Español, etc.)',
    fecha_contratacion DATE NOT NULL,
    estado ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Información de profesores';

-- Enhanced with basic day/time schedule columns to prevent scheduling conflicts
CREATE TABLE asignaciones_profesor (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    seccion VARCHAR(10) COMMENT 'Sección o grupo (A, B, C)',
    dia_semana ENUM('Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado') COMMENT 'Día de clase asignado',
    hora_inicio TIME COMMENT 'Hora de inicio de la clase',
    hora_fin TIME COMMENT 'Hora de fin de la clase',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY asignacion_unica (id_profesor, id_materia, año_academico, seccion),
    INDEX idx_profesor_año (id_profesor, año_academico),
    INDEX idx_materia_año (id_materia, año_academico)
) ENGINE=InnoDB COMMENT='Asignación de profesores a materias por año académico y horario';

CREATE TABLE inscripciones_materia (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV') NOT NULL COMMENT 'Período académico',
    seccion VARCHAR(10) COMMENT 'Sección o grupo asignado',
    fecha_inscripcion DATE NOT NULL,
    estado ENUM('Activa', 'Retirada', 'Completada') DEFAULT 'Activa',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY inscripcion_unica (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico)
) ENGINE=InnoDB COMMENT='Inscripción de estudiantes en materias específicas';

CREATE TABLE registros_academicos (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    id_profesor INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    valor_nota DECIMAL(2,1) NOT NULL COMMENT 'Nota de 0.0 a 5.0 según estándar colombiano',
    observaciones TEXT,
    fecha_registro DATE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor),
    CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0),
    UNIQUE KEY registro_unico (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico),
    INDEX idx_valor_nota (valor_nota)
) ENGINE=InnoDB COMMENT='Registro de calificaciones de estudiantes';

CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    estado_asistencia ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    observaciones TEXT,
    registrado_por INT COMMENT 'ID del profesor que registró la asistencia',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor),
    UNIQUE KEY asistencia_unica (id_estudiante, id_materia, fecha_asistencia),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_asistencia),
    INDEX idx_materia_fecha (id_materia, fecha_asistencia),
    INDEX idx_estado (estado_asistencia)
) ENGINE=InnoDB COMMENT='Registro de asistencia diaria de estudiantes';

-- ----------------------------------------------------------------------------
-- 3. MODULE: EXTRACURRICULAR ACTIVITIES
-- ----------------------------------------------------------------------------

CREATE TABLE clubes (
    id_club INT AUTO_INCREMENT PRIMARY KEY,
    nombre_club VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_profesor_supervisor INT COMMENT 'Profesor encargado del club',
    capacidad_maxima INT DEFAULT 30 COMMENT 'Cupo máximo de estudiantes',
    horario_reunion VARCHAR(100) COMMENT 'Días y horario de reunión (ej: Martes 3-5 PM)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor),
    INDEX idx_activo (esta_activo),
    INDEX idx_nombre (nombre_club)
) ENGINE=InnoDB COMMENT='Catálogo de clubes extracurriculares';

CREATE TABLE membresias_club (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_club INT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estado_membresia ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    fecha_retiro DATE,
    motivo_retiro TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_club) REFERENCES clubes(id_club) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_club_estado (id_club, estado_membresia)
) ENGINE=InnoDB COMMENT='Membresías de estudiantes en clubes';

CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_evento VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_evento ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    fecha_evento DATE NOT NULL,
    lugar VARCHAR(150),
    tiene_competencia BOOLEAN DEFAULT FALSE COMMENT 'Indica si el evento tiene competencia con posiciones',
    id_organizador INT COMMENT 'Profesor organizador del evento',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor),
    INDEX idx_fecha (fecha_evento),
    INDEX idx_tipo (tipo_evento),
    INDEX idx_activo (esta_activo)
) ENGINE=InnoDB COMMENT='Catálogo de eventos del colegio';

CREATE TABLE participantes_evento (
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    id_estudiante INT NOT NULL,
    rol_participacion ENUM('Participante', 'Competidor', 'Organizador', 'Asistente') DEFAULT 'Participante',
    posicion_obtenida VARCHAR(20) COMMENT 'Posición obtenida si hay competencia (1er Lugar, 2do Lugar, 3er Lugar, Participante)',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    UNIQUE KEY participacion_unica (id_evento, id_estudiante),
    INDEX idx_evento (id_evento),
    INDEX idx_estudiante (id_estudiante)
) ENGINE=InnoDB COMMENT='Registro de participación de estudiantes en eventos';

-- ----------------------------------------------------------------------------
-- 4. MODULE: FINANCIAL
-- ----------------------------------------------------------------------------

CREATE TABLE conceptos_pago (
    id_concepto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_concepto VARCHAR(20) NOT NULL UNIQUE,
    nombre_concepto VARCHAR(100) NOT NULL,
    descripcion TEXT,
    monto_base DECIMAL(10,2) NOT NULL COMMENT 'Valor base del concepto en pesos colombianos',
    es_recurrente BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un pago recurrente (ej: pensión mensual)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (esta_activo),
    INDEX idx_codigo (codigo_concepto)
) ENGINE=InnoDB COMMENT='Catálogo de conceptos de pago del colegio';

-- Enhanced table to support student-specific fee customization (sibling discounts, scholarships)
CREATE TABLE tarifas_estudiante (
    id_tarifa INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_concepto INT NOT NULL,
    monto_personalizado DECIMAL(10,2) NOT NULL COMMENT 'Valor ajustado para este estudiante',
    porcentaje_descuento DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Porcentaje de descuento aplicado',
    motivo VARCHAR(150) COMMENT 'Ej: Descuento por hermanos, Beca académica',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto) ON DELETE CASCADE,
    UNIQUE KEY tarifa_unica (id_estudiante, id_concepto),
    CHECK (monto_personalizado >= 0.00),
    CHECK (porcentaje_descuento >= 0.00 AND porcentaje_descuento <= 100.00)
) ENGINE=InnoDB COMMENT='Tarifas y descuentos personalizados por estudiante';

CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_acudiente INT COMMENT 'Acudiente que realizó el pago',
    id_concepto INT NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    numero_recibo VARCHAR(50) UNIQUE,
    año_academico INT COMMENT 'Año al que aplica el pago',
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Anual') COMMENT 'Período al que aplica el pago',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente),
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_pago),
    INDEX idx_concepto (id_concepto),
    INDEX idx_fecha (fecha_pago),
    INDEX idx_recibo (numero_recibo)
) ENGINE=InnoDB COMMENT='Registro de todos los pagos realizados';

CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE COMMENT 'Número consecutivo de factura',
    id_pago INT NOT NULL COMMENT 'Pago asociado a esta factura',
    fecha_emision DATE NOT NULL,
    cufe VARCHAR(100) COMMENT 'Código Único de Factura Electrónica (simulado)',
    nombre_archivo_txt VARCHAR(255) COMMENT 'Nombre del archivo TXT generado',
    ruta_archivo VARCHAR(500) COMMENT 'Ruta completa donde se guardó la factura TXT',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE CASCADE,
    UNIQUE KEY factura_pago_unica (id_pago),
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha_emision (fecha_emision),
    INDEX idx_pago (id_pago)
) ENGINE=InnoDB COMMENT='Registro de facturas electrónicas TXT generadas';

-- ----------------------------------------------------------------------------
-- 5. MODULE: RESOURCE MANAGEMENT
-- ----------------------------------------------------------------------------

CREATE TABLE categorias_recurso (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Categorías de recursos (Libro, Equipo de Laboratorio, etc.)';

CREATE TABLE recursos (
    id_recurso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_recurso VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código de inventario único',
    nombre_recurso VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion DATE,
    ubicacion_fisica VARCHAR(100) COMMENT 'Ubicación física (Biblioteca, Laboratorio 1, etc.)',
    estado_recurso ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') DEFAULT 'Disponible',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_recurso(id_categoria),
    INDEX idx_codigo (codigo_recurso),
    INDEX idx_estado (estado_recurso),
    INDEX idx_categoria (id_categoria),
    INDEX idx_nombre (nombre_recurso)
) ENGINE=InnoDB COMMENT='Inventario de recursos prestables del colegio';

CREATE TABLE prestamos_estudiante (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_estudiante INT NOT NULL COMMENT 'Estudiante que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a estudiantes';

CREATE TABLE prestamos_profesor (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_profesor INT NOT NULL COMMENT 'Profesor que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_profesor (id_profesor),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a profesores';

-- ============================================================================
-- SECTION 2: DML (Data Manipulation Language - Coherent Seed Data >= 30 rows)
-- ============================================================================

-- 1. Grados (12 rows)
INSERT INTO grados (nombre_grado, nivel_numerico) VALUES
('Preescolar', 0), ('Primero', 1), ('Segundo', 2), ('Tercero', 3),
('Cuarto', 4), ('Quinto', 5), ('Sexto', 6), ('Séptimo', 7),
('Octavo', 8), ('Noveno', 9), ('Décimo', 10), ('Once', 11);

-- 2. Estudiantes (35 rows)
INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) VALUES
('TI', '1032456001', 'Sofía', 'Valentina', 'Gómez', 'Restrepo', '2020-03-15', 'F', 'Calle 45 #12-30, Bogotá', '3102345001', 'sofia.gomez@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2025-01-20', 'Activo'),
('TI', '1032456002', 'Mateo', 'Alejandro', 'Rodríguez', 'Pérez', '2019-06-22', 'M', 'Carrera 15 #78-12, Bogotá', '3102345002', 'mateo.rodriguez@littlehorizons.edu.co', 'A+', 'Polen', 'Ninguna', '2024-01-22', 'Activo'),
('TI', '1032456003', 'Luciana', 'María', 'Martínez', 'López', '2018-09-10', 'F', 'Av. Boyacá #63-40, Bogotá', '3102345003', 'luciana.martinez@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2023-01-25', 'Activo'),
('TI', '1032456004', 'Santiago', 'David', 'Hernández', 'Castro', '2017-01-18', 'M', 'Calle 127 #54-20, Bogotá', '3102345004', 'santiago.hernandez@littlehorizons.edu.co', 'O-', 'Maní', 'Asma leve', '2022-01-24', 'Activo'),
('TI', '1032456005', 'Isabella', 'Camila', 'García', 'Moreno', '2016-05-04', 'F', 'Carrera 7 #92-15, Bogotá', '3102345005', 'isabella.garcia@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2021-01-25', 'Activo'),
('TI', '1032456006', 'Sebastián', 'Andrés', 'Díaz', 'Vargas', '2015-11-28', 'M', 'Calle 80 #24-18, Bogotá', '3102345006', 'sebastian.diaz@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
('TI', '1032456007', 'Valeria', 'Sofía', 'Sánchez', 'Ruiz', '2014-07-12', 'F', 'Diagonal 40 #18-22, Bogotá', '3102345007', 'valeria.sanchez@littlehorizons.edu.co', 'AB+', 'Mariscos', 'Ninguna', '2020-01-20', 'Activo'),
('TI', '1032456008', 'Emiliano', 'José', 'Ramírez', 'Mejía', '2013-04-09', 'M', 'Carrera 30 #45-60, Bogotá', '3102345008', 'emiliano.ramirez@littlehorizons.edu.co', 'A-', 'Ninguna', 'Ninguna', '2019-01-21', 'Activo'),
('TI', '1032456009', 'Mariana', 'Lucía', 'Torres', 'Salazar', '2012-08-19', 'F', 'Calle 100 #19-45, Bogotá', '3102345009', 'mariana.torres@littlehorizons.edu.co', 'O+', 'Penicilina', 'Ninguna', '2018-01-22', 'Activo'),
('TI', '1032456010', 'Nicolás', 'Esteban', 'Flores', 'Ortega', '2011-02-27', 'M', 'Carrera 68 #32-11, Bogotá', '3102345010', 'nicolas.flores@littlehorizons.edu.co', 'B-', 'Ninguna', 'Miopía', '2017-01-23', 'Activo'),
('TI', '1032456011', 'Gabriela', 'Andrea', 'Vega', 'Mendoza', '2010-10-14', 'F', 'Calle 116 #48-30, Bogotá', '3102345011', 'gabriela.vega@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2017-01-23', 'Activo'),
('CC', '1032456012', 'Juan', 'Pablo', 'Gómez', 'Restrepo', '2009-12-03', 'M', 'Calle 45 #12-30, Bogotá', '3102345012', 'juan.gomez@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2016-01-25', 'Activo'),
('TI', '1032456013', 'Camila', 'Fernanda', 'Rojas', 'Pineda', '2016-08-21', 'F', 'Calle 53 #22-14, Bogotá', '3102345013', 'camila.rojas@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2022-01-24', 'Activo'),
('TI', '1032456014', 'Tomás', 'Felipe', 'Reyes', 'Cardona', '2015-03-11', 'M', 'Carrera 11 #84-22, Bogotá', '3102345014', 'tomas.reyes@littlehorizons.edu.co', 'O+', 'Polvo', 'Ninguna', '2021-01-25', 'Activo'),
('TI', '1032456015', 'Sara', 'Isabel', 'Jiménez', 'Cárdenas', '2014-09-25', 'F', 'Calle 72 #10-34, Bogotá', '3102345015', 'sara.jimenez@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
('TI', '1032456016', 'Samuel', 'Enrique', 'Moreno', 'Gutiérrez', '2013-06-17', 'M', 'Av. Suba #104-50, Bogotá', '3102345016', 'samuel.moreno@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2019-01-21', 'Activo'),
('TI', '1032456017', 'Antonella', 'Victoria', 'Álvarez', 'Pardo', '2012-11-05', 'F', 'Carrera 50 #26-40, Bogotá', '3102345017', 'antonella.alvarez@littlehorizons.edu.co', 'O+', 'Látex', 'Ninguna', '2018-01-22', 'Activo'),
('TI', '1032456018', 'Jerónimo', 'Miguel', 'Suárez', 'Quintero', '2011-04-30', 'M', 'Calle 140 #13-25, Bogotá', '3102345018', 'jeronimo.suarez@littlehorizons.edu.co', 'O-', 'Ninguna', 'Ninguna', '2017-01-23', 'Activo'),
('TI', '1032456019', 'Salomé', 'Alejandra', 'Navarro', 'Rincón', '2010-07-08', 'F', 'Carrera 9 #67-18, Bogotá', '3102345019', 'salome.navarro@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2016-01-25', 'Activo'),
('CC', '1032456020', 'Daniel', 'Mauricio', 'Herrera', 'Peña', '2009-05-19', 'M', 'Calle 134 #45-12, Bogotá', '3102345020', 'daniel.herrera@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2015-01-26', 'Activo'),
('TI', '1032456021', 'Victoria', 'Elena', 'Mendoza', 'Acosta', '2018-02-14', 'F', 'Calle 60 #8-21, Bogotá', '3102345021', 'victoria.mendoza@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2023-01-25', 'Activo'),
('TI', '1032456022', 'Martín', 'Eduardo', 'Castaño', 'Silva', '2017-10-29', 'M', 'Carrera 19 #108-14, Bogotá', '3102345022', 'martin.castano@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2022-01-24', 'Activo'),
('TI', '1032456023', 'Paula', 'Andrea', 'Ortega', 'Molina', '2016-01-07', 'F', 'Calle 85 #16-33, Bogotá', '3102345023', 'paula.ortega@littlehorizons.edu.co', 'O+', 'Nueces', 'Ninguna', '2021-01-25', 'Activo'),
('TI', '1032456024', 'Alejandro', 'Rafael', 'Parra', 'Bautista', '2015-07-23', 'M', 'Av. 19 #122-45, Bogotá', '3102345024', 'alejandro.parra@littlehorizons.edu.co', 'AB-', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
('TI', '1032456025', 'Laura', 'Daniela', 'Cruz', 'Velasco', '2014-03-16', 'F', 'Carrera 24 #51-19, Bogotá', '3102345025', 'laura.cruz@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
('TI', '1032456026', 'Simón', 'Gabriel', 'Patiño', 'Arias', '2013-09-02', 'M', 'Calle 147 #18-40, Bogotá', '3102345026', 'simon.patino@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2019-01-21', 'Activo'),
('TI', '1032456027', 'Manuela', 'Catalina', 'Guerrero', 'León', '2012-05-12', 'F', 'Carrera 7 #112-30, Bogotá', '3102345027', 'manuela.guerrero@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2018-01-22', 'Activo'),
('TI', '1032456028', 'Esteban', 'Julián', 'Vargas', 'Salinas', '2011-08-26', 'M', 'Calle 95 #14-28, Bogotá', '3102345028', 'esteban.vargas@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2017-01-23', 'Activo'),
('TI', '1032456029', 'Juliana', 'Patricia', 'Cortés', 'Bermúdez', '2010-11-18', 'F', 'Carrera 45 #103-15, Bogotá', '3102345029', 'juliana.cortes@littlehorizons.edu.co', 'A-', 'Ninguna', 'Ninguna', '2016-01-25', 'Activo'),
('CC', '1032456030', 'Andrés', 'Felipe', 'Rincón', 'Osorio', '2009-02-09', 'M', 'Calle 125 #21-50, Bogotá', '3102345030', 'andres.rincon@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2015-01-26', 'Activo'),
('TI', '1032456031', 'Elena', 'Beatriz', 'Rodríguez', 'Pérez', '2015-12-10', 'F', 'Carrera 15 #78-12, Bogotá', '3102345031', 'elena.rodriguez@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2021-01-25', 'Activo'),
('TI', '1032456032', 'Lucas', 'Matías', 'Martínez', 'López', '2014-04-21', 'M', 'Av. Boyacá #63-40, Bogotá', '3102345032', 'lucas.martinez@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
('TI', '1032456033', 'Emma', 'Sofía', 'Lozano', 'Duarte', '2019-11-03', 'F', 'Calle 68 #12-19, Bogotá', '3102345033', 'emma.lozano@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-22', 'Activo'),
('TI', '1032456034', 'Joaquín', 'David', 'Medina', 'Franco', '2013-02-28', 'M', 'Carrera 13 #55-24, Bogotá', '3102345034', 'joaquin.medina@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2019-01-21', 'Activo'),
('CE', '9001234035', 'Chloe', 'Marie', 'Dupont', 'Laurent', '2011-06-15', 'F', 'Calle 90 #11-44, Bogotá', '3102345035', 'chloe.dupont@littlehorizons.edu.co', 'O-', 'Ninguna', 'Ninguna', '2023-07-10', 'Activo');

-- 3. Acudientes (32 rows)
INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES
('CC', '52110001', 'Carolina', 'Andrea', 'Restrepo', 'Giraldo', 'Madre', '3001110001', '6012340001', 'carolina.restrepo@gmail.com', 'Calle 45 #12-30, Bogotá', 'Arquitecta', TRUE),
('CC', '79220002', 'Carlos', 'Eduardo', 'Rodríguez', 'Nieto', 'Padre', '3001110002', '6012340002', 'carlos.rodriguez@gmail.com', 'Carrera 15 #78-12, Bogotá', 'Ingeniero Civil', TRUE),
('CC', '52330003', 'Claudia', 'Patricia', 'López', 'Gómez', 'Madre', '3001110003', NULL, 'claudia.lopez@gmail.com', 'Av. Boyacá #63-40, Bogotá', 'Contadora Pública', TRUE),
('CC', '80440004', 'Fernando', 'José', 'Hernández', 'Ruiz', 'Padre', '3001110004', '6012340004', 'fernando.hernandez@gmail.com', 'Calle 127 #54-20, Bogotá', 'Médico Cirujano', TRUE),
('CC', '52550005', 'Mónica', 'Lucía', 'Moreno', 'Díaz', 'Madre', '3001110005', NULL, 'monica.moreno@gmail.com', 'Carrera 7 #92-15, Bogotá', 'Abogada', TRUE),
('CC', '79660006', 'Ricardo', 'Alfonso', 'Díaz', 'Pardo', 'Padre', '3001110006', NULL, 'ricardo.diaz@gmail.com', 'Calle 80 #24-18, Bogotá', 'Empresario', TRUE),
('CC', '52770007', 'Adriana', 'Marcela', 'Ruiz', 'Salazar', 'Madre', '3001110007', '6012340007', 'adriana.ruiz@gmail.com', 'Diagonal 40 #18-22, Bogotá', 'Psicóloga', TRUE),
('CC', '80880008', 'Jorge', 'Enrique', 'Ramírez', 'Cano', 'Padre', '3001110008', NULL, 'jorge.ramirez@gmail.com', 'Carrera 30 #45-60, Bogotá', 'Economista', TRUE),
('CC', '52990009', 'Paola', 'Cristina', 'Salazar', 'Toro', 'Madre', '3001110009', NULL, 'paola.salazar@gmail.com', 'Calle 100 #19-45, Bogotá', 'Diseñadora Industrial', TRUE),
('CC', '79100010', 'Mauricio', 'Andrés', 'Flores', 'Vega', 'Padre', '3001110010', '6012340010', 'mauricio.flores@gmail.com', 'Carrera 68 #32-11, Bogotá', 'Ingeniero de Sistemas', TRUE),
('CC', '52110011', 'Diana', 'Milena', 'Mendoza', 'Cárdenas', 'Madre', '3001110011', NULL, 'diana.mendoza@gmail.com', 'Calle 116 #48-30, Bogotá', 'Administradora de Empresas', TRUE),
('CC', '79220012', 'Gustavo', 'Adolfo', 'Gómez', 'Vélez', 'Padre', '3001110012', NULL, 'gustavo.gomez@gmail.com', 'Calle 45 #12-30, Bogotá', 'Comerciante', FALSE),
('CC', '52330013', 'Natalia', 'Esperanza', 'Pineda', 'Rojas', 'Madre', '3001110013', NULL, 'natalia.pineda@gmail.com', 'Calle 53 #22-14, Bogotá', 'Odontóloga', TRUE),
('CC', '80440014', 'Alejandro', 'Javier', 'Reyes', 'Serrano', 'Padre', '3001110014', '6012340014', 'alejandro.reyes@gmail.com', 'Carrera 11 #84-22, Bogotá', 'Gerente Comercial', TRUE),
('CC', '52550015', 'Beatriz', 'Helena', 'Cárdenas', 'Soto', 'Madre', '3001110015', NULL, 'beatriz.cardenas@gmail.com', 'Calle 72 #10-34, Bogotá', 'Profesora Universitaria', TRUE),
('CC', '79660016', 'Héctor', 'Fabio', 'Moreno', 'Beltrán', 'Padre', '3001110016', NULL, 'hector.moreno@gmail.com', 'Av. Suba #104-50, Bogotá', 'Piloto Comercial', TRUE),
('CC', '52770017', 'Liliana', 'Rocío', 'Pardo', 'Arias', 'Madre', '3001110017', NULL, 'liliana.pardo@gmail.com', 'Carrera 50 #26-40, Bogotá', 'Química Farmacéutica', TRUE),
('CC', '80880018', 'Óscar', 'Iván', 'Suárez', 'Mejía', 'Padre', '3001110018', '6012340018', 'oscar.suarez@gmail.com', 'Calle 140 #13-25, Bogotá', 'Consultor Financiero', TRUE),
('CC', '52990019', 'Sandra', 'Milena', 'Rincón', 'Pérez', 'Madre', '3001110019', NULL, 'sandra.rincon@gmail.com', 'Carrera 9 #67-18, Bogotá', 'Bacterióloga', TRUE),
('CC', '79100020', 'Hernán', 'Darío', 'Herrera', 'Lagos', 'Padre', '3001110020', NULL, 'hernan.herrera@gmail.com', 'Calle 134 #45-12, Bogotá', 'Ingeniero Industrial', TRUE),
('CC', '52110021', 'Martha', 'Cecilia', 'Acosta', 'Reyes', 'Madre', '3001110021', NULL, 'martha.acosta@gmail.com', 'Calle 60 #8-21, Bogotá', 'Mercadóloga', TRUE),
('CC', '79220022', 'Rodrigo', 'Alberto', 'Castaño', 'Mesa', 'Padre', '3001110022', '6012340022', 'rodrigo.castano@gmail.com', 'Carrera 19 #108-14, Bogotá', 'Arquitecto', TRUE),
('CC', '52330023', 'Juliana', 'María', 'Molina', 'Ortiz', 'Madre', '3001110023', NULL, 'juliana.molina@gmail.com', 'Calle 85 #16-33, Bogotá', 'Comunicadora Social', TRUE),
('CC', '80440024', 'Diego', 'Armando', 'Parra', 'Guzmán', 'Padre', '3001110024', NULL, 'diego.parra@gmail.com', 'Av. 19 #122-45, Bogotá', 'Contador', TRUE),
('CC', '52550025', 'Patricia', 'Elena', 'Velasco', 'Cruz', 'Madre', '3001110025', NULL, 'patricia.velasco@gmail.com', 'Carrera 24 #51-19, Bogotá', 'Enfermera Jefe', TRUE),
('CC', '79660026', 'Camilo', 'Ernesto', 'Patiño', 'Ramírez', 'Padre', '3001110026', '6012340026', 'camilo.patino@gmail.com', 'Calle 147 #18-40, Bogotá', 'Ingeniero Mecánico', TRUE),
('CC', '52770027', 'Gloria', 'Inés', 'León', 'Guerrero', 'Madre', '3001110027', NULL, 'gloria.leon@gmail.com', 'Carrera 7 #112-30, Bogotá', 'Empresaria Textil', TRUE),
('CC', '80880028', 'Felipe', 'Santiago', 'Vargas', 'Prieto', 'Padre', '3001110028', NULL, 'felipe.vargas@gmail.com', 'Calle 95 #14-28, Bogotá', 'Director de Proyectos', TRUE),
('CC', '52990029', 'Verónica', 'Isabel', 'Bermúdez', 'Cortés', 'Madre', '3001110029', NULL, 'veronica.bermudez@gmail.com', 'Carrera 45 #103-15, Bogotá', 'Médica Pediatra', TRUE),
('CC', '79100030', 'Alonso', 'Rafael', 'Rincón', 'Camargo', 'Padre', '3001110030', NULL, 'alonso.rincon@gmail.com', 'Calle 125 #21-50, Bogotá', 'Abogado Tributario', TRUE),
('CC', '52110031', 'Ángela', 'María', 'Duarte', 'Lozano', 'Madre', '3001110031', NULL, 'angela.duarte@gmail.com', 'Calle 68 #12-19, Bogotá', 'Veterinaria', TRUE),
('CE', '40010032', 'Pierre', 'Jean', 'Dupont', 'Morel', 'Padre', '3001110032', '6012340032', 'pierre.dupont@gmail.com', 'Calle 90 #11-44, Bogotá', 'Consultor Diplomático', TRUE);

-- 4. Estudiante-Acudiente (37 links, including siblings sharing guardians 1, 2, 3)
INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) VALUES
(1, 1, TRUE, TRUE), (1, 12, FALSE, TRUE), (2, 2, TRUE, TRUE), (3, 3, TRUE, TRUE),
(4, 4, TRUE, TRUE), (5, 5, TRUE, TRUE), (6, 6, TRUE, TRUE), (7, 7, TRUE, TRUE),
(8, 8, TRUE, TRUE), (9, 9, TRUE, TRUE), (10, 10, TRUE, TRUE), (11, 11, TRUE, TRUE),
(12, 1, TRUE, TRUE), (12, 12, FALSE, TRUE), (13, 13, TRUE, TRUE), (14, 14, TRUE, TRUE),
(15, 15, TRUE, TRUE), (16, 16, TRUE, TRUE), (17, 17, TRUE, TRUE), (18, 18, TRUE, TRUE),
(19, 19, TRUE, TRUE), (20, 20, TRUE, TRUE), (21, 21, TRUE, TRUE), (22, 22, TRUE, TRUE),
(23, 23, TRUE, TRUE), (24, 24, TRUE, TRUE), (25, 25, TRUE, TRUE), (26, 26, TRUE, TRUE),
(27, 27, TRUE, TRUE), (28, 28, TRUE, TRUE), (29, 29, TRUE, TRUE), (30, 30, TRUE, TRUE),
(31, 2, TRUE, TRUE), (32, 3, TRUE, TRUE), (33, 31, TRUE, TRUE), (34, 8, TRUE, TRUE),
(35, 32, TRUE, TRUE);

-- 5. Matrículas (35 active enrollments for 2026)
INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula, observaciones) VALUES
(1, 1, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(2, 2, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(3, 3, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(4, 4, 2026, 'Activa', '2026-01-16', 'Seguimiento médico asma'),
(5, 5, 2026, 'Activa', '2026-01-16', 'Matrícula regular'),
(6, 6, 2026, 'Activa', '2026-01-16', 'Matrícula regular'),
(7, 7, 2026, 'Activa', '2026-01-17', 'Matrícula regular'),
(8, 8, 2026, 'Activa', '2026-01-17', 'Matrícula regular'),
(9, 9, 2026, 'Activa', '2026-01-17', 'Matrícula regular'),
(10, 10, 2026, 'Activa', '2026-01-18', 'Matrícula regular'),
(11, 11, 2026, 'Activa', '2026-01-18', 'Matrícula regular'),
(12, 12, 2026, 'Activa', '2026-01-18', 'Descuento hermanos'),
(13, 5, 2026, 'Activa', '2026-01-19', 'Matrícula regular'),
(14, 6, 2026, 'Activa', '2026-01-19', 'Matrícula regular'),
(15, 7, 2026, 'Activa', '2026-01-19', 'Matrícula regular'),
(16, 8, 2026, 'Activa', '2026-01-20', 'Matrícula regular'),
(17, 9, 2026, 'Activa', '2026-01-20', 'Matrícula regular'),
(18, 10, 2026, 'Activa', '2026-01-20', 'Matrícula regular'),
(19, 11, 2026, 'Activa', '2026-01-21', 'Beca excelencia académica'),
(20, 12, 2026, 'Activa', '2026-01-21', 'Matrícula regular'),
(21, 3, 2026, 'Activa', '2026-01-21', 'Matrícula regular'),
(22, 4, 2026, 'Activa', '2026-01-22', 'Matrícula regular'),
(23, 5, 2026, 'Activa', '2026-01-22', 'Matrícula regular'),
(24, 6, 2026, 'Activa', '2026-01-22', 'Matrícula regular'),
(25, 7, 2026, 'Activa', '2026-01-23', 'Matrícula regular'),
(26, 8, 2026, 'Activa', '2026-01-23', 'Matrícula regular'),
(27, 9, 2026, 'Activa', '2026-01-23', 'Matrícula regular'),
(28, 10, 2026, 'Activa', '2026-01-24', 'Matrícula regular'),
(29, 11, 2026, 'Activa', '2026-01-24', 'Matrícula regular'),
(30, 12, 2026, 'Activa', '2026-01-24', 'Matrícula regular'),
(31, 6, 2026, 'Activa', '2026-01-25', 'Descuento hermanos'),
(32, 7, 2026, 'Activa', '2026-01-25', 'Descuento hermanos'),
(33, 2, 2026, 'Activa', '2026-01-25', 'Matrícula regular'),
(34, 8, 2026, 'Activa', '2026-01-26', 'Descuento hermanos'),
(35, 10, 2026, 'Activa', '2026-01-26', 'Estudiante internacional');

-- 6. Profesores (30 rows)
INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) VALUES
('CC', '79001001', 'Alberto', 'Mario', 'Guzmán', 'Londoño', 'alberto.guzman@littlehorizons.edu.co', '3115001001', 'Matemáticas', '2016-01-15', 'Activo'),
('CC', '52001002', 'Elena', 'María', 'Quintero', 'Ospina', 'elena.quintero@littlehorizons.edu.co', '3115001002', 'Lengua Castellana', '2017-01-16', 'Activo'),
('CC', '79001003', 'Javier', 'Andrés', 'Becerra', 'Ríos', 'javier.becerra@littlehorizons.edu.co', '3115001003', 'Ciencias Naturales', '2018-01-15', 'Activo'),
('CC', '52001004', 'Lucía', 'Fernanda', 'Patiño', 'Arango', 'lucia.patino@littlehorizons.edu.co', '3115001004', 'Inglés', '2018-07-10', 'Activo'),
('CC', '79001005', 'Sergio', 'Alejandro', 'Salgado', 'Mora', 'sergio.salgado@littlehorizons.edu.co', '3115001005', 'Física', '2019-01-14', 'Activo'),
('CC', '52001006', 'Carolina', 'Inés', 'Villalba', 'Cano', 'carolina.villalba@littlehorizons.edu.co', '3115001006', 'Química', '2019-01-14', 'Activo'),
('CC', '79001007', 'Mauricio', 'Esteban', 'Cardona', 'Zapata', 'mauricio.cardona@littlehorizons.edu.co', '3115001007', 'Historia y Sociales', '2020-01-13', 'Activo'),
('CC', '52001008', 'Natalia', 'Sofía', 'Montoya', 'Vélez', 'natalia.montoya@littlehorizons.edu.co', '3115001008', 'Educación Preescolar', '2020-01-13', 'Activo'),
('CC', '79001009', 'Andrés', 'Felipe', 'Ríos', 'Gaviria', 'andres.rios@littlehorizons.edu.co', '3115001009', 'Educación Física', '2020-07-06', 'Activo'),
('CC', '52001010', 'Valeria', 'Cristina', 'Núñez', 'Palacios', 'valeria.nunez@littlehorizons.edu.co', '3115001010', 'Artes Plásticas', '2021-01-18', 'Activo'),
('CC', '79001011', 'César', 'Augusto', 'Márquez', 'Pinto', 'cesar.marquez@littlehorizons.edu.co', '3115001011', 'Informática y Robótica', '2021-01-18', 'Activo'),
('CC', '52001012', 'Claudia', 'Liliana', 'Osorio', 'Marín', 'claudia.osorio@littlehorizons.edu.co', '3115001012', 'Biología', '2021-07-12', 'Activo'),
('CC', '79001013', 'Rafael', 'Eduardo', 'Céspedes', 'Toro', 'rafael.cespedes@littlehorizons.edu.co', '3115001013', 'Música', '2022-01-17', 'Activo'),
('CC', '52001014', 'Paula', 'Andrea', 'Sierra', 'Bustos', 'paula.sierra@littlehorizons.edu.co', '3115001014', 'Filosofía y Ética', '2022-01-17', 'Activo'),
('CC', '79001015', 'Daniel', 'Santiago', 'Prieto', 'Reyes', 'daniel.prieto@littlehorizons.edu.co', '3115001015', 'Matemáticas', '2022-07-11', 'Activo'),
('CE', '30001016', 'Emily', 'Grace', 'Thompson', 'Clark', 'emily.thompson@littlehorizons.edu.co', '3115001016', 'Inglés Avanzado', '2022-07-11', 'Activo'),
('CC', '79001017', 'Iván', 'Darío', 'Benítez', 'Lara', 'ivan.benitez@littlehorizons.edu.co', '3115001017', 'Geografía', '2023-01-16', 'Activo'),
('CC', '52001018', 'Marcela', 'Judith', 'Escobar', 'Dávila', 'marcela.escobar@littlehorizons.edu.co', '3115001018', 'Educación Primaria', '2023-01-16', 'Activo'),
('CC', '79001019', 'Nicolás', 'Martín', 'Forero', 'Peña', 'nicolas.forero@littlehorizons.edu.co', '3115001019', 'Tecnología', '2023-07-10', 'Activo'),
('CC', '52001020', 'Tatiana', 'Lorena', 'Garzón', 'Méndez', 'tatiana.garzon@littlehorizons.edu.co', '3115001020', 'Literatura', '2023-07-10', 'Activo'),
('CC', '79001021', 'Óscar', 'Javier', 'Rueda', 'Camacho', 'oscar.rueda@littlehorizons.edu.co', '3115001021', 'Economía y Finanzas', '2024-01-15', 'Activo'),
('CC', '52001022', 'Juliana', 'Paola', 'Valencia', 'Correa', 'juliana.valencia@littlehorizons.edu.co', '3115001022', 'Teatro y Expresión', '2024-01-15', 'Activo'),
('CC', '79001023', 'Gabriel', 'Enrique', 'Miranda', 'Solano', 'gabriel.miranda@littlehorizons.edu.co', '3115001023', 'Estadística', '2024-01-15', 'Activo'),
('CC', '52001024', 'Lorena', 'Estefanía', 'Zambrano', 'Vera', 'lorena.zambrano@littlehorizons.edu.co', '3115001024', 'Francés', '2024-07-08', 'Activo'),
('CC', '79001025', 'Héctor', 'Manuel', 'Barrios', 'Cuevas', 'hector.barrios@littlehorizons.edu.co', '3115001025', 'Ajedrez y Lógica', '2024-07-08', 'Activo'),
('CC', '52001026', 'Camila', 'Rosa', 'Delgado', 'Sánchez', 'camila.delgado@littlehorizons.edu.co', '3115001026', 'Ecología', '2025-01-13', 'Activo'),
('CC', '79001027', 'Fabián', 'Ricardo', 'Arenas', 'Puentes', 'fabian.arenas@littlehorizons.edu.co', '3115001027', 'Educación Física', '2025-01-13', 'Activo'),
('CC', '52001028', 'Mariana', 'Ximena', 'Galindo', 'Tovar', 'mariana.galindo@littlehorizons.edu.co', '3115001028', 'Psicopedagogía', '2025-01-13', 'Activo'),
('CC', '79001029', 'Rodrigo', 'Alonso', 'Pacheco', 'Nieto', 'rodrigo.pacheco@littlehorizons.edu.co', '3115001029', 'Programación', '2025-07-07', 'Activo'),
('CC', '52001030', 'Sofía', 'Alejandra', 'Bustos', 'Rincón', 'sofia.bustos@littlehorizons.edu.co', '3115001030', 'Danza y Cultura', '2025-07-07', 'Licencia');

-- 7. Materias (36 rows covering Preescolar to Once)
INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) VALUES
('PRE-01', 'Motricidad y Juego', 'Desarrollo psicomotor en primera infancia', 1, 5, TRUE),
('PRE-02', 'Pre-Lectura y Cuentos', 'Iniciación a la lectura y expresión oral', 1, 4, TRUE),
('PRI-01', 'Matemáticas Iniciales', 'Números naturales, suma y resta básica', 2, 5, TRUE),
('PRI-02', 'Lenguaje I', 'Lectoescritura y comprensión', 2, 5, TRUE),
('SEG-01', 'Matemáticas II', 'Multiplicación y geometría básica', 3, 5, TRUE),
('SEG-02', 'Ciencias Naturales II', 'Seres vivos y entorno', 3, 3, TRUE),
('TER-01', 'Matemáticas III', 'División y fracciones iniciales', 4, 5, TRUE),
('TER-02', 'Inglés III', 'Vocabulario y estructuras básicas', 4, 4, TRUE),
('CUA-01', 'Matemáticas IV', 'Operaciones combinadas y decimales', 5, 5, TRUE),
('CUA-02', 'Ciencias Sociales IV', 'Geografía e historia de Colombia', 5, 3, TRUE),
('CUA-03', 'Lengua Castellana IV', 'Redacción y análisis literario inicial', 5, 4, TRUE),
('QUI-01', 'Matemáticas V', 'Fracciones, porcentajes y geometría', 6, 5, TRUE),
('QUI-02', 'Ciencias Naturales V', 'Cuerpo humano y ecosistemas', 6, 4, TRUE),
('QUI-03', 'Inglés V', 'Comprensión lectora y conversación', 6, 4, TRUE),
('SEX-01', 'Matemáticas VI', 'Números enteros y álgebra introductoria', 7, 5, TRUE),
('SEX-02', 'Biología VI', 'Célula y taxonomía básica', 7, 4, TRUE),
('SEX-03', 'Informática VI', 'Lógica computacional y algoritmos', 7, 3, TRUE),
('SEP-01', 'Matemáticas VII', 'Números racionales y ecuaciones lineales', 8, 5, TRUE),
('SEP-02', 'Física VII', 'Introducción al movimiento y fuerzas', 8, 3, TRUE),
('SEP-03', 'Historia Universal VII', 'Edad Media y Renacimiento', 8, 3, TRUE),
('OCT-01', 'Álgebra VIII', 'Polinomios y factorización', 9, 5, TRUE),
('OCT-02', 'Química VIII', 'Estructura atómica y tabla periódica', 9, 4, TRUE),
('OCT-03', 'Inglés VIII', 'Nivel B1 intermedio', 9, 4, TRUE),
('NOV-01', 'Álgebra y Funciones IX', 'Funciones lineales, cuadráticas y sistemas', 10, 5, TRUE),
('NOV-02', 'Biología IX', 'Genética y evolución', 10, 4, TRUE),
('NOV-03', 'Física IX', 'Cinemática y dinámica', 10, 4, TRUE),
('DEC-01', 'Trigonometría X', 'Razones trigonométricas y geometría analítica', 11, 5, TRUE),
('DEC-02', 'Química X', 'Estequiometría y soluciones químicas', 11, 4, TRUE),
('DEC-03', 'Física X', 'Termodinámica y ondas', 11, 4, TRUE),
('DEC-04', 'Filosofía X', 'Historia del pensamiento filosófico', 11, 2, TRUE),
('ONC-01', 'Cálculo XI', 'Límites, derivadas y aplicaciones', 12, 5, TRUE),
('ONC-02', 'Física XI', 'Electromagnetismo y óptica', 12, 4, TRUE),
('ONC-03', 'Química XI', 'Química orgánica', 12, 4, TRUE),
('ONC-04', 'Inglés XI', 'Preparación B2 y lectura crítica', 12, 4, TRUE),
('ONC-05', 'Economía y Política XI', 'Macroeconomía y ciudadanía', 12, 2, TRUE),
('ONC-06', 'Robótica y Programación XI', 'Proyectos de automatización y software', 12, 3, TRUE);

-- 8. Asignaciones de Profesores con Horario (36 rows, sin conflictos)
INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion, dia_semana, hora_inicio, hora_fin) VALUES
(8, 1, 2026, 'A', 'Lunes', '08:00:00', '10:00:00'),
(8, 2, 2026, 'A', 'Martes', '08:00:00', '10:00:00'),
(18, 3, 2026, 'A', 'Lunes', '08:00:00', '10:00:00'),
(18, 4, 2026, 'A', 'Martes', '08:00:00', '10:00:00'),
(1, 5, 2026, 'A', 'Lunes', '07:00:00', '09:00:00'),
(3, 6, 2026, 'A', 'Martes', '07:00:00', '09:00:00'),
(15, 7, 2026, 'A', 'Lunes', '07:00:00', '09:00:00'),
(4, 8, 2026, 'A', 'Miércoles', '07:00:00', '09:00:00'),
(1, 9, 2026, 'A', 'Martes', '09:00:00', '11:00:00'),
(7, 10, 2026, 'A', 'Jueves', '08:00:00', '10:00:00'),
(2, 11, 2026, 'A', 'Lunes', '09:00:00', '11:00:00'),
(15, 12, 2026, 'A', 'Martes', '09:00:00', '11:00:00'),
(3, 13, 2026, 'A', 'Miércoles', '09:00:00', '11:00:00'),
(4, 14, 2026, 'A', 'Jueves', '09:00:00', '11:00:00'),
(1, 15, 2026, 'A', 'Miércoles', '07:00:00', '09:00:00'),
(12, 16, 2026, 'A', 'Lunes', '10:00:00', '12:00:00'),
(11, 17, 2026, 'A', 'Viernes', '08:00:00', '10:00:00'),
(15, 18, 2026, 'A', 'Miércoles', '10:00:00', '12:00:00'),
(5, 19, 2026, 'A', 'Lunes', '08:00:00', '10:00:00'),
(7, 20, 2026, 'A', 'Martes', '10:00:00', '12:00:00'),
(1, 21, 2026, 'A', 'Jueves', '07:00:00', '09:00:00'),
(6, 22, 2026, 'A', 'Martes', '07:00:00', '09:00:00'),
(16, 23, 2026, 'A', 'Lunes', '09:00:00', '11:00:00'),
(23, 24, 2026, 'A', 'Lunes', '07:00:00', '09:00:00'),
(12, 25, 2026, 'A', 'Miércoles', '08:00:00', '10:00:00'),
(5, 26, 2026, 'A', 'Miércoles', '10:00:00', '12:00:00'),
(23, 27, 2026, 'A', 'Martes', '07:00:00', '09:00:00'),
(6, 28, 2026, 'A', 'Jueves', '09:00:00', '11:00:00'),
(5, 29, 2026, 'A', 'Jueves', '10:00:00', '12:00:00'),
(14, 30, 2026, 'A', 'Viernes', '09:00:00', '11:00:00'),
(1, 31, 2026, 'A', 'Viernes', '07:00:00', '09:00:00'),
(5, 32, 2026, 'A', 'Viernes', '09:00:00', '11:00:00'),
(6, 33, 2026, 'A', 'Viernes', '11:00:00', '13:00:00'),
(16, 34, 2026, 'A', 'Miércoles', '09:00:00', '11:00:00'),
(21, 35, 2026, 'A', 'Jueves', '11:00:00', '13:00:00'),
(29, 36, 2026, 'A', 'Martes', '11:00:00', '13:00:00');

-- 9. Inscripciones en Materias (45 rows)
INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) VALUES
(1, 1, 2026, 'I', 'A', '2026-01-26', 'Activa'), (1, 2, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(2, 3, 2026, 'I', 'A', '2026-01-26', 'Activa'), (2, 4, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(3, 5, 2026, 'I', 'A', '2026-01-26', 'Activa'), (3, 6, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(4, 7, 2026, 'I', 'A', '2026-01-26', 'Activa'), (4, 8, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(5, 9, 2026, 'I', 'A', '2026-01-26', 'Activa'), (5, 10, 2026, 'I', 'A', '2026-01-26', 'Activa'), (5, 11, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(6, 12, 2026, 'I', 'A', '2026-01-26', 'Activa'), (6, 13, 2026, 'I', 'A', '2026-01-26', 'Activa'), (6, 14, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(7, 15, 2026, 'I', 'A', '2026-01-26', 'Activa'), (7, 16, 2026, 'I', 'A', '2026-01-26', 'Activa'), (7, 17, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(8, 18, 2026, 'I', 'A', '2026-01-26', 'Activa'), (8, 19, 2026, 'I', 'A', '2026-01-26', 'Activa'), (8, 20, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(9, 21, 2026, 'I', 'A', '2026-01-26', 'Activa'), (9, 22, 2026, 'I', 'A', '2026-01-26', 'Activa'), (9, 23, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(10, 24, 2026, 'I', 'A', '2026-01-26', 'Activa'), (10, 25, 2026, 'I', 'A', '2026-01-26', 'Activa'), (10, 26, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(11, 27, 2026, 'I', 'A', '2026-01-26', 'Activa'), (11, 28, 2026, 'I', 'A', '2026-01-26', 'Activa'), (11, 29, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(12, 31, 2026, 'I', 'A', '2026-01-26', 'Activa'), (12, 32, 2026, 'I', 'A', '2026-01-26', 'Activa'), (12, 33, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(14, 12, 2026, 'I', 'A', '2026-01-26', 'Activa'), (15, 15, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(16, 18, 2026, 'I', 'A', '2026-01-26', 'Activa'), (17, 21, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(18, 24, 2026, 'I', 'A', '2026-01-26', 'Activa'), (19, 27, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(20, 31, 2026, 'I', 'A', '2026-01-26', 'Activa'), (20, 34, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(30, 31, 2026, 'I', 'A', '2026-01-26', 'Activa'), (30, 36, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(31, 12, 2026, 'I', 'A', '2026-01-26', 'Activa'), (32, 15, 2026, 'I', 'A', '2026-01-26', 'Activa'),
(35, 24, 2026, 'I', 'A', '2026-01-26', 'Activa');

-- 10. Registros Académicos / Calificaciones (36 rows)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) VALUES
(1, 1, 8, 2026, 'I', 4.8, 'Excelente coordinación motriz', '2026-04-10'),
(1, 2, 8, 2026, 'I', 4.6, 'Gran participación en lectura', '2026-04-10'),
(2, 3, 18, 2026, 'I', 4.2, 'Buen dominio numérico', '2026-04-10'),
(2, 4, 18, 2026, 'I', 4.4, 'Lectura fluida', '2026-04-10'),
(3, 5, 1, 2026, 'I', 4.5, 'Resuelve problemas con agilidad', '2026-04-11'),
(3, 6, 3, 2026, 'I', 4.7, 'Muy curiosa en laboratorio', '2026-04-11'),
(4, 7, 15, 2026, 'I', 3.4, 'Requiere reforzar fracciones', '2026-04-11'),
(4, 8, 4, 2026, 'I', 3.9, 'Participación constante', '2026-04-11'),
(5, 9, 1, 2026, 'I', 4.9, 'Desempeño sobresaliente', '2026-04-12'),
(5, 10, 7, 2026, 'I', 4.5, 'Excelente exposición', '2026-04-12'),
(5, 11, 2, 2026, 'I', 4.7, 'Muy buena redacción', '2026-04-12'),
(6, 12, 15, 2026, 'I', 3.8, 'Cumple con los talleres', '2026-04-12'),
(6, 12, 15, 2026, 'II', 4.1, 'Progreso notable en segundo período', '2026-06-18'),
(6, 13, 3, 2026, 'I', 4.0, 'Buen desempeño', '2026-04-12'),
(7, 15, 1, 2026, 'I', 4.3, 'Buena base en enteros', '2026-04-13'),
(7, 16, 12, 2026, 'I', 4.6, 'Excelente informe de microscopía', '2026-04-13'),
(7, 17, 11, 2026, 'I', 4.8, 'Destacada en lógica', '2026-04-13'),
(8, 18, 15, 2026, 'I', 2.8, 'Bajo rendimiento en ecuaciones, requiere tutoría', '2026-04-13'),
(8, 19, 5, 2026, 'I', 3.2, 'Mejorar entrega de prácticas', '2026-04-13'),
(9, 21, 1, 2026, 'I', 4.4, 'Buen manejo de factorización', '2026-04-14'),
(9, 22, 6, 2026, 'I', 4.1, 'Prácticas completas', '2026-04-14'),
(10, 24, 23, 2026, 'I', 3.7, 'Avance adecuado', '2026-04-14'),
(10, 26, 5, 2026, 'I', 3.5, 'Reforzar diagramas de cuerpo libre', '2026-04-14'),
(11, 27, 23, 2026, 'I', 4.8, 'Excelente nivel en trigonometría', '2026-04-15'),
(11, 28, 6, 2026, 'I', 4.5, 'Muy precisa en estequiometría', '2026-04-15'),
(12, 31, 1, 2026, 'I', 4.6, 'Sólido en cálculo diferencial', '2026-04-15'),
(12, 31, 1, 2026, 'II', 4.8, 'Excelente proyecto de derivadas', '2026-06-20'),
(12, 32, 5, 2026, 'I', 4.3, 'Buen análisis experimental', '2026-04-15'),
(14, 12, 15, 2026, 'I', 2.6, 'Requiere plan de mejoramiento', '2026-04-15'),
(15, 15, 1, 2026, 'I', 4.0, 'Buen trabajo en clase', '2026-04-15'),
(19, 27, 23, 2026, 'I', 5.0, 'Nota máxima del curso', '2026-04-16'),
(20, 31, 1, 2026, 'I', 4.2, 'Constante', '2026-04-16'),
(20, 34, 16, 2026, 'I', 4.7, 'Excelente nivel oral en inglés', '2026-04-16'),
(30, 31, 1, 2026, 'I', 3.9, 'Buen cierre de período', '2026-04-16'),
(30, 36, 29, 2026, 'I', 4.9, 'Proyecto destacado en robótica', '2026-04-16'),
(35, 24, 23, 2026, 'I', 4.5, 'Adaptación sobresaliente', '2026-04-16');

-- 11. Asistencias (24 rows, including student 8 with 4 absences in one week)
INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) VALUES
(1, 1, '2026-11-02', 'Presente', NULL, 8),
(2, 3, '2026-11-02', 'Presente', NULL, 18),
(3, 5, '2026-11-02', 'Presente', NULL, 1),
(4, 7, '2026-11-02', 'Excusa Médica', 'Control respiratorio', 15),
(5, 9, '2026-11-02', 'Presente', NULL, 1),
(6, 12, '2026-11-02', 'Tardanza', 'Llegó 10 minutos tarde', 15),
(7, 15, '2026-11-02', 'Presente', NULL, 1),
(8, 18, '2026-11-02', 'Ausente', 'Sin excusa', 15),
(8, 18, '2026-11-03', 'Ausente', 'Sin excusa', 15),
(8, 19, '2026-11-04', 'Ausente', 'Sin excusa', 5),
(8, 20, '2026-11-05', 'Ausente', 'Sin excusa', 7),
(9, 21, '2026-11-02', 'Presente', NULL, 1),
(10, 24, '2026-11-02', 'Presente', NULL, 23),
(11, 27, '2026-11-02', 'Presente', NULL, 23),
(12, 31, '2026-11-02', 'Presente', NULL, 1),
(14, 12, '2026-11-02', 'Ausente', 'Falta injustificada', 15),
(14, 12, '2026-11-03', 'Ausente', 'Falta injustificada', 15),
(14, 12, '2026-11-04', 'Ausente', 'Falta injustificada', 15),
(14, 12, '2026-11-05', 'Ausente', 'Falta injustificada', 15),
(15, 15, '2026-11-02', 'Presente', NULL, 1),
(19, 27, '2026-11-02', 'Presente', NULL, 23),
(20, 31, '2026-11-02', 'Presente', NULL, 1),
(30, 36, '2026-11-03', 'Presente', NULL, 29),
(35, 24, '2026-11-02', 'Tardanza', 'Tráfico en Av. 9', 23);

-- 12. Clubes (30 rows)
INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) VALUES
('Club de Ciencias Experimentales', 'Experimentos de física, química y biología aplicada', 3, 25, 'Martes 3:00-5:00 PM', TRUE),
('Club de Robótica', 'Diseño, armado y programación de robots educativos', 11, 20, 'Miércoles 3:00-5:00 PM', TRUE),
('Club de Artes Plásticas', 'Pintura, escultura y técnicas mixtas', 10, 25, 'Jueves 3:00-4:30 PM', TRUE),
('Club de Fútbol Juvenil', 'Entrenamiento técnico y torneos intercolegiados', 9, 30, 'Martes y Jueves 3:30-5:00 PM', TRUE),
('Club de Ajedrez', 'Estrategia, táctica y competencias regionales', 25, 24, 'Lunes 3:00-4:30 PM', TRUE),
('Club de Debate y Oratoria', 'Argumentación crítica y modelo ONU', 14, 20, 'Miércoles 3:00-4:30 PM', TRUE),
('Club de Música y Coro', 'Ensamble instrumental y técnica vocal', 13, 30, 'Viernes 2:30-4:30 PM', TRUE),
('Club de Baloncesto', 'Fundamentos y liga escolar de baloncesto', 27, 25, 'Lunes y Miércoles 3:30-5:00 PM', TRUE),
('Club de Teatro', 'Actuación, improvisación y montaje escénico', 22, 22, 'Jueves 3:00-5:00 PM', TRUE),
('Club de Programación Competitiva', 'Algoritmos en Java y Python', 29, 20, 'Viernes 3:00-5:00 PM', TRUE),
('Club de Lectura Creativa', 'Análisis literario y escritura de cuentos', 20, 25, 'Martes 3:00-4:30 PM', TRUE),
('Club de Ecología y Huerta', 'Sostenibilidad, reciclaje y huerta escolar', 26, 25, 'Miércoles 3:00-4:30 PM', TRUE),
('Club de Francés Cultural', 'Conversación, cine y gastronomía francófona', 24, 20, 'Jueves 3:00-4:30 PM', TRUE),
('Club de Inglés Conversacional', 'Práctica inmersiva y cultura anglófona', 16, 25, 'Lunes 3:00-4:30 PM', TRUE),
('Club de Astronomía', 'Observación celeste y astrofísica básica', 5, 20, 'Viernes 4:00-6:00 PM', TRUE),
('Club de Matemáticas Olímpicas', 'Preparación para olimpiadas matemáticas', 1, 20, 'Martes 3:00-4:30 PM', TRUE),
('Club de Fotografía', 'Composición fotográfica y edición digital', 10, 18, 'Miércoles 3:00-4:30 PM', TRUE),
('Club de Natación', 'Entrenamiento acuático en convenio deportivo', 9, 20, 'Sábado 8:00-10:00 AM', TRUE),
('Club de Voleibol', 'Técnica de voleibol femenino y masculino', 27, 24, 'Martes y Jueves 3:30-5:00 PM', TRUE),
('Club de Periodismo Escolar', 'Redacción de periódico y podcast del colegio', 2, 18, 'Lunes 3:00-4:30 PM', TRUE),
('Club de Emprendimiento Juvenil', 'Diseño de modelos de negocio escolares', 21, 25, 'Jueves 3:00-4:30 PM', TRUE),
('Club de Química Divertida', 'Reacciones seguras y feria científica', 6, 20, 'Lunes 3:00-4:30 PM', TRUE),
('Club de Diseño 3D', 'Modelado CAD e impresión 3D', 19, 16, 'Miércoles 3:00-5:00 PM', TRUE),
('Club de Danza Folclórica', 'Ritmos tradicionales colombianos', 30, 28, 'Viernes 3:00-4:30 PM', TRUE),
('Club de Primeros Auxilios', 'Brigada estudiantil de prevención y socorro', 12, 20, 'Jueves 3:00-4:30 PM', TRUE),
('Club de Geografía y Viajes', 'Cartografía, culturas y geopolítica', 17, 22, 'Martes 3:00-4:30 PM', TRUE),
('Club de Estadística Aplicada', 'Análisis de datos deportivos y escolares', 23, 18, 'Miércoles 3:00-4:30 PM', TRUE),
('Club de Cuentacuentos Infantil', 'Narración oral para primaria', 18, 25, 'Lunes 2:30-4:00 PM', TRUE),
('Club de Juegos Lúdicos', 'Estimulación temprana mediante juego dirigido', 8, 20, 'Martes 2:00-3:30 PM', TRUE),
('Club de Bienestar y Mindfulness', 'Manejo emocional y técnicas de estudio', 28, 25, 'Viernes 2:30-4:00 PM', TRUE);

-- 13. Membresías de Club (25 rows)
INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) VALUES
(1, 29, '2026-02-02', 'Activa'), (2, 28, '2026-02-02', 'Activa'), (3, 3, '2026-02-02', 'Activa'),
(4, 5, '2026-02-03', 'Activa'), (5, 11, '2026-02-03', 'Activa'), (6, 4, '2026-02-03', 'Activa'),
(7, 1, '2026-02-04', 'Activa'), (8, 4, '2026-02-04', 'Activa'), (9, 7, '2026-02-04', 'Activa'),
(10, 2, '2026-02-05', 'Activa'), (11, 16, '2026-02-05', 'Activa'), (12, 10, '2026-02-05', 'Activa'),
(13, 3, '2026-02-06', 'Activa'), (14, 8, '2026-02-06', 'Activa'), (15, 12, '2026-02-06', 'Activa'),
(16, 5, '2026-02-07', 'Activa'), (17, 9, '2026-02-07', 'Activa'), (18, 6, '2026-02-07', 'Activa'),
(19, 16, '2026-02-08', 'Activa'), (20, 21, '2026-02-08', 'Activa'), (25, 14, '2026-02-09', 'Activa'),
(27, 19, '2026-02-09', 'Activa'), (29, 6, '2026-02-09', 'Activa'), (30, 2, '2026-02-10', 'Activa'),
(35, 13, '2026-02-10', 'Activa');

-- 14. Eventos (10 rows)
INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) VALUES
('Feria Anual de Ciencias e Innovación', 'Exposición de proyectos científicos y prototipos de robótica', 'Científico', '2026-09-18', 'Coliseo Principal', TRUE, 3, TRUE),
('Día de la Colombianidad y Cultura', 'Muestras artísticas, música y gastronomía regional', 'Cultural', '2026-07-20', 'Patio Central', FALSE, 13, TRUE),
('Olimpiadas Deportivas Little Horizons', 'Torneos intercursos de fútbol, baloncesto y atletismo', 'Deportivo', '2026-10-09', 'Canchas Deportivas', TRUE, 9, TRUE),
('Olimpiada Regional de Matemáticas', 'Competencia académica intercolegiada', 'Académico', '2026-08-14', 'Auditorio Principal', TRUE, 1, TRUE),
('Festival de Teatro y Literatura', 'Obras cortas y declamación poética', 'Cultural', '2026-05-22', 'Teatro Escolar', FALSE, 22, TRUE),
('Torneo Escolar de Ajedrez', 'Rondas suizas categorías infantil y juvenil', 'Deportivo', '2026-06-12', 'Biblioteca Central', TRUE, 25, TRUE),
('Día de la Familia Little Horizons', 'Integración comunitaria de padres, estudiantes y docentes', 'Social', '2026-04-25', 'Campus Completo', FALSE, 28, TRUE),
('Hackathon Juvenil de Programación', 'Reto de desarrollo de soluciones educativas en 8 horas', 'Científico', '2026-10-23', 'Laboratorio de Cómputo', TRUE, 29, TRUE),
('Spelling Bee Contest', 'Concurso anual de deletreo en inglés', 'Académico', '2026-09-04', 'Auditorio Principal', TRUE, 16, TRUE),
('Jornada Ecológica y Siembra', 'Actividad de arborización y conciencia ambiental', 'Social', '2026-06-05', 'Zona Verde Norte', FALSE, 26, TRUE);

-- 15. Participantes en Eventos (20 rows)
INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) VALUES
(1, 7, 'Competidor', '1er Lugar', 'Proyecto de bioplásticos'),
(1, 10, 'Competidor', '2do Lugar', 'Robot seguidor de línea'),
(1, 30, 'Competidor', '3er Lugar', 'Brazo hidráulico automatizado'),
(1, 11, 'Participante', 'Participante', 'Muestra de óptica'),
(2, 9, 'Participante', 'Participante', 'Presentación coral'),
(2, 17, 'Participante', 'Participante', 'Obra cultural'),
(3, 6, 'Competidor', '1er Lugar', 'Campeón fútbol infantil'),
(3, 8, 'Competidor', '2do Lugar', 'Subcampeón fútbol'),
(3, 14, 'Competidor', '1er Lugar', 'Máximo encestador'),
(4, 11, 'Competidor', '1er Lugar', 'Puntaje perfecto categoría X'),
(4, 19, 'Competidor', '2do Lugar', 'Mención honorífica'),
(4, 12, 'Competidor', '3er Lugar', 'Categoría XI'),
(6, 4, 'Competidor', '1er Lugar', 'Invicto 5 rondas'),
(6, 16, 'Competidor', '2do Lugar', 'Desempate técnico'),
(8, 12, 'Competidor', '1er Lugar', 'App de reciclaje escolar'),
(8, 30, 'Competidor', '2do Lugar', 'Sistema de riego inteligente'),
(9, 5, 'Competidor', '1er Lugar', 'Categoría primaria'),
(9, 25, 'Competidor', '2do Lugar', 'Categoría secundaria'),
(9, 35, 'Organizador', 'Participante', 'Apoyo bilingüe'),
(10, 15, 'Asistente', 'Participante', 'Siembra de 10 árboles');

-- 16. Conceptos de Pago (10 rows)
INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente, esta_activo) VALUES
('MAT', 'Matrícula Anual', 'Pago anual de matrícula escolar', 500000.00, FALSE, TRUE),
('PENSION', 'Pensión Mensual', 'Pago mensual de pensión académica', 350000.00, TRUE, TRUE),
('UNIFORME', 'Uniforme Completo', 'Uniforme diario y de educación física', 250000.00, FALSE, TRUE),
('TRANSPORTE', 'Transporte Escolar', 'Servicio de ruta escolar mensual', 150000.00, TRUE, TRUE),
('ALIMENTACION', 'Servicio de Alimentación', 'Almuerzo escolar balanceado mensual', 120000.00, TRUE, TRUE),
('ACTIVIDAD', 'Salida Pedagógica', 'Salida académica o cultural programada', 50000.00, FALSE, TRUE),
('CERTIFICADO', 'Certificado de Estudio', 'Expedición de certificados y constancias', 15000.00, FALSE, TRUE),
('LAB', 'Derechos de Laboratorio', 'Materiales anuales de laboratorio', 80000.00, FALSE, TRUE),
('SEGURO', 'Seguro Estudiantil', 'Póliza anual contra accidentes escolares', 65000.00, FALSE, TRUE),
('ANUARIO', 'Anuario Escolar', 'Edición impresa y digital anual', 45000.00, FALSE, TRUE);

-- 17. Tarifas Personalizadas por Estudiante (10 rows)
INSERT INTO tarifas_estudiante (id_estudiante, id_concepto, monto_personalizado, porcentaje_descuento, motivo, esta_activo) VALUES
(12, 2, 280000.00, 20.00, 'Descuento por hermanos (familia Gómez Restrepo)', TRUE),
(19, 2, 175000.00, 50.00, 'Beca excelencia académica 50%', TRUE),
(19, 1, 250000.00, 50.00, 'Beca excelencia académica 50%', TRUE),
(31, 2, 280000.00, 20.00, 'Descuento por hermanos (familia Rodríguez Pérez)', TRUE),
(32, 2, 280000.00, 20.00, 'Descuento por hermanos (familia Martínez López)', TRUE),
(34, 2, 280000.00, 20.00, 'Descuento por hermanos (familia Ramírez)', TRUE),
(5, 2, 315000.00, 10.00, 'Convenio empresarial', TRUE),
(11, 2, 245000.00, 30.00, 'Beca mérito matemático', TRUE),
(1, 4, 135000.00, 10.00, 'Descuento ruta compartida', TRUE),
(12, 4, 135000.00, 10.00, 'Descuento ruta compartida', TRUE);

-- 18. Pagos (35 rows; students 4, 8, 14, 18, 24 intentionally lack recent monthly pension payment for delinquency detection)
INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) VALUES
(1, 1, 1, 500000.00, '2026-01-15', 'Transferencia', 'REC-2026-0001', 2026, 'Anual', 'Matrícula 2026'),
(2, 2, 1, 500000.00, '2026-01-15', 'Tarjeta Débito', 'REC-2026-0002', 2026, 'Anual', 'Matrícula 2026'),
(3, 3, 1, 500000.00, '2026-01-15', 'Transferencia', 'REC-2026-0003', 2026, 'Anual', 'Matrícula 2026'),
(4, 4, 1, 500000.00, '2026-01-16', 'Tarjeta Crédito', 'REC-2026-0004', 2026, 'Anual', 'Matrícula 2026'),
(5, 5, 1, 500000.00, '2026-01-16', 'Transferencia', 'REC-2026-0005', 2026, 'Anual', 'Matrícula 2026'),
(6, 6, 1, 500000.00, '2026-01-16', 'Efectivo', 'REC-2026-0006', 2026, 'Anual', 'Matrícula 2026'),
(7, 7, 1, 500000.00, '2026-01-17', 'Transferencia', 'REC-2026-0007', 2026, 'Anual', 'Matrícula 2026'),
(8, 8, 1, 500000.00, '2026-01-17', 'Transferencia', 'REC-2026-0008', 2026, 'Anual', 'Matrícula 2026'),
(9, 9, 1, 500000.00, '2026-01-17', 'Tarjeta Débito', 'REC-2026-0009', 2026, 'Anual', 'Matrícula 2026'),
(10, 10, 1, 500000.00, '2026-01-18', 'Transferencia', 'REC-2026-0010', 2026, 'Anual', 'Matrícula 2026'),
(11, 11, 1, 500000.00, '2026-01-18', 'Transferencia', 'REC-2026-0011', 2026, 'Anual', 'Matrícula 2026'),
(12, 1, 1, 500000.00, '2026-01-18', 'Transferencia', 'REC-2026-0012', 2026, 'Anual', 'Matrícula 2026'),
(19, 19, 1, 250000.00, '2026-01-21', 'Transferencia', 'REC-2026-0013', 2026, 'Anual', 'Matrícula con beca 50%'),
(1, 1, 2, 350000.00, '2026-11-01', 'Transferencia', 'REC-2026-0014', 2026, 'IV', 'Pensión noviembre'),
(2, 2, 2, 350000.00, '2026-11-02', 'Tarjeta Débito', 'REC-2026-0015', 2026, 'IV', 'Pensión noviembre'),
(3, 3, 2, 350000.00, '2026-11-02', 'Transferencia', 'REC-2026-0016', 2026, 'IV', 'Pensión noviembre'),
(4, 4, 2, 350000.00, '2026-09-05', 'Transferencia', 'REC-2026-0017', 2026, 'III', 'Última pensión pagada en septiembre'),
(5, 5, 2, 315000.00, '2026-11-03', 'Transferencia', 'REC-2026-0018', 2026, 'IV', 'Pensión noviembre con descuento'),
(6, 6, 2, 350000.00, '2026-11-03', 'Efectivo', 'REC-2026-0019', 2026, 'IV', 'Pensión noviembre'),
(7, 7, 2, 350000.00, '2026-11-04', 'Transferencia', 'REC-2026-0020', 2026, 'IV', 'Pensión noviembre'),
(8, 8, 2, 350000.00, '2026-08-10', 'Transferencia', 'REC-2026-0021', 2026, 'III', 'Pago atrasado desde agosto'),
(9, 9, 2, 350000.00, '2026-11-04', 'Tarjeta Crédito', 'REC-2026-0022', 2026, 'IV', 'Pensión noviembre'),
(10, 10, 2, 350000.00, '2026-11-05', 'Transferencia', 'REC-2026-0023', 2026, 'IV', 'Pensión noviembre'),
(11, 11, 2, 245000.00, '2026-11-05', 'Transferencia', 'REC-2026-0024', 2026, 'IV', 'Pensión noviembre beca'),
(12, 1, 2, 280000.00, '2026-11-05', 'Transferencia', 'REC-2026-0025', 2026, 'IV', 'Pensión noviembre descuento hermanos'),
(13, 13, 2, 350000.00, '2026-11-06', 'Transferencia', 'REC-2026-0026', 2026, 'IV', 'Pensión noviembre'),
(14, 14, 2, 350000.00, '2026-09-10', 'Cheque', 'REC-2026-0027', 2026, 'III', 'Sin pago en octubre/noviembre'),
(15, 15, 2, 350000.00, '2026-11-06', 'Transferencia', 'REC-2026-0028', 2026, 'IV', 'Pensión noviembre'),
(16, 16, 2, 350000.00, '2026-11-07', 'Transferencia', 'REC-2026-0029', 2026, 'IV', 'Pensión noviembre'),
(17, 17, 2, 350000.00, '2026-11-07', 'Tarjeta Débito', 'REC-2026-0030', 2026, 'IV', 'Pensión noviembre'),
(18, 18, 2, 350000.00, '2026-09-15', 'Transferencia', 'REC-2026-0031', 2026, 'III', 'Sin pago reciente'),
(19, 19, 2, 175000.00, '2026-11-08', 'Transferencia', 'REC-2026-0032', 2026, 'IV', 'Pensión noviembre beca 50%'),
(20, 20, 2, 350000.00, '2026-11-08', 'Transferencia', 'REC-2026-0033', 2026, 'IV', 'Pensión noviembre'),
(31, 2, 2, 280000.00, '2026-11-02', 'Tarjeta Débito', 'REC-2026-0034', 2026, 'IV', 'Pensión noviembre hermanos'),
(35, 32, 2, 350000.00, '2026-11-09', 'Transferencia', 'REC-2026-0035', 2026, 'IV', 'Pensión noviembre');

-- 19. Facturas (10 sample invoice records)
INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_txt, ruta_archivo) VALUES
('FAC-000001', 1, '2026-01-15', 'a1b2c3d4e5f60001a1b2c3d4e5f60001a1b2c3d4e5f60001a1b2c3d4e5f60001', 'factura_000001.txt', 'invoices/factura_000001.txt'),
('FAC-000002', 2, '2026-01-15', 'a1b2c3d4e5f60002a1b2c3d4e5f60002a1b2c3d4e5f60002a1b2c3d4e5f60002', 'factura_000002.txt', 'invoices/factura_000002.txt'),
('FAC-000003', 14, '2026-11-01', 'a1b2c3d4e5f60003a1b2c3d4e5f60003a1b2c3d4e5f60003a1b2c3d4e5f60003', 'factura_000003.txt', 'invoices/factura_000003.txt'),
('FAC-000004', 15, '2026-11-02', 'a1b2c3d4e5f60004a1b2c3d4e5f60004a1b2c3d4e5f60004a1b2c3d4e5f60004', 'factura_000004.txt', 'invoices/factura_000004.txt'),
('FAC-000005', 16, '2026-11-02', 'a1b2c3d4e5f60005a1b2c3d4e5f60005a1b2c3d4e5f60005a1b2c3d4e5f60005', 'factura_000005.txt', 'invoices/factura_000005.txt'),
('FAC-000006', 18, '2026-11-03', 'a1b2c3d4e5f60006a1b2c3d4e5f60006a1b2c3d4e5f60006a1b2c3d4e5f60006', 'factura_000006.txt', 'invoices/factura_000006.txt'),
('FAC-000007', 24, '2026-11-05', 'a1b2c3d4e5f60007a1b2c3d4e5f60007a1b2c3d4e5f60007a1b2c3d4e5f60007', 'factura_000007.txt', 'invoices/factura_000007.txt'),
('FAC-000008', 25, '2026-11-05', 'a1b2c3d4e5f60008a1b2c3d4e5f60008a1b2c3d4e5f60008a1b2c3d4e5f60008', 'factura_000008.txt', 'invoices/factura_000008.txt'),
('FAC-000009', 32, '2026-11-08', 'a1b2c3d4e5f60009a1b2c3d4e5f60009a1b2c3d4e5f60009a1b2c3d4e5f60009', 'factura_000009.txt', 'invoices/factura_000009.txt'),
('FAC-000010', 35, '2026-11-09', 'a1b2c3d4e5f60010a1b2c3d4e5f60010a1b2c3d4e5f60010a1b2c3d4e5f60010', 'factura_000010.txt', 'invoices/factura_000010.txt');

-- 20. Categorías de Recurso (5 rows)
INSERT INTO categorias_recurso (nombre_categoria, descripcion) VALUES
('Libro', 'Libros de la biblioteca escolar'),
('Equipo de Laboratorio', 'Equipos científicos y de laboratorio (microscopios, balanzas, etc.)'),
('Equipo Deportivo', 'Equipos y materiales deportivos (balones, conos, etc.)'),
('Dispositivo Tecnológico', 'Tablets, calculadoras científicas, computadores portátiles'),
('Material Didáctico', 'Materiales educativos diversos (mapas, modelos, etc.)');

-- 21. Recursos (32 rows)
INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES
('LIB-001', 'Cien Años de Soledad', 1, 'Edición conmemorativa Gabriel García Márquez', '2023-02-10', 'Biblioteca - Estante A1', 'Disponible'),
('LIB-002', 'Álgebra de Baldor', 1, 'Texto clásico de álgebra escolar', '2023-02-10', 'Biblioteca - Estante B2', 'Prestado'),
('LIB-003', 'Cálculo de Stewart 8va Ed.', 1, 'Cálculo de una variable', '2024-01-20', 'Biblioteca - Estante B3', 'Disponible'),
('LIB-004', 'Biología de Campbell', 1, 'Manual integral de biología', '2024-01-20', 'Biblioteca - Estante C1', 'Prestado'),
('LIB-005', 'Don Quijote de la Mancha', 1, 'Edición escolar anotada', '2023-03-05', 'Biblioteca - Estante A2', 'Disponible'),
('LIB-006', 'Física Universitaria Sears Zemansky', 1, 'Volumen 1 Mecánica y Termodinámica', '2024-02-15', 'Biblioteca - Estante C2', 'Disponible'),
('LIB-007', 'Química General Petrucci', 1, 'Principios y aplicaciones modernas', '2024-02-15', 'Biblioteca - Estante C3', 'Disponible'),
('LIB-008', 'Diccionario Oxford Español-Inglés', 1, 'Edición bilingüe avanzada', '2023-05-12', 'Biblioteca - Estante D1', 'Disponible'),
('LAB-001', 'Microscopio Binocular Zeiss #1', 2, 'Microscopio óptico 1000x con iluminación LED', '2023-08-15', 'Laboratorio Biología', 'Prestado'),
('LAB-002', 'Microscopio Binocular Zeiss #2', 2, 'Microscopio óptico 1000x con iluminación LED', '2023-08-15', 'Laboratorio Biología', 'Disponible'),
('LAB-003', 'Balanza Analítica Ohaus', 2, 'Precisión 0.001g para prácticas químicas', '2024-03-01', 'Laboratorio Química', 'Disponible'),
('LAB-004', 'Kit de Destilación Borosilicato', 2, 'Set completo 12 piezas vidrio pirex', '2024-03-01', 'Laboratorio Química', 'En Mantenimiento'),
('LAB-005', 'Osciloscopio Digital Rigol', 2, 'Osciloscopio 2 canales 50MHz', '2024-04-10', 'Laboratorio Física', 'Prestado'),
('LAB-006', 'Multímetro Digital Fluke', 2, 'Medición de voltaje, corriente y resistencia', '2024-04-10', 'Laboratorio Física', 'Disponible'),
('LAB-007', 'Centrífuga de Laboratorio', 2, 'Centrífuga 6 tubos 4000 RPM', '2023-09-20', 'Laboratorio Biología', 'Disponible'),
('DEP-001', 'Balón Fútbol Profesional Golty #1', 3, 'Balón número 5 reglamentario', '2025-01-15', 'Bodega Deportiva', 'Prestado'),
('DEP-002', 'Balón Fútbol Profesional Golty #2', 3, 'Balón número 5 reglamentario', '2025-01-15', 'Bodega Deportiva', 'Disponible'),
('DEP-003', 'Balón Baloncesto Spalding #1', 3, 'Tamaño oficial 7', '2025-01-15', 'Bodega Deportiva', 'Disponible'),
('DEP-004', 'Set 20 Conos de Entrenamiento', 3, 'Conos plásticos alta visibilidad', '2025-01-15', 'Bodega Deportiva', 'Disponible'),
('DEP-005', 'Red de Voleibol Profesional', 3, 'Red con cable acerado', '2025-01-15', 'Bodega Deportiva', 'Disponible'),
('DEP-006', 'Tablero de Ajedrez Torneo #1', 3, 'Tablero reglamentario con reloj digital', '2024-06-10', 'Salón Lúdico', 'Disponible'),
('TEC-001', 'Tablet Samsung Galaxy Tab S9 #1', 4, 'Tablet educativa con lápiz óptico', '2025-02-01', 'Sala de Tecnología', 'Prestado'),
('TEC-002', 'Tablet Samsung Galaxy Tab S9 #2', 4, 'Tablet educativa con lápiz óptico', '2025-02-01', 'Sala de Tecnología', 'Disponible'),
('TEC-003', 'Calculadora Gráfica TI-84 Plus #1', 4, 'Calculadora programable para cálculo', '2024-02-20', 'Sala de Matemáticas', 'Prestado'),
('TEC-004', 'Calculadora Gráfica TI-84 Plus #2', 4, 'Calculadora programable para cálculo', '2024-02-20', 'Sala de Matemáticas', 'Disponible'),
('TEC-005', 'Kit Arduino Mega Robótica #1', 4, 'Kit completo sensores y actuadores', '2025-02-10', 'Laboratorio Robótica', 'Disponible'),
('TEC-006', 'Portátil Lenovo ThinkPad #1', 4, 'Portátil docente para presentaciones', '2024-08-05', 'Sala de Profesores', 'Prestado'),
('TEC-007', 'Proyector Epson Portátil', 4, 'Proyector HDMI 3600 lúmenes', '2023-11-12', 'Audiovisuales', 'Disponible'),
('DID-001', 'Modelo Anatómico Torso Humano', 5, 'Modelo desmontable 16 piezas', '2023-04-18', 'Laboratorio Biología', 'Disponible'),
('DID-002', 'Globo Terráqueo Político-Físico', 5, 'Escala 1:30.000.000 con iluminación', '2023-04-18', 'Salón Sociales', 'Disponible'),
('DID-003', 'Set Regletas Cuisenaire', 5, 'Material didáctico matemáticas primaria', '2024-01-25', 'Salón Primaria 2', 'Disponible'),
('DID-004', 'Mapa Mural de Colombia', 5, 'Mapa físico y político plastificado', '2024-01-25', 'Salón Sociales', 'Disponible');

-- 22. Préstamos a Estudiantes (10 rows, including active & overdue loans)
INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(2, 9, '2026-10-20', '2026-11-03', NULL, 'Activo', 'Préstamo vencido sin devolver para alerta'),
(4, 11, '2026-11-05', '2026-11-19', NULL, 'Activo', 'Préstamo vigente para proyecto'),
(16, 6, '2026-10-25', '2026-11-01', NULL, 'Activo', 'Préstamo vencido entrenamiento'),
(22, 10, '2026-11-08', '2026-11-15', NULL, 'Activo', 'Uso en club de robótica'),
(24, 12, '2026-11-01', '2026-11-10', NULL, 'Activo', 'Préstamo vencido para cálculo'),
(1, 5, '2026-10-01', '2026-10-15', '2026-10-14', 'Devuelto', 'Devuelto en buen estado'),
(3, 20, '2026-10-05', '2026-10-19', '2026-10-18', 'Devuelto', 'Devuelto a tiempo'),
(5, 7, '2026-10-10', '2026-10-24', '2026-10-23', 'Devuelto', 'Sin novedades'),
(8, 25, '2026-09-15', '2026-09-29', '2026-09-28', 'Devuelto', 'Sin novedades'),
(17, 8, '2026-09-20', '2026-09-27', '2026-09-27', 'Devuelto', 'Devuelto después de práctica');

-- 23. Préstamos a Profesores (8 rows, including active & overdue loans)
INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(9, 12, '2026-11-06', '2026-11-20', NULL, 'Activo', 'Prácticas grado 7 y 9'),
(13, 5, '2026-10-20', '2026-11-04', NULL, 'Activo', 'Préstamo vencido práctica física'),
(27, 16, '2026-11-08', '2026-11-18', NULL, 'Activo', 'Presentaciones clase inglés'),
(28, 1, '2026-10-01', '2026-10-05', '2026-10-05', 'Devuelto', 'Devuelto en sala audiovisuales'),
(10, 3, '2026-10-10', '2026-10-17', '2026-10-16', 'Devuelto', 'Práctica biología'),
(14, 5, '2026-10-12', '2026-10-19', '2026-10-18', 'Devuelto', 'Mediciones eléctricas'),
(29, 12, '2026-09-10', '2026-09-24', '2026-09-22', 'Devuelto', 'Clase anatomía'),
(30, 17, '2026-09-12', '2026-09-26', '2026-09-25', 'Devuelto', 'Clase geografía');

-- ============================================================================
-- FINAL VERIFICATION
-- ============================================================================
SELECT TABLE_NAME AS 'Tabla Creada', TABLE_ROWS AS 'Filas Estimadas'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'erp_little_horizons'
ORDER BY TABLE_NAME;
