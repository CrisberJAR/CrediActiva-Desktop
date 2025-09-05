package pe.crediactiva.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pe.crediactiva.model.enums.EstadoPrestamo;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Pruebas unitarias para la entidad Prestamo.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
class PrestamoTest {
    
    private Prestamo prestamo;
    
    @BeforeEach
    void setUp() {
        prestamo = new Prestamo();
        prestamo.setId(1);
        prestamo.setNumeroPrestamo("PRES-2024-001");
        prestamo.setMontoPrestamo(new BigDecimal("10000.00"));
        prestamo.setTasaInteresMensual(new BigDecimal("0.025")); // 2.5%
        prestamo.setPlazoMeses(12);
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo.setCronogramaPagos(new ArrayList<>());
        prestamo.setPagos(new ArrayList<>());
    }
    
    @Test
    @DisplayName("Constructor por defecto debe inicializar estado ACTIVO")
    void testConstructorDefecto() {
        Prestamo nuevoPrestamo = new Prestamo();
        
        assertEquals(EstadoPrestamo.ACTIVO, nuevoPrestamo.getEstado());
        assertNotNull(nuevoPrestamo.getCronogramaPagos());
        assertNotNull(nuevoPrestamo.getPagos());
        assertNotNull(nuevoPrestamo.getFechaCreacion());
        assertNotNull(nuevoPrestamo.getFechaActualizacion());
    }
    
    @Test
    @DisplayName("Calcular deuda actual sin pagos debe retornar monto total")
    void testCalcularDeudaActual_SinPagos() {
        prestamo.setMontoTotal(new BigDecimal("13000.00"));
        
        BigDecimal deudaActual = prestamo.calcularDeudaActual();
        
        assertEquals(new BigDecimal("13000.00"), deudaActual);
    }
    
    @Test
    @DisplayName("Calcular deuda actual con pagos debe restar pagos del monto total")
    void testCalcularDeudaActual_ConPagos() {
        prestamo.setMontoTotal(new BigDecimal("13000.00"));
        
        // Agregar algunos pagos
        Pago pago1 = new Pago();
        pago1.setMontoPago(new BigDecimal("1000.00"));
        
        Pago pago2 = new Pago();
        pago2.setMontoPago(new BigDecimal("1000.00"));
        
        prestamo.getPagos().add(pago1);
        prestamo.getPagos().add(pago2);
        
        BigDecimal deudaActual = prestamo.calcularDeudaActual();
        
        assertEquals(new BigDecimal("11000.00"), deudaActual);
    }
    
    @Test
    @DisplayName("Calcular porcentaje de avance debe funcionar correctamente")
    void testCalcularPorcentajeAvance() {
        prestamo.setMontoTotal(new BigDecimal("10000.00"));
        
        // Agregar pago del 30%
        Pago pago = new Pago();
        pago.setMontoPago(new BigDecimal("3000.00"));
        prestamo.getPagos().add(pago);
        
        BigDecimal porcentaje = prestamo.calcularPorcentajeAvance();
        
        assertEquals(new BigDecimal("0.3000"), porcentaje);
    }
    
    @Test
    @DisplayName("Contar cuotas pendientes debe funcionar correctamente")
    void testContarCuotasPendientes() {
        // Agregar cuotas al cronograma
        CronogramaPago cuota1 = new CronogramaPago();
        cuota1.setPagado(false);
        
        CronogramaPago cuota2 = new CronogramaPago();
        cuota2.setPagado(true);
        
        CronogramaPago cuota3 = new CronogramaPago();
        cuota3.setPagado(false);
        
        prestamo.getCronogramaPagos().add(cuota1);
        prestamo.getCronogramaPagos().add(cuota2);
        prestamo.getCronogramaPagos().add(cuota3);
        
        int cuotasPendientes = prestamo.contarCuotasPendientes();
        
        assertEquals(2, cuotasPendientes);
    }
    
    @Test
    @DisplayName("Contar cuotas vencidas debe funcionar correctamente")
    void testContarCuotasVencidas() {
        LocalDate hoy = LocalDate.now();
        
        // Cuota vencida no pagada
        CronogramaPago cuotaVencida1 = new CronogramaPago();
        cuotaVencida1.setPagado(false);
        cuotaVencida1.setFechaVencimiento(hoy.minusDays(5));
        
        // Cuota vencida pero pagada
        CronogramaPago cuotaVencida2 = new CronogramaPago();
        cuotaVencida2.setPagado(true);
        cuotaVencida2.setFechaVencimiento(hoy.minusDays(3));
        
        // Cuota no vencida
        CronogramaPago cuotaNoVencida = new CronogramaPago();
        cuotaNoVencida.setPagado(false);
        cuotaNoVencida.setFechaVencimiento(hoy.plusDays(5));
        
        prestamo.getCronogramaPagos().add(cuotaVencida1);
        prestamo.getCronogramaPagos().add(cuotaVencida2);
        prestamo.getCronogramaPagos().add(cuotaNoVencida);
        
        int cuotasVencidas = prestamo.contarCuotasVencidas();
        
        assertEquals(1, cuotasVencidas); // Solo la vencida no pagada
    }
    
    @Test
    @DisplayName("Esta al día debe retornar true si no hay cuotas vencidas")
    void testEstaAlDia() {
        LocalDate hoy = LocalDate.now();
        
        // Todas las cuotas al día
        CronogramaPago cuota1 = new CronogramaPago();
        cuota1.setPagado(true);
        cuota1.setFechaVencimiento(hoy.minusDays(5));
        
        CronogramaPago cuota2 = new CronogramaPago();
        cuota2.setPagado(false);
        cuota2.setFechaVencimiento(hoy.plusDays(5));
        
        prestamo.getCronogramaPagos().add(cuota1);
        prestamo.getCronogramaPagos().add(cuota2);
        
        assertTrue(prestamo.estaAlDia());
    }
    
    @Test
    @DisplayName("Esta completamente pagado debe retornar true si todas las cuotas están pagadas")
    void testEstaCompletamentePagado() {
        // Todas las cuotas pagadas
        CronogramaPago cuota1 = new CronogramaPago();
        cuota1.setPagado(true);
        
        CronogramaPago cuota2 = new CronogramaPago();
        cuota2.setPagado(true);
        
        prestamo.getCronogramaPagos().add(cuota1);
        prestamo.getCronogramaPagos().add(cuota2);
        
        assertTrue(prestamo.estaCompletamentePagado());
    }
    
    @Test
    @DisplayName("Obtener próxima cuota debe retornar la cuota no pagada más próxima")
    void testGetProximaCuota() {
        LocalDate hoy = LocalDate.now();
        
        CronogramaPago cuota1 = new CronogramaPago();
        cuota1.setPagado(false);
        cuota1.setFechaVencimiento(hoy.plusDays(10));
        cuota1.setNumeroCuota(2);
        
        CronogramaPago cuota2 = new CronogramaPago();
        cuota2.setPagado(false);
        cuota2.setFechaVencimiento(hoy.plusDays(5));
        cuota2.setNumeroCuota(1);
        
        CronogramaPago cuota3 = new CronogramaPago();
        cuota3.setPagado(true);
        cuota3.setFechaVencimiento(hoy.minusDays(5));
        cuota3.setNumeroCuota(0);
        
        prestamo.getCronogramaPagos().add(cuota1);
        prestamo.getCronogramaPagos().add(cuota2);
        prestamo.getCronogramaPagos().add(cuota3);
        
        CronogramaPago proximaCuota = prestamo.getProximaCuota();
        
        assertNotNull(proximaCuota);
        assertEquals(Integer.valueOf(1), proximaCuota.getNumeroCuota());
    }
    
    @Test
    @DisplayName("Actualizar estado debe cambiar a PAGADO si está completamente pagado")
    void testActualizarEstado_CompletamentePagado() {
        // Todas las cuotas pagadas
        CronogramaPago cuota1 = new CronogramaPago();
        cuota1.setPagado(true);
        
        prestamo.getCronogramaPagos().add(cuota1);
        
        prestamo.actualizarEstado();
        
        assertEquals(EstadoPrestamo.PAGADO, prestamo.getEstado());
    }
    
    @Test
    @DisplayName("Actualizar estado debe cambiar a VENCIDO si hay cuotas vencidas")
    void testActualizarEstado_ConCuotasVencidas() {
        LocalDate hoy = LocalDate.now();
        
        // Cuota vencida no pagada
        CronogramaPago cuotaVencida = new CronogramaPago();
        cuotaVencida.setPagado(false);
        cuotaVencida.setFechaVencimiento(hoy.minusDays(5));
        
        prestamo.getCronogramaPagos().add(cuotaVencida);
        
        prestamo.actualizarEstado();
        
        assertEquals(EstadoPrestamo.VENCIDO, prestamo.getEstado());
    }
    
    @Test
    @DisplayName("Actualizar estado debe mantener ACTIVO si está al día")
    void testActualizarEstado_AlDia() {
        LocalDate hoy = LocalDate.now();
        
        // Cuota futura no pagada
        CronogramaPago cuotaFutura = new CronogramaPago();
        cuotaFutura.setPagado(false);
        cuotaFutura.setFechaVencimiento(hoy.plusDays(5));
        
        prestamo.getCronogramaPagos().add(cuotaFutura);
        
        prestamo.actualizarEstado();
        
        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstado());
    }
    
    @Test
    @DisplayName("Equals debe funcionar correctamente con ID y número de préstamo")
    void testEquals() {
        Prestamo prestamo1 = new Prestamo();
        prestamo1.setId(1);
        prestamo1.setNumeroPrestamo("PRES-001");
        
        Prestamo prestamo2 = new Prestamo();
        prestamo2.setId(1);
        prestamo2.setNumeroPrestamo("PRES-002");
        
        Prestamo prestamo3 = new Prestamo();
        prestamo3.setId(2);
        prestamo3.setNumeroPrestamo("PRES-001");
        
        // Mismo ID
        assertEquals(prestamo1, prestamo2);
        
        // Mismo número de préstamo
        assertEquals(prestamo1, prestamo3);
        
        // Diferente objeto
        assertNotEquals(prestamo1, "string");
        assertNotEquals(prestamo1, null);
    }
    
    @Test
    @DisplayName("ToString debe incluir información relevante")
    void testToString() {
        Usuario cliente = new Usuario();
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        prestamo.setCliente(cliente);
        
        String toString = prestamo.toString();
        
        assertTrue(toString.contains("PRES-2024-001"));
        assertTrue(toString.contains("Juan Pérez"));
        assertTrue(toString.contains("10000"));
        assertTrue(toString.contains("ACTIVO"));
    }
}


