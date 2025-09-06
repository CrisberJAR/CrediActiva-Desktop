-- Datos iniciales para CrediActiva
-- Incluye usuarios por defecto, roles y datos de ejemplo

USE crediactiva;

-- Insertar roles
INSERT INTO roles (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Acceso completo al sistema, gestión de usuarios y aprobación de préstamos'),
('ASESOR', 'Creación de solicitudes, gestión de clientes y seguimiento de préstamos'),
('CLIENTE', 'Acceso a información personal de préstamos y solicitudes');

-- Insertar usuarios (contraseñas hasheadas con BCrypt)
-- Contraseña para todos: "admin123", "asesor123", "cliente123" respectivamente
INSERT INTO usuarios (username, email, password_hash, nombres, apellidos, documento_identidad, telefono, direccion, activo) VALUES
('admin', 'admin@crediactiva.pe', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyc6YRqGQUugt6wLtXXTBe', 'Administrador', 'Sistema', '12345678', '999999999', 'Av. Principal 123, Lima', TRUE),
('asesor1', 'asesor1@crediactiva.pe', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Carlos', 'Mendoza', '87654321', '988888888', 'Jr. Los Asesores 456, Lima', TRUE),
('cliente1', 'cliente1@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'María', 'García', '11111111', '977777777', 'Av. Los Clientes 789, Lima', TRUE),
('cliente2', 'cliente2@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Juan', 'Pérez', '22222222', '966666666', 'Jr. San Martín 321, Lima', TRUE),
('cliente3', 'cliente3@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Ana', 'López', '33333333', '955555555', 'Av. Arequipa 654, Lima', TRUE);

-- Asignar roles a usuarios
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES
(1, 1), -- admin -> ADMINISTRADOR
(2, 2), -- asesor1 -> ASESOR
(3, 3), -- cliente1 -> CLIENTE
(4, 3), -- cliente2 -> CLIENTE
(5, 3); -- cliente3 -> CLIENTE

-- Insertar información de asesor
INSERT INTO asesores (usuario_id, codigo_asesor, comision_porcentaje, meta_mensual) VALUES
(2, 'ASE001', 0.0250, 50000.00); -- 2.5% de comisión, meta de S/ 50,000

-- Insertar información de clientes
INSERT INTO clientes (usuario_id, codigo_cliente, tipo_cliente, limite_credito, score_crediticio, ingresos_declarados, ocupacion, empresa) VALUES
(3, 'CLI001', 'RECURRENTE', 25000.00, 750, 3000.00, 'Comerciante', 'Negocio Propio'),
(4, 'CLI002', 'NUEVO', 20000.00, 680, 4500.00, 'Empleado', 'Empresa ABC SAC'),
(5, 'CLI003', 'NUEVO', 15000.00, 720, 2800.00, 'Independiente', 'Consultoría');

-- Insertar solicitudes de ejemplo
INSERT INTO solicitudes (
    numero_solicitud, cliente_id, asesor_id, nombres_cliente, apellidos_cliente, 
    documento_cliente, telefono_cliente, email_cliente, direccion_cliente,
    monto_solicitado, plazo_meses, tasa_interes_mensual, finalidad, ingresos_mensuales, estado
) VALUES
('SOL-2024-001', 3, 1, 'María', 'García', '11111111', '977777777', 'cliente1@email.com', 'Av. Los Clientes 789, Lima', 
 10000.00, 12, 0.0250, 'Capital de trabajo para negocio', 3000.00, 'APROBADA'),
 
('SOL-2024-002', 4, 1, 'Juan', 'Pérez', '22222222', '966666666', 'cliente2@email.com', 'Jr. San Martín 321, Lima', 
 15000.00, 18, 0.0220, 'Compra de mercadería', 4500.00, 'PENDIENTE'),
 
('SOL-2024-003', NULL, 1, 'Pedro', 'Rodríguez', '44444444', '944444444', 'pedro@email.com', 'Av. Brasil 987, Lima', 
 8000.00, 6, 0.0280, 'Gastos médicos', 2500.00, 'EN_REVISION');

-- Insertar un préstamo aprobado (basado en la primera solicitud)
INSERT INTO prestamos (
    numero_prestamo, solicitud_id, cliente_id, asesor_id, monto_prestamo, monto_total,
    plazo_meses, tasa_interes_mensual, cuota_mensual, fecha_desembolso, 
    fecha_primer_vencimiento, fecha_ultimo_vencimiento, estado
) VALUES
('PRES-2024-001', 1, 3, 1, 10000.00, 13000.00, 12, 0.0250, 1083.33, 
 '2024-01-15', '2024-02-15', '2025-01-15', 'ACTIVO');

-- Insertar cronograma de pagos para el préstamo
-- Generamos las 12 cuotas con fechas que excluyen domingos
INSERT INTO cronograma_pagos (
    prestamo_id, numero_cuota, fecha_vencimiento, monto_cuota, capital, interes, saldo_pendiente
) VALUES
(1, 1, '2024-02-15', 1083.33, 833.33, 250.00, 9166.67),
(1, 2, '2024-03-15', 1083.33, 854.17, 229.17, 8312.50),
(1, 3, '2024-04-15', 1083.33, 875.52, 207.81, 7436.98),
(1, 4, '2024-05-15', 1083.33, 897.40, 185.93, 6539.58),
(1, 5, '2024-06-17', 1083.33, 919.83, 163.50, 5619.75), -- 16 era domingo
(1, 6, '2024-07-15', 1083.33, 942.82, 140.51, 4676.93),
(1, 7, '2024-08-15', 1083.33, 966.39, 116.94, 3710.54),
(1, 8, '2024-09-16', 1083.33, 990.55, 92.78, 2719.99), -- 15 era domingo
(1, 9, '2024-10-15', 1083.33, 1015.31, 68.02, 1704.68),
(1, 10, '2024-11-15', 1083.33, 1040.69, 42.64, 663.99),
(1, 11, '2024-12-16', 1083.33, 1066.71, 16.62, -402.72), -- 15 era domingo
(1, 12, '2025-01-15', 686.61, 663.99, 22.62, 0.00); -- Última cuota ajustada

-- Insertar algunos pagos de ejemplo
INSERT INTO pagos (
    numero_recibo, prestamo_id, cronograma_id, monto_pago, fecha_pago, 
    metodo_pago, numero_operacion, observaciones, registrado_por
) VALUES
('REC-2024-001', 1, 1, 1083.33, '2024-02-15', 'TRANSFERENCIA', 'OP123456789', 'Pago puntual primera cuota', 2),
('REC-2024-002', 1, 2, 1083.33, '2024-03-15', 'EFECTIVO', NULL, 'Pago puntual segunda cuota', 2),
('REC-2024-003', 1, 3, 1083.33, '2024-04-20', 'DEPOSITO', 'DEP987654321', 'Pago con 5 días de atraso', 2);

-- Actualizar estado de cuotas pagadas
UPDATE cronograma_pagos SET 
    pagado = TRUE, 
    fecha_pago = '2024-02-15', 
    monto_pagado = 1083.33, 
    dias_atraso = 0 
WHERE id = 1;

UPDATE cronograma_pagos SET 
    pagado = TRUE, 
    fecha_pago = '2024-03-15', 
    monto_pagado = 1083.33, 
    dias_atraso = 0 
WHERE id = 2;

UPDATE cronograma_pagos SET 
    pagado = TRUE, 
    fecha_pago = '2024-04-20', 
    monto_pagado = 1083.33, 
    dias_atraso = 5 
WHERE id = 3;

-- Actualizar fecha de revisión y decisión de solicitudes
UPDATE solicitudes SET 
    fecha_revision = '2024-01-10 10:30:00',
    fecha_decision = '2024-01-10 14:15:00',
    revisado_por = 1,
    observaciones = 'Solicitud aprobada. Cliente con buen historial crediticio.'
WHERE id = 1;

UPDATE solicitudes SET 
    fecha_revision = '2024-01-12 09:00:00',
    observaciones = 'En proceso de verificación de ingresos.'
WHERE id = 3;

-- Insertar registros de auditoría de ejemplo
INSERT INTO auditoria (tabla_afectada, registro_id, accion, usuario_id, ip_address) VALUES
('prestamos', 1, 'INSERT', 1, '127.0.0.1'),
('cronograma_pagos', 1, 'INSERT', 1, '127.0.0.1'),
('pagos', 1, 'INSERT', 2, '192.168.1.100'),
('pagos', 2, 'INSERT', 2, '192.168.1.100'),
('pagos', 3, 'INSERT', 2, '192.168.1.100');

-- Comentario informativo
SELECT 'Datos iniciales insertados correctamente' as Mensaje,
       COUNT(*) as Total_Usuarios FROM usuarios
UNION ALL
SELECT 'Total Roles', COUNT(*) FROM roles
UNION ALL
SELECT 'Total Asesores', COUNT(*) FROM asesores
UNION ALL
SELECT 'Total Solicitudes', COUNT(*) FROM solicitudes
UNION ALL
SELECT 'Total Préstamos', COUNT(*) FROM prestamos
UNION ALL
SELECT 'Total Cuotas Cronograma', COUNT(*) FROM cronograma_pagos
UNION ALL
SELECT 'Total Pagos Registrados', COUNT(*) FROM pagos;


