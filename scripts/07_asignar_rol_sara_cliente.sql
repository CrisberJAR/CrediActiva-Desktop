-- Script para asignar rol CLIENTE a Sara
-- Ejecutar estos comandos en tu base de datos MySQL

USE crediactiva;

-- Paso 1: Verificar que Sara existe en la tabla usuarios
SELECT id, username, nombres, apellidos, email, activo 
FROM usuarios 
WHERE nombres LIKE '%Sara%' OR username LIKE '%sara%';

-- Paso 2: Obtener el ID del rol CLIENTE
SELECT id, nombre, descripcion 
FROM roles 
WHERE nombre = 'CLIENTE';

-- Paso 3: Asignar el rol CLIENTE a Sara
INSERT INTO usuarios_roles (usuario_id, rol_id, activo) 
SELECT u.id, r.id, TRUE
FROM usuarios u, roles r 
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%')
  AND r.nombre = 'CLIENTE'
  AND NOT EXISTS (
    SELECT 1 FROM usuarios_roles ur 
    WHERE ur.usuario_id = u.id AND ur.rol_id = r.id
  );

-- Paso 4: Crear registro en la tabla clientes con valores por defecto
INSERT INTO clientes (usuario_id, codigo_cliente, tipo_cliente, limite_credito, score_crediticio, ingresos_declarados, activo)
SELECT 
    u.id,
    CONCAT('CLI', LPAD(u.id, 3, '0')) as codigo_cliente,
    'NUEVO' as tipo_cliente,
    15000.00 as limite_credito,     -- Límite de crédito por defecto
    650 as score_crediticio,        -- Score crediticio por defecto
    0.00 as ingresos_declarados,    -- Ingresos iniciales en 0
    TRUE as activo
FROM usuarios u
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%')
  AND NOT EXISTS (
    SELECT 1 FROM clientes c WHERE c.usuario_id = u.id
  );

-- Paso 5: Verificar que todo se creó correctamente
SELECT 
    u.id as usuario_id,
    u.username,
    u.nombres,
    u.apellidos,
    r.nombre as rol,
    c.codigo_cliente,
    c.tipo_cliente,
    c.limite_credito,
    c.score_crediticio,
    c.ingresos_declarados,
    ur.activo as rol_activo,
    c.activo as cliente_activo
FROM usuarios u
INNER JOIN usuarios_roles ur ON u.id = ur.usuario_id
INNER JOIN roles r ON ur.rol_id = r.id
LEFT JOIN clientes c ON u.id = c.usuario_id
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%')
  AND r.nombre = 'CLIENTE';

-- Paso 6: Mostrar el nuevo código de cliente generado
SELECT 
    CONCAT('CLI', LPAD(u.id, 3, '0')) as codigo_cliente_generado,
    u.nombres,
    u.apellidos
FROM usuarios u
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%');

COMMIT;
