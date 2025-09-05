package pe.crediactiva.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.DayOfWeek;

/**
 * Pruebas unitarias para DateUtils.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
class DateUtilsTest {
    
    @Test
    @DisplayName("Ajustar fecha que cae en domingo debe moverla al lunes")
    void testAjustarSinDomingo_Domingo() {
        // Crear una fecha que sea domingo
        LocalDate domingo = LocalDate.of(2024, 1, 7); // 7 de enero de 2024 es domingo
        assertEquals(DayOfWeek.SUNDAY, domingo.getDayOfWeek());
        
        // Ajustar la fecha
        LocalDate fechaAjustada = DateUtils.ajustarSinDomingo(domingo);
        
        // Verificar que se movió al lunes
        assertEquals(DayOfWeek.MONDAY, fechaAjustada.getDayOfWeek());
        assertEquals(LocalDate.of(2024, 1, 8), fechaAjustada);
    }
    
    @Test
    @DisplayName("Ajustar fecha que no es domingo debe mantenerla igual")
    void testAjustarSinDomingo_NoEsDomingo() {
        LocalDate lunes = LocalDate.of(2024, 1, 8); // Lunes
        LocalDate martes = LocalDate.of(2024, 1, 9); // Martes
        LocalDate sabado = LocalDate.of(2024, 1, 6); // Sábado
        
        assertEquals(lunes, DateUtils.ajustarSinDomingo(lunes));
        assertEquals(martes, DateUtils.ajustarSinDomingo(martes));
        assertEquals(sabado, DateUtils.ajustarSinDomingo(sabado));
    }
    
    @Test
    @DisplayName("Agregar meses sin domingo debe ajustar correctamente")
    void testAgregarMesesSinDomingo() {
        LocalDate fechaBase = LocalDate.of(2024, 1, 7); // Domingo
        
        // Agregar 1 mes (debería ser 7 de febrero, si es domingo se ajusta)
        LocalDate resultado = DateUtils.agregarMesesSinDomingo(fechaBase, 1);
        
        // Verificar que no sea domingo
        assertNotEquals(DayOfWeek.SUNDAY, resultado.getDayOfWeek());
    }
    
    @Test
    @DisplayName("Calcular días de atraso correctamente")
    void testCalcularDiasAtraso() {
        LocalDate hoy = DateUtils.hoy();
        
        // Fecha vencida (5 días atrás)
        LocalDate fechaVencida = hoy.minusDays(5);
        assertEquals(5, DateUtils.calcularDiasAtraso(fechaVencida));
        
        // Fecha futura (no vencida)
        LocalDate fechaFutura = hoy.plusDays(3);
        assertEquals(0, DateUtils.calcularDiasAtraso(fechaFutura));
        
        // Fecha de hoy (no vencida)
        assertEquals(0, DateUtils.calcularDiasAtraso(hoy));
    }
    
    @Test
    @DisplayName("Verificar si fecha está vencida")
    void testEstaVencida() {
        LocalDate hoy = DateUtils.hoy();
        
        assertTrue(DateUtils.estaVencida(hoy.minusDays(1)));
        assertFalse(DateUtils.estaVencida(hoy));
        assertFalse(DateUtils.estaVencida(hoy.plusDays(1)));
    }
    
    @Test
    @DisplayName("Formatear fecha correctamente")
    void testFormatearFecha() {
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        assertEquals("15/01/2024", DateUtils.formatearFecha(fecha));
        assertEquals("", DateUtils.formatearFecha(null));
    }
    
    @Test
    @DisplayName("Parsear fecha correctamente")
    void testParsearFecha() {
        LocalDate fechaEsperada = LocalDate.of(2024, 1, 15);
        assertEquals(fechaEsperada, DateUtils.parsearFecha("15/01/2024"));
        assertNull(DateUtils.parsearFecha("fecha-inválida"));
        assertNull(DateUtils.parsearFecha(null));
        assertNull(DateUtils.parsearFecha(""));
    }
    
    @Test
    @DisplayName("Verificar rango de fechas")
    void testEstaEnRango() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2024, 1, 31);
        LocalDate fechaEnRango = LocalDate.of(2024, 1, 15);
        LocalDate fechaFueraRango = LocalDate.of(2024, 2, 1);
        
        assertTrue(DateUtils.estaEnRango(fechaEnRango, inicio, fin));
        assertTrue(DateUtils.estaEnRango(inicio, inicio, fin)); // Límite inferior
        assertTrue(DateUtils.estaEnRango(fin, inicio, fin)); // Límite superior
        assertFalse(DateUtils.estaEnRango(fechaFueraRango, inicio, fin));
    }
    
    @Test
    @DisplayName("Obtener nombres de días y meses en español")
    void testNombresEspanol() {
        LocalDate fecha = LocalDate.of(2024, 1, 15); // Lunes, Enero
        
        assertEquals("Lunes", DateUtils.getNombreDia(fecha));
        assertEquals("Enero", DateUtils.getNombreMes(fecha));
        assertEquals("", DateUtils.getNombreDia(null));
        assertEquals("", DateUtils.getNombreMes(null));
    }
}


