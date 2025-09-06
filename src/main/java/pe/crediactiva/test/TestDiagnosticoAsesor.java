package pe.crediactiva.test;

import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.util.DiagnosticoAsesorUtil;

/**
 * Programa de prueba para diagnosticar problemas con asesores.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class TestDiagnosticoAsesor {
    
    public static void main(String[] args) {
        System.out.println("🔍 INICIANDO DIAGNÓSTICO DE ASESORES");
        System.out.println("=" .repeat(50));
        
        try {
            // Inicializar base de datos
            DatabaseConfig.initialize();
            System.out.println("✅ Base de datos inicializada");
            
            // Ejecutar diagnóstico completo
            DiagnosticoAsesorUtil.ejecutarDiagnostico();
            
            System.out.println("✅ Diagnóstico completado");
            
        } catch (Exception e) {
            System.err.println("❌ Error en diagnóstico: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar conexiones
            try {
                DatabaseConfig.shutdown();
                System.out.println("✅ Conexiones cerradas");
            } catch (Exception e) {
                System.err.println("⚠️ Error al cerrar conexiones: " + e.getMessage());
            }
        }
    }
}
