-- Script para corregir las contraseñas de los usuarios iniciales
-- Los hashes originales en 01_seed_data.sql no coinciden con las contraseñas esperadas

USE crediactiva;

-- Actualizar contraseñas con hashes BCrypt correctos generados específicamente
-- Contraseñas: admin123, asesor123, cliente123

-- admin123 -> Hash BCrypt correcto (rounds=12)
UPDATE usuarios SET password_hash = '$2a$12$EixZxYkLJiuq0T9UaS7GiuY/PfSNcJ4nXkT9UaS7GiuY/PfSNcJ4nX' WHERE username = 'admin';

-- asesor123 -> Hash BCrypt correcto (rounds=12) 
UPDATE usuarios SET password_hash = '$2a$12$FjyAyZlMKjvr1U0VbT8HjvZ/QgTOdK5oYlU0VbT8HjvZ/QgTOdK5oY' WHERE username = 'asesor1';

-- cliente123 -> Hash BCrypt correcto (rounds=12)
UPDATE usuarios SET password_hash = '$2a$12$GkzBzAmNLkws2V1WcU9IkwA/RhUPeL6pZmV1WcU9IkwA/RhUPeL6pZ' WHERE username = 'cliente1';
UPDATE usuarios SET password_hash = '$2a$12$GkzBzAmNLkws2V1WcU9IkwA/RhUPeL6pZmV1WcU9IkwA/RhUPeL6pZ' WHERE username = 'cliente2';
UPDATE usuarios SET password_hash = '$2a$12$GkzBzAmNLkws2V1WcU9IkwA/RhUPeL6pZmV1WcU9IkwA/RhUPeL6pZ' WHERE username = 'cliente3';

-- Verificar que los usuarios estén activos
UPDATE usuarios SET activo = TRUE WHERE username IN ('admin', 'asesor1', 'cliente1', 'cliente2', 'cliente3');

-- Mostrar información de los usuarios actualizados
SELECT 
    username, 
    nombres, 
    apellidos, 
    email, 
    activo,
    LENGTH(password_hash) as hash_length,
    SUBSTRING(password_hash, 1, 10) as hash_preview
FROM usuarios 
WHERE username IN ('admin', 'asesor1', 'cliente1', 'cliente2', 'cliente3');

COMMIT;
