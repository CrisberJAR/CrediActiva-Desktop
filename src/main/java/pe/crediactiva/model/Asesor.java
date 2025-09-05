package pe.crediactiva.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un asesor en el sistema CrediActiva.
 * Contiene información específica de asesores como comisiones y metas.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class Asesor {
    
    private Integer id;
    private Integer usuarioId;
    private String codigoAsesor;
    private BigDecimal comisionPorcentaje;
    private BigDecimal metaMensual;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Relaciones
    private Usuario usuario;
    
    // Constructores
    public Asesor() {
        this.activo = true;
        this.comisionPorcentaje = new BigDecimal("0.0200"); // 2% por defecto
        this.metaMensual = BigDecimal.ZERO;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public Asesor(Usuario usuario, String codigoAsesor) {
        this();
        this.usuario = usuario;
        this.usuarioId = usuario != null ? usuario.getId() : null;
        this.codigoAsesor = codigoAsesor;
    }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el nombre completo del asesor.
     * 
     * @return nombre completo del usuario asociado
     */
    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "";
    }
    
    /**
     * Calcula la comisión sobre un monto dado.
     * 
     * @param monto el monto base para calcular la comisión
     * @return la comisión calculada
     */
    public BigDecimal calcularComision(BigDecimal monto) {
        if (monto == null || comisionPorcentaje == null) {
            return BigDecimal.ZERO;
        }
        return monto.multiply(comisionPorcentaje);
    }
    
    /**
     * Verifica si el asesor está activo y su usuario también.
     * 
     * @return true si ambos están activos
     */
    public boolean estaActivoCompleto() {
        return activo && (usuario == null || usuario.isActivo());
    }
    
    /**
     * Obtiene el porcentaje de comisión como string formateado.
     * 
     * @return porcentaje formateado (ej: "2.50%")
     */
    public String getComisionPorcentajeFormateado() {
        if (comisionPorcentaje == null) return "0.00%";
        return comisionPorcentaje.multiply(new BigDecimal("100")).setScale(2) + "%";
    }
    
    /**
     * Marca el asesor como actualizado.
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
    
    public String getCodigoAsesor() {
        return codigoAsesor;
    }
    
    public void setCodigoAsesor(String codigoAsesor) {
        this.codigoAsesor = codigoAsesor;
        marcarComoActualizado();
    }
    
    public BigDecimal getComisionPorcentaje() {
        return comisionPorcentaje;
    }
    
    public void setComisionPorcentaje(BigDecimal comisionPorcentaje) {
        this.comisionPorcentaje = comisionPorcentaje;
        marcarComoActualizado();
    }
    
    public BigDecimal getMetaMensual() {
        return metaMensual;
    }
    
    public void setMetaMensual(BigDecimal metaMensual) {
        this.metaMensual = metaMensual;
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
        Asesor asesor = (Asesor) o;
        return Objects.equals(id, asesor.id) ||
               (Objects.equals(codigoAsesor, asesor.codigoAsesor) && codigoAsesor != null);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, codigoAsesor);
    }
    
    @Override
    public String toString() {
        return "Asesor{" +
                "id=" + id +
                ", codigoAsesor='" + codigoAsesor + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", comisionPorcentaje=" + comisionPorcentaje +
                ", activo=" + activo +
                '}';
    }
}


