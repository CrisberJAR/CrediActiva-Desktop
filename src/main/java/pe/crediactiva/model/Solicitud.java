package pe.crediactiva.model;

import pe.crediactiva.model.enums.EstadoSolicitud;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa una solicitud de préstamo en el sistema CrediActiva.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Solicitud {
    
    private Integer id;
    private String numeroSolicitud;
    private Integer clienteId;
    private Integer asesorId;
    private String nombresCliente;
    private String apellidosCliente;
    private String documentoCliente;
    private String telefonoCliente;
    private String emailCliente;
    private String direccionCliente;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaInteresMensual;
    private String finalidad;
    private BigDecimal ingresosMensuales;
    private EstadoSolicitud estado;
    private String observaciones;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private LocalDateTime fechaDecision;
    private Integer revisadoPor;
    
    // Relaciones
    private Usuario cliente;
    private Asesor asesor;
    private Usuario revisor;
    
    // Constructores
    public Solicitud() {
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
    }
    
    public Solicitud(String numeroSolicitud, Asesor asesor, String nombresCliente, String apellidosCliente, 
                    String documentoCliente, BigDecimal montoSolicitado, Integer plazoMeses) {
        this();
        this.numeroSolicitud = numeroSolicitud;
        this.asesor = asesor;
        this.asesorId = asesor != null ? asesor.getId() : null;
        this.nombresCliente = nombresCliente;
        this.apellidosCliente = apellidosCliente;
        this.documentoCliente = documentoCliente;
        this.montoSolicitado = montoSolicitado;
        this.plazoMeses = plazoMeses;
    }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el nombre completo del cliente.
     * 
     * @return nombres y apellidos concatenados
     */
    public String getNombreCompletoCliente() {
        return (nombresCliente != null ? nombresCliente : "") + " " + 
               (apellidosCliente != null ? apellidosCliente : "");
    }
    
    /**
     * Verifica si la solicitud puede ser editada.
     * 
     * @return true si puede ser editada
     */
    public boolean puedeEditar() {
        return estado != null && estado.puedeEditar();
    }
    
    /**
     * Verifica si la solicitud puede ser aprobada.
     * 
     * @return true si puede ser aprobada
     */
    public boolean puedeAprobar() {
        return estado != null && estado.puedeAprobar();
    }
    
    /**
     * Verifica si la solicitud puede ser rechazada.
     * 
     * @return true si puede ser rechazada
     */
    public boolean puedeRechazar() {
        return estado != null && estado.puedeRechazar();
    }
    
    /**
     * Cambia el estado de la solicitud a EN_REVISION.
     */
    public void marcarEnRevision() {
        if (puedeEditar()) {
            this.estado = EstadoSolicitud.EN_REVISION;
            this.fechaRevision = LocalDateTime.now();
        }
    }
    
    /**
     * Aprueba la solicitud.
     * 
     * @param revisor el usuario que aprueba
     * @param observaciones observaciones de la aprobación
     */
    public void aprobar(Usuario revisor, String observaciones) {
        if (puedeAprobar()) {
            this.estado = EstadoSolicitud.APROBADA;
            this.revisor = revisor;
            this.revisadoPor = revisor != null ? revisor.getId() : null;
            this.observaciones = observaciones;
            this.fechaDecision = LocalDateTime.now();
            if (this.fechaRevision == null) {
                this.fechaRevision = LocalDateTime.now();
            }
        }
    }
    
    /**
     * Rechaza la solicitud.
     * 
     * @param revisor el usuario que rechaza
     * @param observaciones motivo del rechazo
     */
    public void rechazar(Usuario revisor, String observaciones) {
        if (puedeRechazar()) {
            this.estado = EstadoSolicitud.RECHAZADA;
            this.revisor = revisor;
            this.revisadoPor = revisor != null ? revisor.getId() : null;
            this.observaciones = observaciones;
            this.fechaDecision = LocalDateTime.now();
            if (this.fechaRevision == null) {
                this.fechaRevision = LocalDateTime.now();
            }
        }
    }
    
    /**
     * Cancela la solicitud.
     * 
     * @param observaciones motivo de la cancelación
     */
    public void cancelar(String observaciones) {
        if (puedeEditar()) {
            this.estado = EstadoSolicitud.CANCELADA;
            this.observaciones = observaciones;
            this.fechaDecision = LocalDateTime.now();
        }
    }
    
    /**
     * Calcula la cuota mensual estimada usando el sistema francés.
     * 
     * @return cuota mensual estimada
     */
    public BigDecimal calcularCuotaMensualEstimada() {
        if (montoSolicitado == null || tasaInteresMensual == null || plazoMeses == null || plazoMeses <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (tasaInteresMensual.compareTo(BigDecimal.ZERO) == 0) {
            return montoSolicitado.divide(new BigDecimal(plazoMeses), 2, java.math.RoundingMode.HALF_UP);
        }
        
        BigDecimal factorPago = BigDecimal.ONE.add(tasaInteresMensual).pow(plazoMeses);
        BigDecimal numerador = montoSolicitado.multiply(tasaInteresMensual).multiply(factorPago);
        BigDecimal denominador = factorPago.subtract(BigDecimal.ONE);
        
        return numerador.divide(denominador, 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula el monto total a pagar (capital + intereses).
     * 
     * @return monto total estimado
     */
    public BigDecimal calcularMontoTotalEstimado() {
        BigDecimal cuotaMensual = calcularCuotaMensualEstimada();
        if (cuotaMensual.compareTo(BigDecimal.ZERO) == 0 || plazoMeses == null) {
            return montoSolicitado != null ? montoSolicitado : BigDecimal.ZERO;
        }
        return cuotaMensual.multiply(new BigDecimal(plazoMeses));
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNumeroSolicitud() {
        return numeroSolicitud;
    }
    
    public void setNumeroSolicitud(String numeroSolicitud) {
        this.numeroSolicitud = numeroSolicitud;
    }
    
    public Integer getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }
    
    public Integer getAsesorId() {
        return asesorId;
    }
    
    public void setAsesorId(Integer asesorId) {
        this.asesorId = asesorId;
    }
    
    public String getNombresCliente() {
        return nombresCliente;
    }
    
    public void setNombresCliente(String nombresCliente) {
        this.nombresCliente = nombresCliente;
    }
    
    public String getApellidosCliente() {
        return apellidosCliente;
    }
    
    public void setApellidosCliente(String apellidosCliente) {
        this.apellidosCliente = apellidosCliente;
    }
    
    public String getDocumentoCliente() {
        return documentoCliente;
    }
    
    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }
    
    public String getTelefonoCliente() {
        return telefonoCliente;
    }
    
    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }
    
    public String getEmailCliente() {
        return emailCliente;
    }
    
    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }
    
    public String getDireccionCliente() {
        return direccionCliente;
    }
    
    public void setDireccionCliente(String direccionCliente) {
        this.direccionCliente = direccionCliente;
    }
    
    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }
    
    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }
    
    public Integer getPlazoMeses() {
        return plazoMeses;
    }
    
    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }
    
    public BigDecimal getTasaInteresMensual() {
        return tasaInteresMensual;
    }
    
    public void setTasaInteresMensual(BigDecimal tasaInteresMensual) {
        this.tasaInteresMensual = tasaInteresMensual;
    }
    
    public String getFinalidad() {
        return finalidad;
    }
    
    public void setFinalidad(String finalidad) {
        this.finalidad = finalidad;
    }
    
    public BigDecimal getIngresosMensuales() {
        return ingresosMensuales;
    }
    
    public void setIngresosMensuales(BigDecimal ingresosMensuales) {
        this.ingresosMensuales = ingresosMensuales;
    }
    
    public EstadoSolicitud getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }
    
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
    
    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }
    
    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }
    
    public LocalDateTime getFechaDecision() {
        return fechaDecision;
    }
    
    public void setFechaDecision(LocalDateTime fechaDecision) {
        this.fechaDecision = fechaDecision;
    }
    
    public Integer getRevisadoPor() {
        return revisadoPor;
    }
    
    public void setRevisadoPor(Integer revisadoPor) {
        this.revisadoPor = revisadoPor;
    }
    
    public Usuario getCliente() {
        return cliente;
    }
    
    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
        this.clienteId = cliente != null ? cliente.getId() : null;
    }
    
    public Asesor getAsesor() {
        return asesor;
    }
    
    public void setAsesor(Asesor asesor) {
        this.asesor = asesor;
        this.asesorId = asesor != null ? asesor.getId() : null;
    }
    
    public Usuario getRevisor() {
        return revisor;
    }
    
    public void setRevisor(Usuario revisor) {
        this.revisor = revisor;
        this.revisadoPor = revisor != null ? revisor.getId() : null;
    }
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solicitud solicitud = (Solicitud) o;
        return Objects.equals(id, solicitud.id) ||
               (Objects.equals(numeroSolicitud, solicitud.numeroSolicitud) && numeroSolicitud != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, numeroSolicitud);
    }
    
    @Override
    public String toString() {
        return "Solicitud{" +
                "id=" + id +
                ", numeroSolicitud='" + numeroSolicitud + '\'' +
                ", cliente='" + getNombreCompletoCliente() + '\'' +
                ", montoSolicitado=" + montoSolicitado +
                ", estado=" + estado +
                '}';
    }
}
