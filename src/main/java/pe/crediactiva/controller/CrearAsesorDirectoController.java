package pe.crediactiva.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.CrediActivaApp;
import pe.crediactiva.model.Asesor;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.service.AsesorService;
import pe.crediactiva.service.UsuarioService;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador súper simple para crear asesores directamente.
 */
public class CrearAsesorDirectoController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(CrearAsesorDirectoController.class);
    
    @FXML private ComboBox<Usuario> usuarioComboBox;
    @FXML private Label usuarioInfoLabel;
    @FXML private TextField codigoField;
    @FXML private TextField comisionField;
    @FXML private TextField metaField;
    @FXML private Button crearButton;
    @FXML private Button limpiarButton;
    @FXML private Button cerrarButton;
    @FXML private Label statusLabel;
    
    private UsuarioService usuarioService;
    private AsesorService asesorService;
    private Usuario usuarioSeleccionado;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Inicializando CrearAsesorDirectoController");
        
        usuarioService = new UsuarioService();
        asesorService = new AsesorService();
        
        configurarComboBox();
        cargarUsuarios();
        generarCodigo();
        configurarValidaciones();
    }
    
    private void configurarComboBox() {
        usuarioComboBox.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) return "";
                return String.format("%s - %s %s", 
                                   usuario.getUsername(), 
                                   usuario.getNombres(), 
                                   usuario.getApellidos());
            }
            
            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });
        
        usuarioComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            usuarioSeleccionado = newVal;
            actualizarInfo();
            validar();
        });
    }
    
    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            usuarioComboBox.setItems(FXCollections.observableArrayList(usuarios));
            statusLabel.setText(String.format("Usuarios cargados: %d", usuarios.size()));
        } catch (Exception e) {
            logger.error("Error al cargar usuarios", e);
            statusLabel.setText("Error al cargar usuarios");
        }
    }
    
    private void generarCodigo() {
        try {
            String codigo = asesorService.generarCodigoAsesor();
            codigoField.setText(codigo);
        } catch (Exception e) {
            logger.error("Error al generar código", e);
        }
    }
    
    private void configurarValidaciones() {
        comisionField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                comisionField.setText(oldVal);
            }
            validar();
        });
        
        metaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                metaField.setText(oldVal);
            }
            validar();
        });
    }
    
    private void actualizarInfo() {
        if (usuarioSeleccionado == null) {
            usuarioInfoLabel.setText("Seleccione un usuario");
            return;
        }
        
        try {
            boolean yaEsAsesor = asesorService.esAsesor(usuarioSeleccionado.getId());
            if (yaEsAsesor) {
                usuarioInfoLabel.setText("⚠️ Este usuario YA es asesor");
                usuarioInfoLabel.setStyle("-fx-text-fill: #dc3545;");
            } else {
                usuarioInfoLabel.setText(String.format("✅ Usuario: %s - Email: %s", 
                                                      usuarioSeleccionado.getNombreCompleto(),
                                                      usuarioSeleccionado.getEmail()));
                usuarioInfoLabel.setStyle("-fx-text-fill: #28a745;");
            }
        } catch (Exception e) {
            usuarioInfoLabel.setText("Error al verificar usuario");
            usuarioInfoLabel.setStyle("-fx-text-fill: #dc3545;");
        }
    }
    
    private void validar() {
        boolean valido = usuarioSeleccionado != null && 
                        !comisionField.getText().trim().isEmpty() && 
                        !metaField.getText().trim().isEmpty();
        
        crearButton.setDisable(!valido);
        
        if (valido) {
            statusLabel.setText("Listo para crear asesor");
        } else {
            statusLabel.setText("Complete todos los campos");
        }
    }
    
    @FXML
    private void handleCrear() {
        logger.info("🏢 Creando asesor para usuario: {}", usuarioSeleccionado.getUsername());
        
        try {
            // Verificar si ya es asesor
            if (asesorService.esAsesor(usuarioSeleccionado.getId())) {
                CrediActivaApp.showWarningAlert("Advertencia", "Usuario ya es asesor", 
                                               "Este usuario ya tiene un registro de asesor.");
                return;
            }
            
            // Obtener valores
            BigDecimal comision = new BigDecimal(comisionField.getText().trim()).divide(BigDecimal.valueOf(100));
            BigDecimal meta = new BigDecimal(metaField.getText().trim());
            
            // Crear asesor
            Asesor asesor = asesorService.crearAsesor(usuarioSeleccionado, comision, meta);
            
            if (asesor != null) {
                statusLabel.setText("✅ Asesor creado exitosamente");
                CrediActivaApp.showInfoAlert("Éxito", "Asesor Creado", 
                                           String.format("Asesor creado exitosamente:\n\n" +
                                                        "Usuario: %s\n" +
                                                        "Código: %s\n" +
                                                        "Comisión: %s%%\n" +
                                                        "Meta: S/ %s",
                                                        usuarioSeleccionado.getUsername(),
                                                        asesor.getCodigoAsesor(),
                                                        comision.multiply(BigDecimal.valueOf(100)),
                                                        meta));
                limpiar();
            } else {
                statusLabel.setText("❌ Error al crear asesor");
                CrediActivaApp.showErrorAlert("Error", "Error al crear asesor", 
                                            "No se pudo crear el asesor. Revise los logs.");
            }
            
        } catch (Exception e) {
            logger.error("Error al crear asesor", e);
            statusLabel.setText("❌ Error al crear asesor");
            CrediActivaApp.showErrorAlert("Error", "Error al crear asesor", 
                                        "Error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLimpiar() {
        limpiar();
    }
    
    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) cerrarButton.getScene().getWindow();
        stage.close();
    }
    
    private void limpiar() {
        usuarioComboBox.getSelectionModel().clearSelection();
        usuarioSeleccionado = null;
        comisionField.setText("2.0");
        metaField.setText("0.00");
        generarCodigo();
        actualizarInfo();
        validar();
        statusLabel.setText("Formulario limpiado");
    }
}
