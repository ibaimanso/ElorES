package com.elorrieta.elores.view;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.service.AuthService;
import com.elorrieta.elores.service.PerfilService;

/**
 * Panel para visualizar el perfil del profesor
 * CU03-Consultar Perfil
 */
public class PerfilPanel extends JPanel {
    private JLabel lblFoto;
    private JLabel lblNombre;
    private JLabel lblEmail;
    private JLabel lblDni;
    private JLabel lblDireccion;
    private JLabel lblTelefono1;
    private JLabel lblTelefono2;
    private JButton btnVolver;
    private JLabel lblCargando;
    
    private AuthService authService;
    private PerfilService perfilService;
    private NavigationController navigationController;
    private Usuario perfilUsuario;

    public PerfilPanel() {
        authService = AuthService.getInstance();
        perfilService = PerfilService.getInstance();
        navigationController = NavigationController.getInstance();
        initComponents();
        cargarPerfil();
    }

    private void initComponents() {
        setLayout(null);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        
        // Título
        JLabel lblTitulo = new JLabel("Mi Perfil");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(41, 128, 185));
        lblTitulo.setBounds(50, 20, 500, 40);
        add(lblTitulo);
        
        // Foto de perfil
        lblFoto = new JLabel();
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setBorder(new LineBorder(new Color(41, 128, 185), 3, true));
        lblFoto.setBounds(225, 70, 150, 150);
        lblFoto.setBackground(Color.WHITE);
        lblFoto.setOpaque(true);
        add(lblFoto);
        
        // Label de cargando
        lblCargando = new JLabel("Cargando perfil...");
        lblCargando.setHorizontalAlignment(SwingConstants.CENTER);
        lblCargando.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblCargando.setForeground(new Color(127, 140, 141));
        lblCargando.setBounds(50, 240, 500, 30);
        add(lblCargando);
        
        // Nombre completo
        lblNombre = new JLabel("");
        lblNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNombre.setForeground(new Color(44, 62, 80));
        lblNombre.setBounds(50, 240, 500, 30);
        lblNombre.setVisible(false);
        add(lblNombre);
        
        // Email
        JLabel lblEmailLabel = new JLabel("Email:");
        lblEmailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEmailLabel.setBounds(100, 280, 150, 25);
        add(lblEmailLabel);
        
        lblEmail = new JLabel("");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblEmail.setBounds(250, 280, 300, 25);
        add(lblEmail);
        
        // DNI
        JLabel lblDniLabel = new JLabel("DNI:");
        lblDniLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDniLabel.setBounds(100, 310, 150, 25);
        add(lblDniLabel);
        
        lblDni = new JLabel("");
        lblDni.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDni.setBounds(250, 310, 300, 25);
        add(lblDni);
        
        // Dirección
        JLabel lblDireccionLabel = new JLabel("Dirección:");
        lblDireccionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDireccionLabel.setBounds(100, 340, 150, 25);
        add(lblDireccionLabel);
        
        lblDireccion = new JLabel("");
        lblDireccion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDireccion.setBounds(250, 340, 300, 25);
        add(lblDireccion);
        
        // Teléfono 1
        JLabel lblTelefono1Label = new JLabel("Teléfono:");
        lblTelefono1Label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTelefono1Label.setBounds(100, 370, 150, 25);
        add(lblTelefono1Label);
        
        lblTelefono1 = new JLabel("");
        lblTelefono1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTelefono1.setBounds(250, 370, 300, 25);
        add(lblTelefono1);
        
        // Teléfono 2
        JLabel lblTelefono2Label = new JLabel("Teléfono 2:");
        lblTelefono2Label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTelefono2Label.setBounds(100, 400, 150, 25);
        add(lblTelefono2Label);
        
        lblTelefono2 = new JLabel("");
        lblTelefono2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTelefono2.setBounds(250, 400, 300, 25);
        add(lblTelefono2);
        
        // Botón Volver
        btnVolver = new JButton("← Volver al Menú");
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setBackground(new Color(52, 73, 94));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnVolver.setBounds(200, 440, 200, 40);
        btnVolver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                volverAlMenu();
            }
        });
        add(btnVolver);
    }
    
    /**
     * Carga el perfil del usuario desde el servidor
     */
    private void cargarPerfil() {
        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                Usuario usuarioActual = authService.getUsuarioActual();
                if (usuarioActual == null) {
                    mostrarError("No hay usuario logueado");
                    return;
                }
                
                // Obtener perfil completo del servidor
                perfilUsuario = perfilService.getPerfil(usuarioActual.getId());
                
                // Actualizar UI en el hilo de eventos de Swing
                javax.swing.SwingUtilities.invokeLater(() -> {
                    mostrarDatosPerfil();
                });
                
            } catch (Exception e) {
                System.err.println("Error cargando perfil: " + e.getMessage());
                e.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    mostrarError("Error al cargar el perfil: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Muestra los datos del perfil en la UI
     */
    private void mostrarDatosPerfil() {
        lblCargando.setVisible(false);
        
        // Mostrar datos
        lblNombre.setText(perfilUsuario.getNombreCompleto());
        lblNombre.setVisible(true);
        
        lblEmail.setText(perfilUsuario.getEmail() != null ? perfilUsuario.getEmail() : "No especificado");
        lblDni.setText(perfilUsuario.getDni() != null && !perfilUsuario.getDni().isEmpty() ? 
                      perfilUsuario.getDni() : "No especificado");
        lblDireccion.setText(perfilUsuario.getDireccion() != null && !perfilUsuario.getDireccion().isEmpty() ? 
                            perfilUsuario.getDireccion() : "No especificada");
        lblTelefono1.setText(perfilUsuario.getTelefono1() != null && !perfilUsuario.getTelefono1().isEmpty() ? 
                            perfilUsuario.getTelefono1() : "No especificado");
        lblTelefono2.setText(perfilUsuario.getTelefono2() != null && !perfilUsuario.getTelefono2().isEmpty() ? 
                            perfilUsuario.getTelefono2() : "No especificado");
        
        // Cargar foto si existe
        cargarFotoPerfil();
    }
    
    /**
     * Carga la foto de perfil desde la URL
     */
    private void cargarFotoPerfil() {
        String argazkiaUrl = perfilUsuario.getArgazkiaUrl();
        
        if (argazkiaUrl != null && !argazkiaUrl.isEmpty()) {
            try {
                URL url = new URL(argazkiaUrl);
                ImageIcon iconoOriginal = new ImageIcon(url);
                
                // Redimensionar imagen para que quepa en el label
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(
                    140, 140, Image.SCALE_SMOOTH);
                
                lblFoto.setIcon(new ImageIcon(imagenEscalada));
                lblFoto.setText("");
                
            } catch (Exception e) {
                System.err.println("Error cargando imagen de perfil: " + e.getMessage());
                mostrarIconoUsuarioPorDefecto();
            }
        } else {
            mostrarIconoUsuarioPorDefecto();
        }
    }
    
    /**
     * Muestra un ícono por defecto cuando no hay foto
     */
    private void mostrarIconoUsuarioPorDefecto() {
        lblFoto.setText("👤");
        lblFoto.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        lblFoto.setForeground(new Color(149, 165, 166));
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblCargando.setText("Error: " + mensaje);
        lblCargando.setForeground(new Color(231, 76, 60));
        
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Vuelve al menú principal
     */
    private void volverAlMenu() {
        navigationController.navigateTo(new MenuPanel());
    }
}
