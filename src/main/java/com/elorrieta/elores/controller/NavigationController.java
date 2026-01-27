package com.elorrieta.elores.controller;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Controlador de navegación entre paneles
 * Gestiona el cambio de vistas en el JFrame principal
 */
public class NavigationController {
    private static NavigationController instance;
    private JFrame mainFrame;
    
    private NavigationController() {}
    
    public static synchronized NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }
    
    /**
     * Establece el frame principal de la aplicación
     */
    public void setMainFrame(JFrame frame) {
        this.mainFrame = frame;
    }
    
    /**
     * Navega a un nuevo panel, reemplazando el contenido actual
     */
    public void navigateTo(JPanel panel) {
        if (mainFrame != null) {
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(panel);
            mainFrame.revalidate();
            mainFrame.setResizable(false);
            mainFrame.repaint();
            mainFrame.setSize(600, 600);
        }
    }
    
    /**
     * Obtiene el frame principal
     */
    public JFrame getMainFrame() {
        return mainFrame;
    }
}
