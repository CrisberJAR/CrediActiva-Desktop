-- Script para asignar rol ASESOR a Sara
-- Ejecutar estos comandos en tu base de datos MySQL

USE crediactiva;

-- Paso 1: Verificar que Sara existe en la tabla usuarios
SELECT id, username, nombres, apellidos, email, activo 
FROM usuarios 
WHERE nombres LIKE '%Sara%' OR username LIKE '%sara%';

-- Paso 2: Obtener el ID del rol ASESOR
SELECT id, nombre, descripcion 
FROM roles 
WHERE nombre = 'ASESOR';

-- Paso 3: Asignar el rol ASESOR a Sara (ajusta el usuario_id según el resultado del Paso 1)
-- REEMPLAZA [ID_DE_SARA] con el ID real de Sara obtenido en el Paso 1
INSERT INTO usuarios_roles (usuario_id, rol_id, activo) 
SELECT u.id, r.id, TRUE
FROM usuarios u, roles r 
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%')
  AND r.nombre = 'ASESOR'
  AND NOT EXISTS (
    SELECT 1 FROM usuarios_roles ur 
    WHERE ur.usuario_id = u.id AND ur.rol_id = r.id
  );

-- Paso 4: Crear registro en la tabla asesores con valores por defecto
INSERT INTO asesores (usuario_id, codigo_asesor, comision_porcentaje, meta_mensual, activo)
SELECT 
    u.id,
    CONCAT('ASE', LPAD(u.id, 3, '0')) as codigo_asesor,
    0.02 as comision_porcentaje,  -- 2% de comisión
    0.00 as meta_mensual,         -- Meta inicial en 0
    TRUE as activo
FROM usuarios u
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%')
  AND NOT EXISTS (
    SELECT 1 FROM asesores a WHERE a.usuario_id = u.id
  );

-- Paso 5: Verificar que todo se creó correctamente
SELECT 
    u.id as usuario_id,
    u.username,
    u.nombres,
    u.apellidos,
    r.nombre as rol,
    a.codigo_asesor,
    a.comision_porcentaje,
    a.meta_mensual,
    ur.activo as rol_activo,
    a.activo as asesor_activo
FROM usuarios u
INNER JOIN usuarios_roles ur ON u.id = ur.usuario_id
INNER JOIN roles r ON ur.rol_id = r.id
LEFT JOIN asesores a ON u.id = a.usuario_id
WHERE (u.nombres LIKE '%Sara%' OR u.username LIKE '%sara%')
  AND r.nombre = 'ASESOR';

COMMIT;
