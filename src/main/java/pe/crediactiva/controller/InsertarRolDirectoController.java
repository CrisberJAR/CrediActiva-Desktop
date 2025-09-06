package pe.crediactiva.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.config.DatabaseConfig;
import pe.crediactiva.model.Rol;
import pe.crediactiva.model.Usuario;
import pe.crediactiva.service.RolService;
import pe.crediactiva.service.UsuarioService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ResourceBundle;

public class InsertarRolDirectoController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(InsertarRolDirectoController.class);
    
    @FXML private ComboBox<Usuario> usuarioComboBox;
    @FXML private ComboBox<Rol> rolComboBox;
    @FXML private Label usuarioInfoLabel;
    @FXML private Label rolInfoLabel;
    
    @FXML private VBox camposEspecificosContainer;
    @FXML private VBox camposAsesor;
    @FXML private VBox camposCliente;
    
    @FXML private TextField comisionField;
    @FXML private TextField metaMensualField;
    @FXML private TextField limiteCreditoField;
    @FXML private TextField scoreCrediticioField;
    
    @FXML private TextArea previewTextArea;
    
    private UsuarioService usuarioService;
    private RolService rolService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("🎯 Inicializando InsertarRolDirectoController");
        
        usuarioService = new UsuarioService();
        rolService = new RolService();
        
        configurarComboBoxes();
        cargarDatos();
        configurarListeners();
        
        logger.info("✅ InsertarRolDirectoController inicializado");
    }
    
    private void configurarComboBoxes() {
        // Configurar ComboBox de usuarios
        usuarioComboBox.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario != null ? usuario.getUsername() + " - " + usuario.getNombreCompleto() : "";
            }
            
            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });
        
        // Configurar ComboBox de roles
        rolComboBox.setConverter(new StringConverter<Rol>() {
            @Override
            public String toString(Rol rol) {
                return rol != null ? rol.getNombre() : "";
            }
            
            @Override
            public Rol fromString(String string) {
                return null;
            }
        });
    }
    
    private void cargarDatos() {
        try {
            // Cargar usuarios
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            usuarioComboBox.setItems(FXCollections.observableArrayList(usuarios));
            logger.info("📋 Cargados {} usuarios", usuarios.size());
            
            // Cargar roles
            List<Rol> roles = rolService.obtenerTodosLosRoles();
            rolComboBox.setItems(FXCollections.observableArrayList(roles));
            logger.info("🎭 Cargados {} roles", roles.size());
            
        } catch (Exception e) {
            logger.error("❌ Error al cargar datos", e);
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }
    
    private void configurarListeners() {
        // Listener para usuario seleccionado
        usuarioComboBox.setOnAction(e -> {
            Usuario usuario = usuarioComboBox.getValue();
            if (usuario != null) {
                usuarioInfoLabel.setText("ID: " + usuario.getId());
                actualizarPreview();
            }
        });
        
        // Listener para rol seleccionado
        rolComboBox.setOnAction(e -> {
            Rol rol = rolComboBox.getValue();
            if (rol != null) {
                rolInfoLabel.setText("ID: " + rol.getId());
                mostrarCamposEspecificos(rol.getNombre());
                actualizarPreview();
            }
        });
        
        // Listeners para campos específicos
        comisionField.textProperty().addListener((obs, oldVal, newVal) -> actualizarPreview());
        metaMensualField.textProperty().addListener((obs, oldVal, newVal) -> actualizarPreview());
        limiteCreditoField.textProperty().addListener((obs, oldVal, newVal) -> actualizarPreview());
        scoreCrediticioField.textProperty().addListener((obs, oldVal, newVal) -> actualizarPreview());
    }
    
    private void mostrarCamposEspecificos(String nombreRol) {
        // Ocultar todos los campos específicos
        camposEspecificosContainer.setVisible(false);
        camposAsesor.setVisible(false);
        camposCliente.setVisible(false);
        
        if ("ASESOR".equals(nombreRol)) {
            camposEspecificosContainer.setVisible(true);
            camposAsesor.setVisible(true);
            // Valores por defecto
            if (comisionField.getText().trim().isEmpty()) {
                comisionField.setText("2.0");
            }
            if (metaMensualField.getText().trim().isEmpty()) {
                metaMensualField.setText("0.00");
            }
        } else if ("CLIENTE".equals(nombreRol)) {
            camposEspecificosContainer.setVisible(true);
            camposCliente.setVisible(true);
            // Valores por defecto
            if (limiteCreditoField.getText().trim().isEmpty()) {
                limiteCreditoField.setText("15000.00");
            }
            if (scoreCrediticioField.getText().trim().isEmpty()) {
                scoreCrediticioField.setText("650");
            }
        }
    }
    
    private void actualizarPreview() {
        Usuario usuario = usuarioComboBox.getValue();
        Rol rol = rolComboBox.getValue();
        
        if (usuario == null || rol == null) {
            previewTextArea.setText("Seleccione usuario y rol para ver el preview...");
            return;
        }
        
        StringBuilder preview = new StringBuilder();
        
        // 1. INSERT en usuarios_roles
        preview.append("-- 1. Asignar rol al usuario\n");
        preview.append("INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (")
               .append(usuario.getId()).append(", ").append(rol.getId()).append(");\n\n");
        
        // 2. INSERT específico según el rol
        if ("ASESOR".equals(rol.getNombre())) {
            String comision = comisionField.getText().trim();
            String meta = metaMensualField.getText().trim();
            
            if (!comision.isEmpty() && !meta.isEmpty()) {
                // Convertir porcentaje a decimal para el preview
                try {
                    BigDecimal comisionDecimal = new BigDecimal(comision).divide(new BigDecimal("100"));
                    preview.append("-- 2. Crear registro en tabla asesores\n");
                    preview.append("-- Comisión: ").append(comision).append("% = ").append(comisionDecimal).append(" (decimal)\n");
                    preview.append("INSERT INTO asesores (usuario_id, codigo_asesor, comision_porcentaje, meta_mensual) VALUES (")
                           .append(usuario.getId()).append(", ")
                           .append("'ASE").append(String.format("%03d", usuario.getId())).append("', ")
                           .append(comisionDecimal).append(", ")
                           .append(meta).append(");");
                } catch (NumberFormatException e) {
                    preview.append("-- ERROR: Comisión debe ser un número válido");
                }
            }
        } else if ("CLIENTE".equals(rol.getNombre())) {
            String limite = limiteCreditoField.getText().trim();
            String score = scoreCrediticioField.getText().trim();
            
            if (!limite.isEmpty() && !score.isEmpty()) {
                preview.append("-- 2. Crear registro en tabla clientes\n");
                preview.append("INSERT INTO clientes (usuario_id, codigo_cliente, limite_credito, score_crediticio) VALUES (")
                       .append(usuario.getId()).append(", ")
                       .append("'CLI").append(String.format("%03d", usuario.getId())).append("', ")
                       .append(limite).append(", ")
                       .append(score).append(");");
            }
        }
        
        previewTextArea.setText(preview.toString());
    }
    
    @FXML
    private void handleEjecutarInsert() {
        Usuario usuario = usuarioComboBox.getValue();
        Rol rol = rolComboBox.getValue();
        
        if (usuario == null || rol == null) {
            mostrarError("Debe seleccionar usuario y rol");
            return;
        }
        
        logger.info("🚀 EJECUTANDO INSERT DIRECTO - Usuario: {} (ID: {}), Rol: {} (ID: {})", 
                   usuario.getUsername(), usuario.getId(), rol.getNombre(), rol.getId());
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. INSERT en usuarios_roles
            String sqlUsuarioRol = "INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuarioRol)) {
                stmt.setInt(1, usuario.getId());
                stmt.setInt(2, rol.getId());
                int filasAfectadas1 = stmt.executeUpdate();
                logger.info("✅ INSERT usuarios_roles: {} filas afectadas", filasAfectadas1);
            }
            
            // 2. INSERT específico según el rol
            if ("ASESOR".equals(rol.getNombre())) {
                ejecutarInsertAsesor(conn, usuario);
            } else if ("CLIENTE".equals(rol.getNombre())) {
                ejecutarInsertCliente(conn, usuario);
            }
            
            conn.commit();
            logger.info("🎉 ¡INSERT DIRECTO COMPLETADO EXITOSAMENTE!");
            
            mostrarExito("✅ INSERT ejecutado exitosamente!\n\n" +
                        "Usuario: " + usuario.getUsername() + "\n" +
                        "Rol: " + rol.getNombre() + "\n" +
                        "Registros creados correctamente en la base de datos.");
            
            handleLimpiar();
            
        } catch (Exception e) {
            logger.error("💥 ERROR al ejecutar INSERT directo", e);
            mostrarError("Error al ejecutar INSERT: " + e.getMessage());
        }
    }
    
    private void ejecutarInsertAsesor(Connection conn, Usuario usuario) throws Exception {
        String comision = comisionField.getText().trim();
        String meta = metaMensualField.getText().trim();
        
        if (comision.isEmpty() || meta.isEmpty()) {
            throw new IllegalArgumentException("Comisión y meta mensual son requeridas para ASESOR");
        }
        
        // Generar código único
        String codigoAsesor = generarCodigoAsesor(conn);
        
        // Convertir porcentaje a decimal (2.0% -> 0.02)
        BigDecimal comisionDecimal = new BigDecimal(comision).divide(new BigDecimal("100"));
        
        logger.info("🔢 Conversión: {}% -> {}", comision, comisionDecimal);
        
        String sqlAsesor = "INSERT INTO asesores (usuario_id, codigo_asesor, comision_porcentaje, meta_mensual) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlAsesor)) {
            stmt.setInt(1, usuario.getId());
            stmt.setString(2, codigoAsesor);
            stmt.setBigDecimal(3, comisionDecimal);
            stmt.setBigDecimal(4, new BigDecimal(meta));
            
            int filasAfectadas = stmt.executeUpdate();
            logger.info("✅ INSERT asesores: {} filas afectadas, código: {}", filasAfectadas, codigoAsesor);
        }
    }
    
    private void ejecutarInsertCliente(Connection conn, Usuario usuario) throws Exception {
        String limite = limiteCreditoField.getText().trim();
        String score = scoreCrediticioField.getText().trim();
        
        if (limite.isEmpty() || score.isEmpty()) {
            throw new IllegalArgumentException("Límite de crédito y score crediticio son requeridos para CLIENTE");
        }
        
        // Generar código único
        String codigoCliente = generarCodigoCliente(conn);
        
        String sqlCliente = "INSERT INTO clientes (usuario_id, codigo_cliente, limite_credito, score_crediticio) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlCliente)) {
            stmt.setInt(1, usuario.getId());
            stmt.setString(2, codigoCliente);
            stmt.setBigDecimal(3, new BigDecimal(limite));
            stmt.setInt(4, Integer.parseInt(score));
            
            int filasAfectadas = stmt.executeUpdate();
            logger.info("✅ INSERT clientes: {} filas afectadas, código: {}", filasAfectadas, codigoCliente);
        }
    }
    
    private String generarCodigoAsesor(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo_asesor, 4) AS UNSIGNED)), 0) + 1 as siguiente FROM asesores WHERE codigo_asesor LIKE 'ASE%'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return String.format("ASE%03d", rs.getInt("siguiente"));
            }
        }
        return "ASE001";
    }
    
    private String generarCodigoCliente(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo_cliente, 4) AS UNSIGNED)), 0) + 1 as siguiente FROM clientes WHERE codigo_cliente LIKE 'CLI%'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return String.format("CLI%03d", rs.getInt("siguiente"));
            }
        }
        return "CLI001";
    }
    
    @FXML
    private void handleLimpiar() {
        usuarioComboBox.setValue(null);
        rolComboBox.setValue(null);
        usuarioInfoLabel.setText("ID del usuario aparecerá aquí");
        rolInfoLabel.setText("ID del rol aparecerá aquí");
        
        comisionField.clear();
        metaMensualField.clear();
        limiteCreditoField.clear();
        scoreCrediticioField.clear();
        
        camposEspecificosContainer.setVisible(false);
        previewTextArea.setText("Seleccione usuario y rol para ver el preview...");
    }
    
    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) usuarioComboBox.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error en INSERT Directo");
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    private void mostrarExito(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText("INSERT Ejecutado Correctamente");
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
}
