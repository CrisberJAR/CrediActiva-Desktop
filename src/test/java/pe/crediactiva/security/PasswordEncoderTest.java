package pe.crediactiva.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para PasswordEncoder.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
class PasswordEncoderTest {
    
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
    }
    
    @Test
    @DisplayName("Encode debe generar hash válido para contraseña válida")
    void testEncode_ValidPassword() {
        String plainPassword = "miPasswordSegura123";
        
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertTrue(passwordEncoder.isValidHash(hashedPassword));
    }
    
    @Test
    @DisplayName("Encode debe lanzar excepción para contraseña null")
    void testEncode_NullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(null);
        });
    }
    
    @Test
    @DisplayName("Encode debe lanzar excepción para contraseña vacía")
    void testEncode_EmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode("");
        });
    }
    
    @Test
    @DisplayName("Matches debe retornar true para contraseña correcta")
    void testMatches_CorrectPassword() {
        String plainPassword = "miPasswordSegura123";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        assertTrue(passwordEncoder.matches(plainPassword, hashedPassword));
    }
    
    @Test
    @DisplayName("Matches debe retornar false para contraseña incorrecta")
    void testMatches_IncorrectPassword() {
        String plainPassword = "miPasswordSegura123";
        String wrongPassword = "passwordIncorrecta";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        assertFalse(passwordEncoder.matches(wrongPassword, hashedPassword));
    }
    
    @Test
    @DisplayName("Matches debe retornar false para parámetros null")
    void testMatches_NullParameters() {
        String hashedPassword = passwordEncoder.encode("test123");
        
        assertFalse(passwordEncoder.matches(null, hashedPassword));
        assertFalse(passwordEncoder.matches("test123", null));
        assertFalse(passwordEncoder.matches(null, null));
    }
    
    @Test
    @DisplayName("Matches debe retornar false para parámetros vacíos")
    void testMatches_EmptyParameters() {
        String hashedPassword = passwordEncoder.encode("test123");
        
        assertFalse(passwordEncoder.matches("", hashedPassword));
        assertFalse(passwordEncoder.matches("test123", ""));
    }
    
    @Test
    @DisplayName("IsValidHash debe validar correctamente hashes de BCrypt")
    void testIsValidHash() {
        String validHash = passwordEncoder.encode("test123");
        String invalidHash = "hash_invalido";
        
        assertTrue(passwordEncoder.isValidHash(validHash));
        assertFalse(passwordEncoder.isValidHash(invalidHash));
        assertFalse(passwordEncoder.isValidHash(null));
        assertFalse(passwordEncoder.isValidHash(""));
    }
    
    @Test
    @DisplayName("GenerateTemporaryPassword debe generar contraseña con longitud correcta")
    void testGenerateTemporaryPassword() {
        int length = 12;
        
        String temporaryPassword = passwordEncoder.generateTemporaryPassword(length);
        
        assertNotNull(temporaryPassword);
        assertEquals(length, temporaryPassword.length());
        
        // Verificar que contiene caracteres válidos
        assertTrue(temporaryPassword.matches("[A-Za-z0-9!@#$%^&*]+"));
    }
    
    @Test
    @DisplayName("GenerateTemporaryPassword debe lanzar excepción para longitud menor a 8")
    void testGenerateTemporaryPassword_InvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.generateTemporaryPassword(7);
        });
    }
    
    @Test
    @DisplayName("CheckPasswordStrength debe identificar contraseña fuerte")
    void testCheckPasswordStrength_StrongPassword() {
        String strongPassword = "MiPassword123!";
        
        PasswordEncoder.PasswordStrengthResult result = passwordEncoder.checkPasswordStrength(strongPassword);
        
        assertTrue(result.isStrong());
        assertEquals("Contraseña segura", result.getMessage());
    }
    
    @Test
    @DisplayName("CheckPasswordStrength debe identificar contraseña débil")
    void testCheckPasswordStrength_WeakPassword() {
        String weakPassword = "123456";
        
        PasswordEncoder.PasswordStrengthResult result = passwordEncoder.checkPasswordStrength(weakPassword);
        
        assertFalse(result.isStrong());
        assertTrue(result.getMessage().contains("al menos"));
    }
    
    @Test
    @DisplayName("CheckPasswordStrength debe rechazar contraseñas comunes")
    void testCheckPasswordStrength_CommonPassword() {
        String commonPassword = "password123";
        
        PasswordEncoder.PasswordStrengthResult result = passwordEncoder.checkPasswordStrength(commonPassword);
        
        assertFalse(result.isStrong());
        assertTrue(result.getMessage().contains("común") || result.getMessage().contains("predecible"));
    }
    
    @Test
    @DisplayName("CheckPasswordStrength debe manejar contraseña null o vacía")
    void testCheckPasswordStrength_NullOrEmpty() {
        PasswordEncoder.PasswordStrengthResult resultNull = passwordEncoder.checkPasswordStrength(null);
        PasswordEncoder.PasswordStrengthResult resultEmpty = passwordEncoder.checkPasswordStrength("");
        
        assertFalse(resultNull.isStrong());
        assertFalse(resultEmpty.isStrong());
        assertTrue(resultNull.getMessage().contains("vacía"));
        assertTrue(resultEmpty.getMessage().contains("vacía"));
    }
    
    @Test
    @DisplayName("Encode debe generar hashes diferentes para la misma contraseña")
    void testEncode_DifferentHashesForSamePassword() {
        String password = "miPassword123";
        
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);
        
        assertNotEquals(hash1, hash2);
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }
}


