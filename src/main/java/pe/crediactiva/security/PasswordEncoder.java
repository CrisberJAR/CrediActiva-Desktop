package pe.crediactiva.security;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoder de contraseñas usando BCrypt para CrediActiva.
 * Proporciona funcionalidades para encriptar y verificar contraseñas de forma segura.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class PasswordEncoder {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoder.class);
    
    // Configuración de BCrypt
    private static final int DEFAULT_ROUNDS = 12; // Número de rondas para el hashing
    
    /**
     * Encripta una contraseña usando BCrypt.
     * 
     * @param plainPassword contraseña en texto plano
     * @return hash de la contraseña
     * @throws IllegalArgumentException si la contraseña es null o vacía
     */
    public String encode(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser null o vacía");
        }
        
        try {
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(DEFAULT_ROUNDS));
            logger.debug("Contraseña encriptada exitosamente");
            return hashedPassword;
            
        } catch (Exception e) {
            logger.error("Error al encriptar contraseña", e);
            throw new RuntimeException("Error al encriptar contraseña", e);
        }
    }
    
    /**
     * Verifica si una contraseña en texto plano coincide con un hash.
     * 
     * @param plainPassword contraseña en texto plano
     * @param hashedPassword hash de la contraseña almacenada
     * @return true si la contraseña coincide
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            logger.warn("Intento de verificación con contraseña o hash null");
            return false;
        }
        
        if (plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            logger.warn("Intento de verificación con contraseña o hash vacío");
            return false;
        }
        
        try {
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
            logger.debug("Verificación de contraseña: {}", matches ? "exitosa" : "fallida");
            return matches;
            
        } catch (Exception e) {
            logger.error("Error al verificar contraseña", e);
            return false;
        }
    }
    
    /**
     * Verifica si un hash es válido según el formato de BCrypt.
     * 
     * @param hash hash a verificar
     * @return true si el hash es válido
     */
    public boolean isValidHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        
        try {
            // Un hash de BCrypt válido debe comenzar con $2a$, $2b$, $2x$ o $2y$
            // y tener una longitud específica
            return hash.matches("^\\$2[abyxy]\\$\\d{2}\\$.{53}$");
            
        } catch (Exception e) {
            logger.error("Error al validar hash", e);
            return false;
        }
    }
    
    /**
     * Genera una contraseña temporal aleatoria.
     * Útil para reseteo de contraseñas.
     * 
     * @param length longitud de la contraseña (mínimo 8)
     * @return contraseña temporal
     */
    public String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La longitud mínima de contraseña temporal es 8 caracteres");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        String temporaryPassword = password.toString();
        logger.debug("Contraseña temporal generada con longitud: {}", length);
        
        return temporaryPassword;
    }
    
    /**
     * Verifica la fortaleza de una contraseña.
     * 
     * @param password contraseña a verificar
     * @return resultado de la verificación
     */
    public PasswordStrengthResult checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrengthResult(false, "La contraseña no puede estar vacía");
        }
        
        StringBuilder issues = new StringBuilder();
        boolean isStrong = true;
        
        // Longitud mínima
        if (password.length() < 8) {
            issues.append("Debe tener al menos 8 caracteres. ");
            isStrong = false;
        }
        
        // Al menos una letra minúscula
        if (!password.matches(".*[a-z].*")) {
            issues.append("Debe contener al menos una letra minúscula. ");
            isStrong = false;
        }
        
        // Al menos una letra mayúscula
        if (!password.matches(".*[A-Z].*")) {
            issues.append("Debe contener al menos una letra mayúscula. ");
            isStrong = false;
        }
        
        // Al menos un número
        if (!password.matches(".*[0-9].*")) {
            issues.append("Debe contener al menos un número. ");
            isStrong = false;
        }
        
        // Al menos un carácter especial
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            issues.append("Debe contener al menos un carácter especial. ");
            isStrong = false;
        }
        
        // Verificar patrones comunes débiles
        if (isCommonWeakPassword(password)) {
            issues.append("La contraseña es demasiado común o predecible. ");
            isStrong = false;
        }
        
        String message = isStrong ? "Contraseña segura" : issues.toString().trim();
        return new PasswordStrengthResult(isStrong, message);
    }
    
    /**
     * Verifica si una contraseña es común o débil.
     */
    private boolean isCommonWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Lista de contraseñas comunes
        String[] commonPasswords = {
            "password", "123456", "123456789", "12345678", "12345",
            "qwerty", "abc123", "password123", "admin", "letmein",
            "welcome", "monkey", "dragon", "master", "hello",
            "login", "admin123", "root", "user", "test"
        };
        
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        
        // Verificar secuencias numéricas
        if (lowerPassword.matches(".*123456.*") || 
            lowerPassword.matches(".*654321.*") ||
            lowerPassword.matches(".*111111.*") ||
            lowerPassword.matches(".*000000.*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Clase para el resultado de verificación de fortaleza de contraseña.
     */
    public static class PasswordStrengthResult {
        private final boolean isStrong;
        private final String message;
        
        public PasswordStrengthResult(boolean isStrong, String message) {
            this.isStrong = isStrong;
            this.message = message;
        }
        
        public boolean isStrong() {
            return isStrong;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "PasswordStrengthResult{" +
                    "isStrong=" + isStrong +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}


