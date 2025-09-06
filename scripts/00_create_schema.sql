-- CrediActiva Database Schema
-- MySQL 8+ con zona horaria America/Lima
-- Codificación UTF-8, moneda PEN (S/.)

-- Crear base de datos
DROP DATABASE IF EXISTS crediactiva;
CREATE DATABASE crediactiva 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE crediactiva;

-- Configurar zona horaria
SET time_zone = '-05:00'; -- America/Lima

-- Tabla: roles
CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roles_nombre (nombre),
    INDEX idx_roles_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuarios
CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE,
    telefono VARCHAR(20),
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    ultimo_login TIMESTAMP NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_usuarios_username (username),
    INDEX idx_usuarios_email (email),
    INDEX idx_usuarios_documento (documento_identidad),
    INDEX idx_usuarios_activo (activo),
    INDEX idx_usuarios_nombres_apellidos (nombres, apellidos)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuarios_roles (relación muchos a muchos)
CREATE TABLE usuarios_roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    rol_id INT NOT NULL,
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_usuario_rol (usuario_id, rol_id),
    INDEX idx_usuarios_roles_usuario (usuario_id),
    INDEX idx_usuarios_roles_rol (rol_id),
    INDEX idx_usuarios_roles_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: asesores (información específica de asesores)
CREATE TABLE asesores (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL UNIQUE,
    codigo_asesor VARCHAR(20) NOT NULL UNIQUE,
    comision_porcentaje DECIMAL(5,4) NOT NULL DEFAULT 0.0200, -- 2% por defecto
    meta_mensual DECIMAL(12,2) DEFAULT 0.00,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_asesores_codigo (codigo_asesor),
    INDEX idx_asesores_usuario (usuario_id),
    INDEX idx_asesores_activo (activo),
    
    CONSTRAINT chk_comision_porcentaje CHECK (comision_porcentaje >= 0 AND comision_porcentaje <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: clientes (información específica de clientes)
CREATE TABLE clientes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL UNIQUE,
    codigo_cliente VARCHAR(20) NOT NULL UNIQUE,
    tipo_cliente ENUM('NUEVO', 'RECURRENTE', 'VIP') DEFAULT 'NUEVO',
    limite_credito DECIMAL(12,2) DEFAULT 0.00,
    score_crediticio INT DEFAULT 0, -- 0-1000
    ingresos_declarados DECIMAL(12,2) DEFAULT 0.00,
    ocupacion VARCHAR(100),
    empresa VARCHAR(100),
    referencias_personales TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_clientes_codigo (codigo_cliente),
    INDEX idx_clientes_usuario (usuario_id),
    INDEX idx_clientes_tipo (tipo_cliente),
    INDEX idx_clientes_activo (activo),
    INDEX idx_clientes_score (score_crediticio),
    
    CONSTRAINT chk_limite_credito CHECK (limite_credito >= 0),
    CONSTRAINT chk_score_crediticio CHECK (score_crediticio >= 0 AND score_crediticio <= 1000),
    CONSTRAINT chk_ingresos_declarados CHECK (ingresos_declarados >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: solicitudes
CREATE TABLE solicitudes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero_solicitud VARCHAR(20) NOT NULL UNIQUE,
    cliente_id INT NULL, -- Puede ser NULL si el cliente no existe aún
    asesor_id INT NOT NULL,
    nombres_cliente VARCHAR(100) NOT NULL,
    apellidos_cliente VARCHAR(100) NOT NULL,
    documento_cliente VARCHAR(20) NOT NULL,
    telefono_cliente VARCHAR(20),
    email_cliente VARCHAR(100),
    direccion_cliente TEXT,
    monto_solicitado DECIMAL(12,2) NOT NULL,
    plazo_meses INT NOT NULL,
    tasa_interes_mensual DECIMAL(5,4) NOT NULL,
    finalidad TEXT,
    ingresos_mensuales DECIMAL(12,2),
    estado ENUM('PENDIENTE', 'EN_REVISION', 'APROBADA', 'RECHAZADA', 'CANCELADA') DEFAULT 'PENDIENTE',
    observaciones TEXT,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_revision TIMESTAMP NULL,
    fecha_decision TIMESTAMP NULL,
    revisado_por INT NULL,
    
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (asesor_id) REFERENCES asesores(id) ON DELETE RESTRICT,
    FOREIGN KEY (revisado_por) REFERENCES usuarios(id) ON DELETE SET NULL,
    
    INDEX idx_solicitudes_numero (numero_solicitud),
    INDEX idx_solicitudes_cliente (cliente_id),
    INDEX idx_solicitudes_asesor (asesor_id),
    INDEX idx_solicitudes_documento_cliente (documento_cliente),
    INDEX idx_solicitudes_estado (estado),
    INDEX idx_solicitudes_fecha_solicitud (fecha_solicitud),
    INDEX idx_solicitudes_monto (monto_solicitado),
    
    CONSTRAINT chk_monto_solicitado CHECK (monto_solicitado > 0),
    CONSTRAINT chk_plazo_meses CHECK (plazo_meses > 0 AND plazo_meses <= 120),
    CONSTRAINT chk_tasa_interes CHECK (tasa_interes_mensual >= 0 AND tasa_interes_mensual <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: prestamos
CREATE TABLE prestamos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero_prestamo VARCHAR(20) NOT NULL UNIQUE,
    solicitud_id INT NOT NULL,
    cliente_id INT NOT NULL,
    asesor_id INT NOT NULL,
    monto_prestamo DECIMAL(12,2) NOT NULL,
    monto_total DECIMAL(12,2) NOT NULL, -- Monto + intereses
    plazo_meses INT NOT NULL,
    tasa_interes_mensual DECIMAL(5,4) NOT NULL,
    cuota_mensual DECIMAL(12,2) NOT NULL,
    estado ENUM('ACTIVO', 'PAGADO', 'VENCIDO', 'CANCELADO') DEFAULT 'ACTIVO',
    fecha_desembolso DATE NOT NULL,
    fecha_primer_vencimiento DATE NOT NULL,
    fecha_ultimo_vencimiento DATE NOT NULL,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (solicitud_id) REFERENCES solicitudes(id) ON DELETE RESTRICT,
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    FOREIGN KEY (asesor_id) REFERENCES asesores(id) ON DELETE RESTRICT,
    
    INDEX idx_prestamos_numero (numero_prestamo),
    INDEX idx_prestamos_solicitud (solicitud_id),
    INDEX idx_prestamos_cliente (cliente_id),
    INDEX idx_prestamos_asesor (asesor_id),
    INDEX idx_prestamos_estado (estado),
    INDEX idx_prestamos_fecha_desembolso (fecha_desembolso),
    INDEX idx_prestamos_monto (monto_prestamo),
    
    CONSTRAINT chk_prestamo_monto CHECK (monto_prestamo > 0),
    CONSTRAINT chk_prestamo_monto_total CHECK (monto_total >= monto_prestamo),
    CONSTRAINT chk_prestamo_cuota CHECK (cuota_mensual > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: cronograma_pagos
CREATE TABLE cronograma_pagos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prestamo_id INT NOT NULL,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    monto_cuota DECIMAL(12,2) NOT NULL,
    capital DECIMAL(12,2) NOT NULL,
    interes DECIMAL(12,2) NOT NULL,
    saldo_pendiente DECIMAL(12,2) NOT NULL,
    pagado BOOLEAN DEFAULT FALSE,
    fecha_pago DATE NULL,
    monto_pagado DECIMAL(12,2) DEFAULT 0.00,
    dias_atraso INT DEFAULT 0,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (prestamo_id) REFERENCES prestamos(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_prestamo_cuota (prestamo_id, numero_cuota),
    INDEX idx_cronograma_prestamo (prestamo_id),
    INDEX idx_cronograma_fecha_vencimiento (fecha_vencimiento),
    INDEX idx_cronograma_pagado (pagado),
    INDEX idx_cronograma_numero_cuota (numero_cuota),
    
    CONSTRAINT chk_numero_cuota CHECK (numero_cuota > 0),
    CONSTRAINT chk_monto_cuota_cronograma CHECK (monto_cuota > 0),
    CONSTRAINT chk_monto_pagado CHECK (monto_pagado >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: pagos
CREATE TABLE pagos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero_recibo VARCHAR(20) NOT NULL UNIQUE,
    prestamo_id INT NOT NULL,
    cronograma_id INT NOT NULL,
    monto_pago DECIMAL(12,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('EFECTIVO', 'TRANSFERENCIA', 'CHEQUE', 'DEPOSITO') DEFAULT 'EFECTIVO',
    numero_operacion VARCHAR(50),
    observaciones TEXT,
    registrado_por INT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (prestamo_id) REFERENCES prestamos(id) ON DELETE RESTRICT,
    FOREIGN KEY (cronograma_id) REFERENCES cronograma_pagos(id) ON DELETE RESTRICT,
    FOREIGN KEY (registrado_por) REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    INDEX idx_pagos_numero_recibo (numero_recibo),
    INDEX idx_pagos_prestamo (prestamo_id),
    INDEX idx_pagos_cronograma (cronograma_id),
    INDEX idx_pagos_fecha (fecha_pago),
    INDEX idx_pagos_monto (monto_pago),
    INDEX idx_pagos_metodo (metodo_pago),
    
    CONSTRAINT chk_monto_pago CHECK (monto_pago > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: auditoria (para tracking de cambios importantes)
CREATE TABLE auditoria (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tabla_afectada VARCHAR(50) NOT NULL,
    registro_id INT NOT NULL,
    accion ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    datos_anteriores JSON NULL,
    datos_nuevos JSON NULL,
    usuario_id INT NOT NULL,
    ip_address VARCHAR(45),
    fecha_accion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    INDEX idx_auditoria_tabla (tabla_afectada),
    INDEX idx_auditoria_registro (registro_id),
    INDEX idx_auditoria_usuario (usuario_id),
    INDEX idx_auditoria_fecha (fecha_accion),
    INDEX idx_auditoria_accion (accion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Crear usuario específico para la aplicación
CREATE USER IF NOT EXISTS 'crediactiva_user'@'localhost' IDENTIFIED BY 'crediactiva_pass';
GRANT SELECT, INSERT, UPDATE, DELETE ON crediactiva.* TO 'crediactiva_user'@'localhost';
FLUSH PRIVILEGES;

-- Comentarios para documentación
ALTER TABLE usuarios COMMENT = 'Tabla principal de usuarios del sistema';
ALTER TABLE roles COMMENT = 'Catálogo de roles disponibles en el sistema';
ALTER TABLE asesores COMMENT = 'Información específica de usuarios con rol asesor';
ALTER TABLE solicitudes COMMENT = 'Solicitudes de préstamo realizadas por clientes';
ALTER TABLE prestamos COMMENT = 'Préstamos aprobados y desembolsados';
ALTER TABLE cronograma_pagos COMMENT = 'Cronograma de pagos de cada préstamo';
ALTER TABLE pagos COMMENT = 'Registro de pagos realizados por los clientes';
ALTER TABLE auditoria COMMENT = 'Registro de auditoría de cambios en el sistema';


