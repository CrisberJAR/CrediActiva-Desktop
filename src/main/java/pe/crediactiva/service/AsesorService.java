package pe.crediactiva.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.dao.interfaces.AsesorDAO;
import pe.crediactiva.dao.mysql.AsesorDAOImpl;
import pe.crediactiva.model.Asesor;
import pe.crediactiva.model.Usuario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de asesores en CrediActiva.
 * Contiene la lógica de negocio relacionada con asesores.
 * 
 * @author CrediActiva Development Team
 * @version 1.0
 */
public class AsesorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsesorService.class);
    
    private final AsesorDAO asesorDAO;
    
    // Constructor
    public AsesorService() {
        this.asesorDAO = new AsesorDAOImpl();
    }
    
    // Constructor para inyección de dependencias (testing)
    public AsesorService(AsesorDAO asesorDAO) {
        this.asesorDAO = asesorDAO;
    }
    
    /**
     * Crea un nuevo asesor a partir de un usuario.
     * 
     * @param usuario usuario que será asesor
     * @return asesor creado o null si hay error
     */
    public Asesor crearAsesor(Usuario usuario) {
        return crearAsesor(usuario, null, null);
    }
    
    /**
     * Crea un nuevo asesor con parámetros específicos.
     * 
     * @param usuario usuario que será asesor
     * @param comisionPorcentaje comisión del asesor (opcional, usa default si es null)
     * @param metaMensual meta mensual del asesor (opcional, usa 0 si es null)
     * @return asesor creado o null si hay error
     */
    public Asesor crearAsesor(Usuario usuario, BigDecimal comisionPorcentaje, BigDecimal metaMensual) {
        logger.info("🏢 ASESOR SERVICE: Creando asesor para usuario: {} (ID: {})", usuario.getUsername(), usuario.getId());
        
        try {
            // Validaciones
            if (usuario == null || usuario.getId() == null) {
                logger.error("❌ Error: Usuario es null o no tiene ID");
                throw new IllegalArgumentException("Usuario es requerido y debe tener ID");
            }
            
            logger.info("✅ Usuario válido: {} - {}", usuario.getUsername(), usuario.getNombreCompleto());
            
            // Verificar que no existe ya un asesor para este usuario
            boolean yaExiste = asesorDAO.existsByUsuarioId(usuario.getId());
            logger.info("🔍 Verificando existencia previa: {}", yaExiste ? "YA EXISTE" : "NO EXISTE");
            
            if (yaExiste) {
                logger.error("❌ Ya existe un asesor para este usuario");
                throw new IllegalArgumentException("Ya existe un asesor para este usuario");
            }
            
            // Generar código de asesor único
            logger.info("🔢 Generando código de asesor...");
            String codigoAsesor = asesorDAO.generarSiguienteCodigoAsesor();
            logger.info("✅ Código generado: {}", codigoAsesor);
            
            // Crear asesor
            logger.info("🏗️ Creando objeto Asesor...");
            Asesor asesor = new Asesor(usuario, codigoAsesor);
            
            // Establecer comisión (usar default si no se especifica)
            if (comisionPorcentaje != null) {
                asesor.setComisionPorcentaje(comisionPorcentaje);
                logger.info("💰 Comisión personalizada: {}%", comisionPorcentaje.multiply(BigDecimal.valueOf(100)));
            } else {
                logger.info("💰 Usando comisión por defecto: {}%", asesor.getComisionPorcentaje().multiply(BigDecimal.valueOf(100)));
            }
            
            // Establecer meta mensual (usar 0 si no se especifica)
            if (metaMensual != null) {
                asesor.setMetaMensual(metaMensual);
                logger.info("🎯 Meta personalizada: S/ {}", metaMensual);
            } else {
                logger.info("🎯 Usando meta por defecto: S/ {}", asesor.getMetaMensual());
            }
            
            // Guardar asesor
            logger.info("💾 Guardando asesor en base de datos...");
            Asesor asesorCreado = asesorDAO.save(asesor);
            
            if (asesorCreado != null) {
                logger.info("✅ ÉXITO TOTAL: Asesor creado exitosamente: {} para usuario {} (ID: {})", 
                           asesorCreado.getCodigoAsesor(), usuario.getUsername(), asesorCreado.getId());
            } else {
                logger.error("❌ FALLÓ: asesorDAO.save() retornó null");
            }
            
            return asesorCreado;
            
        } catch (Exception e) {
            logger.error("💥 EXCEPCIÓN en AsesorService.crearAsesor() para usuario: {}", usuario.getUsername(), e);
            throw new RuntimeException("Error al crear asesor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un asesor por su ID.
     * 
     * @param id ID del asesor
     * @return Optional con el asesor si existe
     */
    public Optional<Asesor> buscarPorId(Integer id) {
        try {
            return asesorDAO.findById(id);
        } catch (Exception e) {
            logger.error("Error al buscar asesor por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Busca un asesor por el ID del usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Optional con el asesor si existe
     */
    public Optional<Asesor> buscarPorUsuarioId(Integer usuarioId) {
        try {
            return asesorDAO.findByUsuarioId(usuarioId);
        } catch (Exception e) {
            logger.error("Error al buscar asesor por usuario ID: {}", usuarioId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Busca un asesor por su código.
     * 
     * @param codigoAsesor código del asesor
     * @return Optional con el asesor si existe
     */
    public Optional<Asesor> buscarPorCodigo(String codigoAsesor) {
        try {
            return asesorDAO.findByCodigoAsesor(codigoAsesor);
        } catch (Exception e) {
            logger.error("Error al buscar asesor por código: {}", codigoAsesor, e);
            return Optional.empty();
        }
    }
    
    /**
     * Obtiene todos los asesores activos.
     * 
     * @return lista de asesores activos
     */
    public List<Asesor> obtenerAsesoresActivos() {
        try {
            return asesorDAO.findAllActive();
        } catch (Exception e) {
            logger.error("Error al obtener asesores activos", e);
            return List.of();
        }
    }
    
    /**
     * Obtiene todos los asesores del sistema (activos e inactivos).
     * 
     * @return lista de todos los asesores
     */
    public List<Asesor> obtenerTodosLosAsesores() {
        try {
            return asesorDAO.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todos los asesores", e);
            return List.of();
        }
    }
    
    /**
     * Actualiza un asesor existente.
     * 
     * @param asesor asesor con datos actualizados
     * @return asesor actualizado o null si hay error
     */
    public Asesor actualizarAsesor(Asesor asesor) {
        logger.debug("Actualizando asesor: {}", asesor.getCodigoAsesor());
        
        try {
            // Validaciones
            if (asesor == null || asesor.getId() == null) {
                throw new IllegalArgumentException("Asesor y ID son requeridos");
            }
            
            // Verificar que el asesor existe
            Optional<Asesor> asesorExistente = asesorDAO.findById(asesor.getId());
            if (asesorExistente.isEmpty()) {
                throw new IllegalArgumentException("Asesor no encontrado");
            }
            
            // Actualizar asesor
            Asesor asesorActualizado = asesorDAO.update(asesor);
            
            if (asesorActualizado != null) {
                logger.info("Asesor actualizado exitosamente: {}", asesor.getCodigoAsesor());
            }
            
            return asesorActualizado;
            
        } catch (Exception e) {
            logger.error("Error al actualizar asesor: {}", asesor.getCodigoAsesor(), e);
            throw new RuntimeException("Error al actualizar asesor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Desactiva un asesor.
     * 
     * @param asesorId ID del asesor
     * @return true si se desactivó exitosamente
     */
    public boolean desactivarAsesor(Integer asesorId) {
        try {
            boolean resultado = asesorDAO.deactivate(asesorId);
            if (resultado) {
                logger.info("Asesor desactivado exitosamente: ID {}", asesorId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al desactivar asesor: ID {}", asesorId, e);
            return false;
        }
    }
    
    /**
     * Activa un asesor previamente desactivado.
     * 
     * @param asesorId ID del asesor
     * @return true si se activó exitosamente
     */
    public boolean activarAsesor(Integer asesorId) {
        try {
            boolean resultado = asesorDAO.activate(asesorId);
            if (resultado) {
                logger.info("Asesor activado exitosamente: ID {}", asesorId);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al activar asesor: ID {}", asesorId, e);
            return false;
        }
    }
    
    /**
     * Verifica si un usuario ya tiene un registro de asesor.
     * 
     * @param usuarioId ID del usuario
     * @return true si ya es asesor
     */
    public boolean esAsesor(Integer usuarioId) {
        try {
            return asesorDAO.existsByUsuarioId(usuarioId);
        } catch (Exception e) {
            logger.error("Error al verificar si usuario es asesor: {}", usuarioId, e);
            return false;
        }
    }
    
    /**
     * Cuenta el total de asesores activos.
     * 
     * @return número de asesores activos
     */
    public long contarAsesoresActivos() {
        try {
            return asesorDAO.countActive();
        } catch (Exception e) {
            logger.error("Error al contar asesores activos", e);
            return 0;
        }
    }
    
    /**
     * Genera el siguiente código de asesor disponible.
     * 
     * @return código único para asesor
     */
    public String generarCodigoAsesor() {
        try {
            return asesorDAO.generarSiguienteCodigoAsesor();
        } catch (Exception e) {
            logger.error("Error al generar código de asesor", e);
            return "ASE" + System.currentTimeMillis() % 10000;
        }
    }
}
