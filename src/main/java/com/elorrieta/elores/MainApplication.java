package com.elorrieta.elores;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.elorrieta.elores.config.AppConfig;
import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.network.SocketClient;
import com.elorrieta.elores.view.LoginPanel;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Clase principal de la aplicación ElorES
 * Framework Educativo Elorrieta - Aplicación de Escritorio
 */
public class MainApplication {
    private static final String APP_TITLE = "ElorES - Framework Educativo Elorrieta";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 500;

    public static void main(String[] args) {
        // Establecer Look and Feel moderno
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("No se pudo cargar FlatLaf, usando Look and Feel por defecto");
        }
        
        // Configurar cliente de sockets desde archivo de configuración
        AppConfig config = AppConfig.getInstance();
        SocketClient.getInstance().configure(config.getServerHost(), config.getServerPort());
        
        System.out.println("=== ElorES - Framework Educativo Elorrieta ===");
        System.out.println("Configuración del servidor: " + config.getServerHost() + ":" + config.getServerPort());
        
        // Iniciar la aplicación en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }
    
    
    /**
     * Crea y muestra la interfaz gráfica principal
     */
    private static void createAndShowGUI() {
        // Crear el frame principal
        JFrame mainFrame = new JFrame(APP_TITLE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        mainFrame.setLocationRelativeTo(null); // Centrar en pantalla
        mainFrame.setResizable(false);
        
        // Registrar el frame en el controlador de navegación
        NavigationController navigationController = NavigationController.getInstance();
        navigationController.setMainFrame(mainFrame);
        
        // Cargar el panel de login inicial
        LoginPanel loginPanel = new LoginPanel();
        mainFrame.add(loginPanel);
        
        // Agregar hook de cierre para limpiar recursos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SocketClient.getInstance().disconnect();
        }));
        
        // Mostrar la ventana
        mainFrame.setVisible(true);
    }
}