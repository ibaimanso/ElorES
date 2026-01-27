package com.elorrieta.elores.view;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.service.AuthService;

/**
 * Panel de menú principal para profesores
 */
public class MenuPanel extends JPanel {
    private JLabel lblWelcome;
    private JButton btnHorario;
    private JButton btnReuniones;
    private JButton btnAlumnos;
    private JButton btnPerfil;
    private JButton btnCerrarSesion;
    
    private AuthService authService;
    private NavigationController navigationController;

    public MenuPanel() {
        authService = AuthService.getInstance();
        navigationController = NavigationController.getInstance();
        initComponents();
    }

    private void initComponents() {
        setLayout(null);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        
        // Mensaje de bienvenida
        Usuario usuario = authService.getUsuarioActual();
        lblWelcome = new JLabel("Bienvenido/a, " + usuario.getNombreCompleto());
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(41, 128, 185));
        lblWelcome.setBounds(50, 30, 500, 40);
        add(lblWelcome);
        
        // Panel contenedor de botones
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridLayout(5, 1, 0, 15));
        panelButtons.setBackground(new Color(245, 245, 245));
        panelButtons.setBounds(150, 100, 300, 350);
        add(panelButtons);
        
        // Botón Horario
        btnHorario = createMenuButton("📅 Mi Horario");
        btnHorario.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MenuPanel.this, 
                    "Funcionalidad de Horario en desarrollo", 
                    "Información", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelButtons.add(btnHorario);
        
        // Botón Reuniones
        btnReuniones = createMenuButton("📋 Reuniones");
        btnReuniones.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MenuPanel.this, 
                    "Funcionalidad de Reuniones en desarrollo", 
                    "Información", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panelButtons.add(btnReuniones);
        
        // Botón Alumnos
        btnAlumnos = createMenuButton("👥 Gestión de Alumnos");
        btnAlumnos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Navegar al panel de alumnos
                AlumnosPanel alumnosPanel = new AlumnosPanel();
                navigationController.navigateTo(alumnosPanel);
            }
        });
        panelButtons.add(btnAlumnos);
        
        // Botón Perfil
        btnPerfil = createMenuButton("👤 Mi Perfil");
        btnPerfil.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Navegar al panel de perfil
                PerfilPanel perfilPanel = new PerfilPanel();
                navigationController.navigateTo(perfilPanel);
            }
        });
        panelButtons.add(btnPerfil);
        
        // Botón Cerrar Sesión
        btnCerrarSesion = createMenuButton("🚪 Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(231, 76, 60));
        btnCerrarSesion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
        panelButtons.add(btnCerrarSesion);
    }
    
    /**
     * Crea un botón de menú con estilo consistente
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
        return button;
    }
    
    /**
     * Maneja el cierre de sesión
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                authService.logout();
                navigationController.navigateTo(new LoginPanel());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error al cerrar sesión: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}