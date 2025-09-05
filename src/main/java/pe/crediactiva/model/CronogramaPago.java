package pe.crediactiva.model;

import pe.crediactiva.model.enums.EstadoCuota;
import pe.crediactiva.util.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa una cuota del cronograma de pagos en CrediActiva.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class CronogramaPago {
    
    private Integer id;
    private Integer prestamoId;
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal montoCuota;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal saldoPendiente;
    private boolean pagado;
    private LocalDate fechaPago;
    private BigDecimal montoPagado;
    private int diasAtraso;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    private Prestamo prestamo;
    
    // Constructores
    public CronogramaPago() {
        this.pagado = false;
        this.montoPagado = BigDecimal.ZERO;
        this.diasAtraso = 0;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public CronogramaPago(Integer prestamoId, Integer numeroCuota, LocalDate fechaVencimiento, 
                         BigDecimal montoCuota, BigDecimal capital, BigDecimal interes, 
                         BigDecimal saldoPendiente) {
        this();
        this.prestamoId = prestamoId;
        this.numeroCuota = numeroCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.montoCuota = montoCuota;
        this.capital = capital;
        this.interes = interes;
        this.saldoPendiente = saldoPendiente;
    }
    
    // Métodos de utilidad
    
    /**
     * Calcula el estado dinámico de la cuota basándose en la fecha actual.
     * 
     * @return estado de la cuota
     */
    public EstadoCuota calcularEstado() {
        if (pagado) {
            return EstadoCuota.PAGADO;
        }
        
        int diasAtrasoActual = calcularDiasAtrasoActual();
        return EstadoCuota.calcularEstado(false, diasAtrasoActual);
    }
    
    /**
     * Calcula los días de atraso actuales.
     * 
     * @return días de atraso (0 si no está vencida)
     */
    public int calcularDiasAtrasoActual() {
        if (pagado || fechaVencimiento == null) {
            return 0;
        }
        
        return DateUtils.calcularDiasAtraso(fechaVencimiento);
    }
    
    /**
     * Verifica si la cuota está vencida.
     * 
     * @return true si está vencida y no pagada
     */
    public boolean estaVencida() {
        return !pagado && DateUtils.estaVencida(fechaVencimiento);
    }
    
    /**
     * Verifica si la cuota está dentro del plazo.
     * 
     * @return true si no está vencida
     */
    public boolean estaDentroPlazo() {
        return pagado || DateUtils.estaDentroPlazo(fechaVencimiento);
    }
    
    /**
     * Verifica si la cuota requiere atención urgente.
     * 
     * @return true si está muy atrasada
     */
    public boolean requiereAtencionUrgente() {
        return calcularEstado().requiereAtencionUrgente();
    }
    
    /**
     * Registra el pago de la cuota.
     * 
     * @param montoPago monto pagado
     * @param fechaPago fecha del pago
     * @param observaciones observaciones del pago
     */
    public void registrarPago(BigDecimal montoPago, LocalDate fechaPago, String observaciones) {
        if (montoPago == null || montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        
        this.montoPagado = (this.montoPagado != null ? this.montoPagado : BigDecimal.ZERO).add(montoPago);
        this.fechaPago = fechaPago;
        this.observaciones = observaciones;
        
        // Verificar si se completó el pago
        if (this.montoPagado.compareTo(montoCuota) >= 0) {
            this.pagado = true;
        }
        
        // Calcular días de atraso al momento del pago
        if (fechaPago != null && fechaVencimiento != null) {
            long dias = DateUtils.diasEntre(fechaVencimiento, fechaPago);
            this.diasAtraso = Math.max(0, (int) dias);
        }
        
        marcarComoActualizado();
    }
    
    /**
     * Obtiene el saldo pendiente de la cuota.
     * 
     * @return saldo pendiente
     */
    public BigDecimal getSaldoPendienteCuota() {
        if (pagado || montoCuota == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal pagadoTotal = montoPagado != null ? montoPagado : BigDecimal.ZERO;
        return montoCuota.subtract(pagadoTotal);
    }
    
    /**
     * Verifica si la cuota tiene pago parcial.
     * 
     * @return true si tiene pago parcial
     */
    public boolean tienePagoParcial() {
        return !pagado && montoPagado != null && montoPagado.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Obtiene el porcentaje pagado de la cuota.
     * 
     * @return porcentaje pagado (0.0 a 1.0)
     */
    public BigDecimal getPorcentajePagado() {
        if (montoCuota == null || montoCuota.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal pagadoTotal = montoPagado != null ? montoPagado : BigDecimal.ZERO;
        return pagadoTotal.divide(montoCuota, 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Obtiene una descripción del estado de la cuota.
     * 
     * @return descripción del estado
     */
    public String getDescripcionEstado() {
        EstadoCuota estado = calcularEstado();
        int diasAtraso = calcularDiasAtrasoActual();
        
        return switch (estado) {
            case PAGADO -> "Pagado el " + DateUtils.formatearFecha(fechaPago);
            case PUNTUAL -> "Vence el " + DateUtils.formatearFecha(fechaVencimiento);
            case ATRASADO -> "Vencido hace " + diasAtraso + " día(s)";
            case MUY_ATRASADO -> "MUY ATRASADO - " + diasAtraso + " día(s)";
        };
    }
    
    /**
     * Marca la cuota como actualizada.
     */
    public void marcarComoActualizado() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getPrestamoId() {
        return prestamoId;
    }
    
    public void setPrestamoId(Integer prestamoId) {
        this.prestamoId = prestamoId;
        marcarComoActualizado();
    }
    
    public Integer getNumeroCuota() {
        return numeroCuota;
    }
    
    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
        marcarComoActualizado();
    }
    
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }
    
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
        marcarComoActualizado();
    }
    
    public BigDecimal getMontoCuota() {
        return montoCuota;
    }
    
    public void setMontoCuota(BigDecimal montoCuota) {
        this.montoCuota = montoCuota;
        marcarComoActualizado();
    }
    
    public BigDecimal getCapital() {
        return capital;
    }
    
    public void setCapital(BigDecimal capital) {
        this.capital = capital;
        marcarComoActualizado();
    }
    
    public BigDecimal getInteres() {
        return interes;
    }
    
    public void setInteres(BigDecimal interes) {
        this.interes = interes;
        marcarComoActualizado();
    }
    
    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }
    
    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
        marcarComoActualizado();
    }
    
    public boolean isPagado() {
        return pagado;
    }
    
    public void setPagado(boolean pagado) {
        this.pagado = pagado;
        marcarComoActualizado();
    }
    
    public LocalDate getFechaPago() {
        return fechaPago;
    }
    
    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
        marcarComoActualizado();
    }
    
    public BigDecimal getMontoPagado() {
        return montoPagado;
    }
    
    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
        marcarComoActualizado();
    }
    
    public int getDiasAtraso() {
        return diasAtraso;
    }
    
    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
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
    
    public Prestamo getPrestamo() {
        return prestamo;
    }
    
    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
        this.prestamoId = prestamo != null ? prestamo.getId() : null;
        marcarComoActualizado();
    }
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CronogramaPago that = (CronogramaPago) o;
        return Objects.equals(id, that.id) ||
               (Objects.equals(prestamoId, that.prestamoId) && Objects.equals(numeroCuota, that.numeroCuota));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, prestamoId, numeroCuota);
    }
    
    @Override
    public String toString() {
        return "CronogramaPago{" +
                "id=" + id +
                ", prestamoId=" + prestamoId +
                ", numeroCuota=" + numeroCuota +
                ", fechaVencimiento=" + fechaVencimiento +
                ", montoCuota=" + montoCuota +
                ", pagado=" + pagado +
                ", estado=" + calcularEstado() +
                '}';
    }
}


