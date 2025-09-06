package pe.crediactiva.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un cliente en el sistema CrediActiva.
 * Contiene información específica de clientes como límite de crédito y score crediticio.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Cliente {
    
    /**
     * Enumeración para tipos de cliente.
     */
    public enum TipoCliente {
        NUEVO("Nuevo", "Cliente nuevo sin historial crediticio"),
        RECURRENTE("Recurrente", "Cliente con historial de pagos positivo"),
        VIP("VIP", "Cliente preferencial con excelente historial");
        
        private final String nombre;
        private final String descripcion;
        
        TipoCliente(String nombre, String descripcion) {
            this.nombre = nombre;
            this.descripcion = descripcion;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        @Override
        public String toString() {
            return nombre;
        }
    }
    
    private Integer id;
    private Integer usuarioId;
    private String codigoCliente;
    private TipoCliente tipoCliente;
    private BigDecimal limiteCredito;
    private Integer scoreCrediticio;
    private BigDecimal ingresosDeclarados;
    private String ocupacion;
    private String empresa;
    private String referenciasPersonales;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    private Usuario usuario;
    
    // Constructores
    public Cliente() {
        this.activo = true;
        this.tipoCliente = TipoCliente.NUEVO;
        this.limiteCredito = BigDecimal.ZERO;
        this.scoreCrediticio = 0;
        this.ingresosDeclarados = BigDecimal.ZERO;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public Cliente(Usuario usuario, String codigoCliente) {
        this();
        this.usuario = usuario;
        this.usuarioId = usuario != null ? usuario.getId() : null;
        this.codigoCliente = codigoCliente;
    }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el nombre completo del cliente.
     * 
     * @return nombre completo del usuario asociado
     */
    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }
    
    /**
     * Verifica si el cliente está activo y su usuario también.
     * 
     * @return true si ambos están activos
     */
    public boolean estaActivoCompleto() {
        return activo && usuario != null && usuario.isActivo();
    }
    
    /**
     * Obtiene la calificación crediticia en texto.
     * 
     * @return descripción del score crediticio
     */
    public String getCalificacionCrediticia() {
        if (scoreCrediticio >= 800) {
            return "Excelente";
        } else if (scoreCrediticio >= 700) {
            return "Muy Bueno";
        } else if (scoreCrediticio >= 600) {
            return "Bueno";
        } else if (scoreCrediticio >= 500) {
            return "Regular";
        } else {
            return "Malo";
        }
    }
    
    /**
     * Verifica si el cliente puede solicitar un monto específico.
     * 
     * @param monto monto a verificar
     * @return true si está dentro del límite de crédito
     */
    public boolean puedesolicitarMonto(BigDecimal monto) {
        if (monto == null || limiteCredito == null) {
            return false;
        }
        return monto.compareTo(limiteCredito) <= 0;
    }
    
    /**
     * Actualiza el score crediticio.
     * 
     * @param nuevoScore nuevo score (0-1000)
     */
    public void actualizarScore(Integer nuevoScore) {
        if (nuevoScore != null && nuevoScore >= 0 && nuevoScore <= 1000) {
            this.scoreCrediticio = nuevoScore;
            marcarComoActualizado();
        }
    }
    
    /**
     * Aumenta el límite de crédito.
     * 
     * @param incremento monto a incrementar
     */
    public void aumentarLimiteCredito(BigDecimal incremento) {
        if (incremento != null && incremento.compareTo(BigDecimal.ZERO) > 0) {
            this.limiteCredito = this.limiteCredito.add(incremento);
            marcarComoActualizado();
        }
    }
    
    /**
     * Marca el cliente como actualizado.
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
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
        marcarComoActualizado();
    }
    
    public String getCodigoCliente() {
        return codigoCliente;
    }
    
    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
        marcarComoActualizado();
    }
    
    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }
    
    public void setTipoCliente(TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
        marcarComoActualizado();
    }
    
    public BigDecimal getLimiteCredito() {
        return limiteCredito;
    }
    
    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
        marcarComoActualizado();
    }
    
    public Integer getScoreCrediticio() {
        return scoreCrediticio;
    }
    
    public void setScoreCrediticio(Integer scoreCrediticio) {
        this.scoreCrediticio = scoreCrediticio;
        marcarComoActualizado();
    }
    
    public BigDecimal getIngresosDeclarados() {
        return ingresosDeclarados;
    }
    
    public void setIngresosDeclarados(BigDecimal ingresosDeclarados) {
        this.ingresosDeclarados = ingresosDeclarados;
        marcarComoActualizado();
    }
    
    public String getOcupacion() {
        return ocupacion;
    }
    
    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
        marcarComoActualizado();
    }
    
    public String getEmpresa() {
        return empresa;
    }
    
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
        marcarComoActualizado();
    }
    
    public String getReferenciasPersonales() {
        return referenciasPersonales;
    }
    
    public void setReferenciasPersonales(String referenciasPersonales) {
        this.referenciasPersonales = referenciasPersonales;
        marcarComoActualizado();
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
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
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.usuarioId = usuario != null ? usuario.getId() : null;
        marcarComoActualizado();
    }
    
    // equals, hashCode y toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id) ||
               (Objects.equals(codigoCliente, cliente.codigoCliente) && codigoCliente != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, codigoCliente);
    }
    
    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", codigoCliente='" + codigoCliente + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", tipoCliente=" + tipoCliente +
                ", limiteCredito=" + limiteCredito +
                ", scoreCrediticio=" + scoreCrediticio +
                ", activo=" + activo +
                '}';
    }
}
