package pe.crediactiva.model;

import pe.crediactiva.model.enums.EstadoPrestamo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un préstamo en el sistema CrediActiva.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Prestamo {
    
    private Integer id;
    private String numeroPrestamo;
    private Integer solicitudId;
    private Integer clienteId;
    private Integer asesorId;
    private BigDecimal montoPrestamo;
    private BigDecimal montoTotal;
    private Integer plazoMeses;
    private BigDecimal tasaInteresMensual;
    private BigDecimal cuotaMensual;
    private EstadoPrestamo estado;
    private LocalDate fechaDesembolso;
    private LocalDate fechaPrimerVencimiento;
    private LocalDate fechaUltimoVencimiento;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    private Solicitud solicitud;
    private Usuario cliente;
    private Asesor asesor;
    private List<CronogramaPago> cronogramaPagos;
    private List<Pago> pagos;
    
    // Constructores
    public Prestamo() {
        this.estado = EstadoPrestamo.ACTIVO;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.cronogramaPagos = new ArrayList<>();
        this.pagos = new ArrayList<>();
    }
    
    public Prestamo(String numeroPrestamo, Solicitud solicitud, BigDecimal montoPrestamo, 
                   Integer plazoMeses, BigDecimal tasaInteresMensual) {
        this();
        this.numeroPrestamo = numeroPrestamo;
        this.solicitud = solicitud;
        this.solicitudId = solicitud != null ? solicitud.getId() : null;
        this.cliente = solicitud != null ? solicitud.getCliente() : null;
        this.clienteId = this.cliente != null ? this.cliente.getId() : null;
        this.asesor = solicitud != null ? solicitud.getAsesor() : null;
        this.asesorId = this.asesor != null ? this.asesor.getId() : null;
        this.montoPrestamo = montoPrestamo;
        this.plazoMeses = plazoMeses;
        this.tasaInteresMensual = tasaInteresMensual;
        
        // Calcular cuota mensual y monto total
        calcularCuotaYMontoTotal();
    }
    
    // Métodos de utilidad
    
    /**
     * Calcula la cuota mensual y el monto total usando el sistema francés.
     */
    private void calcularCuotaYMontoTotal() {
        if (montoPrestamo == null || tasaInteresMensual == null || plazoMeses == null || plazoMeses <= 0) {
            this.cuotaMensual = BigDecimal.ZERO;
            this.montoTotal = montoPrestamo != null ? montoPrestamo : BigDecimal.ZERO;
            return;
        }
        
        if (tasaInteresMensual.compareTo(BigDecimal.ZERO) == 0) {
            // Sin interés
            this.cuotaMensual = montoPrestamo.divide(new BigDecimal(plazoMeses), 2, java.math.RoundingMode.HALF_UP);
            this.montoTotal = montoPrestamo;
            return;
        }
        
        // Sistema francés
        BigDecimal factorPago = BigDecimal.ONE.add(tasaInteresMensual).pow(plazoMeses);
        BigDecimal numerador = montoPrestamo.multiply(tasaInteresMensual).multiply(factorPago);
        BigDecimal denominador = factorPago.subtract(BigDecimal.ONE);
        
        this.cuotaMensual = numerador.divide(denominador, 2, java.math.RoundingMode.HALF_UP);
        this.montoTotal = this.cuotaMensual.multiply(new BigDecimal(plazoMeses));
    }
    
    /**
     * Calcula la deuda actual del préstamo.
     * 
     * @return monto total - suma de pagos realizados
     */
    public BigDecimal calcularDeudaActual() {
        if (montoTotal == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPagado = pagos != null ? pagos.stream()
                .map(Pago::getMontoPago)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
        
        return montoTotal.subtract(totalPagado);
    }
    
    /**
     * Obtiene el porcentaje de avance del préstamo.
     * 
     * @return porcentaje pagado (0.0 a 1.0)
     */
    public BigDecimal calcularPorcentajeAvance() {
        if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPagado = montoTotal.subtract(calcularDeudaActual());
        return totalPagado.divide(montoTotal, 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Cuenta las cuotas pendientes de pago.
     * 
     * @return número de cuotas pendientes
     */
    public int contarCuotasPendientes() {
        return cronogramaPagos != null ? (int) cronogramaPagos.stream()
                .filter(cuota -> !cuota.isPagado())
                .count() : 0;
    }
    
    /**
     * Cuenta las cuotas vencidas.
     * 
     * @return número de cuotas vencidas
     */
    public int contarCuotasVencidas() {
        return cronogramaPagos != null ? (int) cronogramaPagos.stream()
                .filter(cuota -> !cuota.isPagado() && cuota.estaVencida())
                .count() : 0;
    }
    
    /**
     * Verifica si el préstamo está al día.
     * 
     * @return true si no tiene cuotas vencidas
     */
    public boolean estaAlDia() {
        return contarCuotasVencidas() == 0;
    }
    
    /**
     * Verifica si el préstamo está completamente pagado.
     * 
     * @return true si todas las cuotas están pagadas
     */
    public boolean estaCompletamentePagado() {
        return contarCuotasPendientes() == 0;
    }
    
    /**
     * Obtiene la próxima cuota a vencer.
     * 
     * @return próxima cuota pendiente o null si no hay
     */
    public CronogramaPago getProximaCuota() {
        return cronogramaPagos != null ? cronogramaPagos.stream()
                .filter(cuota -> !cuota.isPagado())
                .min((c1, c2) -> c1.getFechaVencimiento().compareTo(c2.getFechaVencimiento()))
                .orElse(null) : null;
    }
    
    /**
     * Obtiene el nombre completo del cliente.
     * 
     * @return nombre completo del cliente
     */
    public String getNombreCliente() {
        return cliente != null ? cliente.getNombreCompleto() : "";
    }
    
    /**
     * Obtiene el código del asesor.
     * 
     * @return código del asesor
     */
    public String getCodigoAsesor() {
        return asesor != null ? asesor.getCodigoAsesor() : "";
    }
    
    /**
     * Marca el préstamo como actualizado.
     */
    public void marcarComoActualizado() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Actualiza el estado del préstamo basándose en las cuotas.
     */
    public void actualizarEstado() {
        if (estaCompletamentePagado()) {
            this.estado = EstadoPrestamo.PAGADO;
        } else if (contarCuotasVencidas() > 0) {
            this.estado = EstadoPrestamo.VENCIDO;
        } else {
            this.estado = EstadoPrestamo.ACTIVO;
        }
        marcarComoActualizado();
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNumeroPrestamo() {
        return numeroPrestamo;
    }
    
    public void setNumeroPrestamo(String numeroPrestamo) {
        this.numeroPrestamo = numeroPrestamo;
        marcarComoActualizado();
    }
    
    public Integer getSolicitudId() {
        return solicitudId;
    }
    
    public void setSolicitudId(Integer solicitudId) {
        this.solicitudId = solicitudId;
        marcarComoActualizado();
    }
    
    public Integer getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
        marcarComoActualizado();
    }
    
    public Integer getAsesorId() {
        return asesorId;
    }
    
    public void setAsesorId(Integer asesorId) {
        this.asesorId = asesorId;
        marcarComoActualizado();
    }
    
    public BigDecimal getMontoPrestamo() {
        return montoPrestamo;
    }
    
    public void setMontoPrestamo(BigDecimal montoPrestamo) {
        this.montoPrestamo = montoPrestamo;
        calcularCuotaYMontoTotal();
        marcarComoActualizado();
    }
    
    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
    
    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
        marcarComoActualizado();
    }
    
    public Integer getPlazoMeses() {
        return plazoMeses;
    }
    
    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
        calcularCuotaYMontoTotal();
        marcarComoActualizado();
    }
    
    public BigDecimal getTasaInteresMensual() {
        return tasaInteresMensual;
    }
    
    public void setTasaInteresMensual(BigDecimal tasaInteresMensual) {
        this.tasaInteresMensual = tasaInteresMensual;
        calcularCuotaYMontoTotal();
        marcarComoActualizado();
    }
    
    public BigDecimal getCuotaMensual() {
        return cuotaMensual;
    }
    
    public void setCuotaMensual(BigDecimal cuotaMensual) {
        this.cuotaMensual = cuotaMensual;
        marcarComoActualizado();
    }
    
    public EstadoPrestamo getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
        marcarComoActualizado();
    }
    
    public LocalDate getFechaDesembolso() {
        return fechaDesembolso;
    }
    
    public void setFechaDesembolso(LocalDate fechaDesembolso) {
        this.fechaDesembolso = fechaDesembolso;
        marcarComoActualizado();
    }
    
    public LocalDate getFechaPrimerVencimiento() {
        return fechaPrimerVencimiento;
    }
    
    public void setFechaPrimerVencimiento(LocalDate fechaPrimerVencimiento) {
        this.fechaPrimerVencimiento = fechaPrimerVencimiento;
        marcarComoActualizado();
    }
    
    public LocalDate getFechaUltimoVencimiento() {
        return fechaUltimoVencimiento;
    }
    
    public void setFechaUltimoVencimiento(LocalDate fechaUltimoVencimiento) {
        this.fechaUltimoVencimiento = fechaUltimoVencimiento;
        marcarComoActualizado();
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        marcarComoActualizado();
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Solicitud getSolicitud() {
        return solicitud;
    }
    
    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
        this.solicitudId = solicitud != null ? solicitud.getId() : null;
        marcarComoActualizado();
    }
    
    public Usuario getCliente() {
        return cliente;
    }
    
    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
        this.clienteId = cliente != null ? cliente.getId() : null;
        marcarComoActualizado();
    }
    
    public Asesor getAsesor() {
        return asesor;
    }
    
    public void setAsesor(Asesor asesor) {
        this.asesor = asesor;
        this.asesorId = asesor != null ? asesor.getId() : null;
        marcarComoActualizado();
    }
    
    public List<CronogramaPago> getCronogramaPagos() {
        return cronogramaPagos;
    }
    
    public void setCronogramaPagos(List<CronogramaPago> cronogramaPagos) {
        this.cronogramaPagos = cronogramaPagos;
    }
    
    public List<Pago> getPagos() {
        return pagos;
    }
    
    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(id, prestamo.id) ||
               (Objects.equals(numeroPrestamo, prestamo.numeroPrestamo) && numeroPrestamo != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, numeroPrestamo);
    }
    
    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", numeroPrestamo='" + numeroPrestamo + '\'' +
                ", cliente='" + getNombreCliente() + '\'' +
                ", montoPrestamo=" + montoPrestamo +
                ", estado=" + estado +
                '}';
    }
}


