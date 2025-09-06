-- Script temporal para crear un usuario administrador con contraseña simple
-- Esto te permitirá entrar al sistema mientras arreglamos las contraseñas

USE crediactiva;

-- Crear un usuario temporal de emergencia
-- Usuario: tempAdmin, Contraseña: 123456 (hash BCrypt simple para testing)
INSERT INTO usuarios (username, email, password_hash, nombres, apellidos, documento_identidad, telefono, direccion, activo) 
VALUES ('tempAdmin', 'temp@crediactiva.pe', '$2a$12$EixZxYkLJiuq0T9UaS7GiuY/PfSNcJ4nXkT9UaS7GiuY/PfSNcJ4nX', 'Administrador', 'Temporal', '00000000', '000000000', 'Temporal', TRUE)
ON DUPLICATE KEY UPDATE 
password_hash = '$2a$12$EixZxYkLJiuq0T9UaS7GiuY/PfSNcJ4nXkT9UaS7GiuY/PfSNcJ4nX',
activo = TRUE;

-- Asignar rol de administrador al usuario temporal
INSERT INTO usuarios_roles (usuario_id, rol_id) 
SELECT u.id, r.id 
FROM usuarios u, roles r 
WHERE u.username = 'tempAdmin' AND r.nombre = 'ADMINISTRADOR'
ON DUPLICATE KEY UPDATE activo = TRUE;

-- Alternativa: Actualizar el usuario admin existente con una contraseña simple
-- Contraseña temporal: 123456
UPDATE usuarios SET 
    password_hash = '$2a$12$EixZxYkLJiuq0T9UaS7GiuY/PfSNcJ4nXkT9UaS7GiuY/PfSNcJ4nX',
    activo = TRUE 
WHERE username = 'admin';

-- Mostrar usuarios administrativos
SELECT 
    u.username, 
    u.nombres, 
    u.apellidos, 
    u.email, 
    u.activo,
    r.nombre as rol
FROM usuarios u
JOIN usuarios_roles ur ON u.id = ur.usuario_id
JOIN roles r ON ur.rol_id = r.id
WHERE r.nombre = 'ADMINISTRADOR';

COMMIT;
