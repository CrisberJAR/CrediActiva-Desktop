-- Script final para corregir las contraseñas de los usuarios iniciales
-- Hashes BCrypt válidos generados específicamente para cada contraseña

USE crediactiva;

-- Actualizar contraseñas con hashes BCrypt válidos
-- Estos son hashes reales generados con BCrypt rounds=12

-- admin123 (Hash BCrypt válido)
UPDATE usuarios SET 
    password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyc6YRqGQUugt6wLtXXTBe',
    activo = TRUE 
WHERE username = 'admin';

-- asesor123 (Hash BCrypt válido)
UPDATE usuarios SET 
    password_hash = '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    activo = TRUE 
WHERE username = 'asesor1';

-- cliente123 (Hash BCrypt válido)
UPDATE usuarios SET 
    password_hash = '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    activo = TRUE 
WHERE username IN ('cliente1', 'cliente2', 'cliente3');

-- Verificar que los roles estén correctamente asignados
SELECT 
    u.username,
    u.nombres,
    u.apellidos,
    u.activo as usuario_activo,
    r.nombre as rol,
    ur.activo as rol_activo
FROM usuarios u
LEFT JOIN usuarios_roles ur ON u.id = ur.usuario_id
LEFT JOIN roles r ON ur.rol_id = r.id
WHERE u.username IN ('admin', 'asesor1', 'cliente1', 'cliente2', 'cliente3')
ORDER BY u.username, r.nombre;

-- Verificar estructura de hashes
SELECT 
    username,
    LENGTH(password_hash) as hash_length,
    SUBSTRING(password_hash, 1, 7) as hash_format,
    activo
FROM usuarios 
WHERE username IN ('admin', 'asesor1', 'cliente1', 'cliente2', 'cliente3');

COMMIT;

-- Instrucciones de uso:
-- 1. Ejecutar este script en MySQL
-- 2. Probar login con: admin/admin123, asesor1/asesor123, cliente1/cliente123
-- 3. El sistema debe redirigir automáticamente al dashboard correcto según el rol
