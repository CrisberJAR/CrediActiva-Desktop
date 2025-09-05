-- Vistas y procedimientos almacenados para CrediActiva
-- Incluye vistas para cálculos dinámicos y procedimientos útiles

USE crediactiva;

-- Vista: Estado dinámico de cuotas
DROP VIEW IF EXISTS vista_estado_cuotas;
CREATE VIEW vista_estado_cuotas AS
SELECT 
    cp.id,
    cp.prestamo_id,
    cp.numero_cuota,
    cp.fecha_vencimiento,
    cp.monto_cuota,
    cp.capital,
    cp.interes,
    cp.saldo_pendiente,
    cp.pagado,
    cp.fecha_pago,
    cp.monto_pagado,
    cp.dias_atraso,
    CASE 
        WHEN cp.pagado = TRUE THEN 'PAGADO'
        WHEN CURDATE() <= cp.fecha_vencimiento THEN 'PUNTUAL'
        WHEN DATEDIFF(CURDATE(), cp.fecha_vencimiento) BETWEEN 1 AND 7 THEN 'ATRASADO'
        WHEN DATEDIFF(CURDATE(), cp.fecha_vencimiento) > 7 THEN 'MUY_ATRASADO'
        ELSE 'PUNTUAL'
    END AS estado_cuota,
    CASE 
        WHEN cp.pagado = FALSE AND CURDATE() > cp.fecha_vencimiento 
        THEN DATEDIFF(CURDATE(), cp.fecha_vencimiento)
        ELSE 0
    END AS dias_atraso_actual,
    p.numero_prestamo,
    p.cliente_id,
    CONCAT(u.nombres, ' ', u.apellidos) AS nombre_cliente
FROM cronograma_pagos cp
JOIN prestamos p ON cp.prestamo_id = p.id
JOIN usuarios u ON p.cliente_id = u.id;

-- Vista: Deuda actual de préstamos
DROP VIEW IF EXISTS vista_deuda_prestamos;
CREATE VIEW vista_deuda_prestamos AS
SELECT 
    p.id AS prestamo_id,
    p.numero_prestamo,
    p.cliente_id,
    CONCAT(u.nombres, ' ', u.apellidos) AS nombre_cliente,
    p.monto_prestamo,
    p.monto_total,
    p.estado,
    COALESCE(SUM(pg.monto_pago), 0) AS total_pagado,
    (p.monto_total - COALESCE(SUM(pg.monto_pago), 0)) AS deuda_actual,
    COUNT(CASE WHEN cp.pagado = FALSE AND CURDATE() > cp.fecha_vencimiento THEN 1 END) AS cuotas_vencidas,
    COUNT(CASE WHEN cp.pagado = FALSE THEN 1 END) AS cuotas_pendientes,
    MAX(CASE WHEN cp.pagado = FALSE AND CURDATE() > cp.fecha_vencimiento 
            THEN DATEDIFF(CURDATE(), cp.fecha_vencimiento) ELSE 0 END) AS max_dias_atraso
FROM prestamos p
JOIN usuarios u ON p.cliente_id = u.id
LEFT JOIN cronograma_pagos cp ON p.id = cp.prestamo_id
LEFT JOIN pagos pg ON p.id = pg.prestamo_id
GROUP BY p.id, p.numero_prestamo, p.cliente_id, u.nombres, u.apellidos, 
         p.monto_prestamo, p.monto_total, p.estado;

-- Vista: Resumen de préstamos por asesor
DROP VIEW IF EXISTS vista_prestamos_asesor;
CREATE VIEW vista_prestamos_asesor AS
SELECT 
    a.id AS asesor_id,
    a.codigo_asesor,
    CONCAT(u.nombres, ' ', u.apellidos) AS nombre_asesor,
    COUNT(p.id) AS total_prestamos,
    SUM(p.monto_prestamo) AS monto_total_prestamos,
    SUM(CASE WHEN p.estado = 'ACTIVO' THEN 1 ELSE 0 END) AS prestamos_activos,
    SUM(CASE WHEN p.estado = 'PAGADO' THEN 1 ELSE 0 END) AS prestamos_pagados,
    SUM(CASE WHEN p.estado = 'VENCIDO' THEN 1 ELSE 0 END) AS prestamos_vencidos,
    a.comision_porcentaje,
    a.meta_mensual
FROM asesores a
JOIN usuarios u ON a.usuario_id = u.id
LEFT JOIN prestamos p ON a.id = p.asesor_id
GROUP BY a.id, a.codigo_asesor, u.nombres, u.apellidos, a.comision_porcentaje, a.meta_mensual;

-- Vista: Comisiones de asesores por período
DROP VIEW IF EXISTS vista_comisiones_asesor;
CREATE VIEW vista_comisiones_asesor AS
SELECT 
    a.id AS asesor_id,
    a.codigo_asesor,
    CONCAT(u.nombres, ' ', u.apellidos) AS nombre_asesor,
    YEAR(pg.fecha_pago) AS año,
    MONTH(pg.fecha_pago) AS mes,
    SUM(pg.monto_pago) AS total_cobrado,
    SUM(pg.monto_pago * a.comision_porcentaje) AS comision_generada,
    COUNT(pg.id) AS cantidad_pagos,
    a.comision_porcentaje
FROM asesores a
JOIN usuarios u ON a.usuario_id = u.id
JOIN prestamos p ON a.id = p.asesor_id
JOIN pagos pg ON p.id = pg.prestamo_id
GROUP BY a.id, a.codigo_asesor, u.nombres, u.apellidos, 
         YEAR(pg.fecha_pago), MONTH(pg.fecha_pago), a.comision_porcentaje;

-- Vista: Dashboard de administrador
DROP VIEW IF EXISTS vista_dashboard_admin;
CREATE VIEW vista_dashboard_admin AS
SELECT 
    'RESUMEN_GENERAL' as tipo_dato,
    (SELECT COUNT(*) FROM usuarios WHERE activo = TRUE) as total_usuarios,
    (SELECT COUNT(*) FROM asesores WHERE activo = TRUE) as total_asesores,
    (SELECT COUNT(*) FROM solicitudes WHERE estado = 'PENDIENTE') as solicitudes_pendientes,
    (SELECT COUNT(*) FROM prestamos WHERE estado = 'ACTIVO') as prestamos_activos,
    (SELECT COALESCE(SUM(monto_prestamo), 0) FROM prestamos WHERE estado = 'ACTIVO') as monto_total_prestamos,
    (SELECT COUNT(*) FROM cronograma_pagos WHERE pagado = FALSE AND CURDATE() > fecha_vencimiento) as cuotas_vencidas,
    (SELECT COALESCE(SUM(monto_pago), 0) FROM pagos WHERE DATE(fecha_pago) = CURDATE()) as cobros_hoy;

-- Procedimiento: Generar número de solicitud
DELIMITER $$
DROP PROCEDURE IF EXISTS GenerarNumeroSolicitud$$
CREATE PROCEDURE GenerarNumeroSolicitud(OUT numero_solicitud VARCHAR(20))
BEGIN
    DECLARE contador INT;
    DECLARE año_actual YEAR DEFAULT YEAR(CURDATE());
    
    SELECT COUNT(*) + 1 INTO contador 
    FROM solicitudes 
    WHERE YEAR(fecha_solicitud) = año_actual;
    
    SET numero_solicitud = CONCAT('SOL-', año_actual, '-', LPAD(contador, 3, '0'));
END$$

-- Procedimiento: Generar número de préstamo
DROP PROCEDURE IF EXISTS GenerarNumeroPrestamo$$
CREATE PROCEDURE GenerarNumeroPrestamo(OUT numero_prestamo VARCHAR(20))
BEGIN
    DECLARE contador INT;
    DECLARE año_actual YEAR DEFAULT YEAR(CURDATE());
    
    SELECT COUNT(*) + 1 INTO contador 
    FROM prestamos 
    WHERE YEAR(fecha_creacion) = año_actual;
    
    SET numero_prestamo = CONCAT('PRES-', año_actual, '-', LPAD(contador, 3, '0'));
END$$

-- Procedimiento: Generar número de recibo
DROP PROCEDURE IF EXISTS GenerarNumeroRecibo$$
CREATE PROCEDURE GenerarNumeroRecibo(OUT numero_recibo VARCHAR(20))
BEGIN
    DECLARE contador INT;
    DECLARE año_actual YEAR DEFAULT YEAR(CURDATE());
    
    SELECT COUNT(*) + 1 INTO contador 
    FROM pagos 
    WHERE YEAR(fecha_creacion) = año_actual;
    
    SET numero_recibo = CONCAT('REC-', año_actual, '-', LPAD(contador, 3, '0'));
END$$

-- Procedimiento: Calcular siguiente fecha sin domingo
DROP PROCEDURE IF EXISTS CalcularFechaSinDomingo$$
CREATE PROCEDURE CalcularFechaSinDomingo(IN fecha_base DATE, OUT fecha_ajustada DATE)
BEGIN
    SET fecha_ajustada = fecha_base;
    
    -- Si es domingo (DAYOFWEEK = 1), mover al lunes
    IF DAYOFWEEK(fecha_ajustada) = 1 THEN
        SET fecha_ajustada = DATE_ADD(fecha_ajustada, INTERVAL 1 DAY);
    END IF;
END$$

-- Procedimiento: Generar cronograma de pagos
DROP PROCEDURE IF EXISTS GenerarCronogramaPagos$$
CREATE PROCEDURE GenerarCronogramaPagos(
    IN p_prestamo_id INT,
    IN p_monto_prestamo DECIMAL(12,2),
    IN p_tasa_mensual DECIMAL(5,4),
    IN p_plazo_meses INT,
    IN p_fecha_inicio DATE
)
BEGIN
    DECLARE v_cuota INT DEFAULT 1;
    DECLARE v_fecha_vencimiento DATE;
    DECLARE v_monto_cuota DECIMAL(12,2);
    DECLARE v_capital DECIMAL(12,2);
    DECLARE v_interes DECIMAL(12,2);
    DECLARE v_saldo DECIMAL(12,2);
    DECLARE v_factor_pago DECIMAL(15,10);
    
    -- Calcular cuota fija (sistema francés)
    SET v_factor_pago = POWER(1 + p_tasa_mensual, p_plazo_meses);
    SET v_monto_cuota = p_monto_prestamo * (p_tasa_mensual * v_factor_pago) / (v_factor_pago - 1);
    SET v_saldo = p_monto_prestamo;
    
    -- Limpiar cronograma existente si existe
    DELETE FROM cronograma_pagos WHERE prestamo_id = p_prestamo_id;
    
    -- Generar cada cuota
    WHILE v_cuota <= p_plazo_meses DO
        -- Calcular fecha de vencimiento (agregar meses y ajustar domingo)
        SET v_fecha_vencimiento = DATE_ADD(p_fecha_inicio, INTERVAL v_cuota MONTH);
        
        -- Ajustar si cae domingo
        IF DAYOFWEEK(v_fecha_vencimiento) = 1 THEN
            SET v_fecha_vencimiento = DATE_ADD(v_fecha_vencimiento, INTERVAL 1 DAY);
        END IF;
        
        -- Calcular interés y capital
        SET v_interes = v_saldo * p_tasa_mensual;
        SET v_capital = v_monto_cuota - v_interes;
        
        -- Ajustar última cuota para evitar diferencias por redondeo
        IF v_cuota = p_plazo_meses THEN
            SET v_capital = v_saldo;
            SET v_monto_cuota = v_capital + v_interes;
        END IF;
        
        -- Insertar cuota en cronograma
        INSERT INTO cronograma_pagos (
            prestamo_id, numero_cuota, fecha_vencimiento, monto_cuota, 
            capital, interes, saldo_pendiente
        ) VALUES (
            p_prestamo_id, v_cuota, v_fecha_vencimiento, v_monto_cuota,
            v_capital, v_interes, v_saldo - v_capital
        );
        
        -- Actualizar saldo y contador
        SET v_saldo = v_saldo - v_capital;
        SET v_cuota = v_cuota + 1;
    END WHILE;
END$$

-- Procedimiento: Actualizar estado de préstamo
DROP PROCEDURE IF EXISTS ActualizarEstadoPrestamo$$
CREATE PROCEDURE ActualizarEstadoPrestamo(IN p_prestamo_id INT)
BEGIN
    DECLARE v_cuotas_pendientes INT;
    DECLARE v_cuotas_vencidas INT;
    DECLARE v_nuevo_estado VARCHAR(20);
    
    -- Contar cuotas pendientes y vencidas
    SELECT 
        COUNT(CASE WHEN pagado = FALSE THEN 1 END),
        COUNT(CASE WHEN pagado = FALSE AND CURDATE() > fecha_vencimiento THEN 1 END)
    INTO v_cuotas_pendientes, v_cuotas_vencidas
    FROM cronograma_pagos 
    WHERE prestamo_id = p_prestamo_id;
    
    -- Determinar nuevo estado
    IF v_cuotas_pendientes = 0 THEN
        SET v_nuevo_estado = 'PAGADO';
    ELSEIF v_cuotas_vencidas > 0 THEN
        SET v_nuevo_estado = 'VENCIDO';
    ELSE
        SET v_nuevo_estado = 'ACTIVO';
    END IF;
    
    -- Actualizar estado del préstamo
    UPDATE prestamos 
    SET estado = v_nuevo_estado, fecha_actualizacion = CURRENT_TIMESTAMP
    WHERE id = p_prestamo_id;
END$$

DELIMITER ;

-- Crear índices adicionales para optimizar las vistas
CREATE INDEX idx_cronograma_fecha_vencimiento_pagado ON cronograma_pagos(fecha_vencimiento, pagado);
CREATE INDEX idx_pagos_fecha_pago ON pagos(fecha_pago);
CREATE INDEX idx_prestamos_estado_asesor ON prestamos(estado, asesor_id);

-- Comentarios sobre las vistas creadas
SELECT 'Vistas y procedimientos creados correctamente' as Mensaje
UNION ALL
SELECT 'Vista vista_estado_cuotas: Estados dinámicos de cuotas'
UNION ALL
SELECT 'Vista vista_deuda_prestamos: Deuda actual por préstamo'
UNION ALL
SELECT 'Vista vista_prestamos_asesor: Resumen por asesor'
UNION ALL
SELECT 'Vista vista_comisiones_asesor: Comisiones por período'
UNION ALL
SELECT 'Procedimientos: Generación de números y cronogramas';


