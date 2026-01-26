package com.elorrieta.elores.view;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.service.AuthService;

/**
 * Panel de login con autenticación segura
 */
public class LoginPanel extends JPanel {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblTitle;
    private JLabel lblEmail;
    private JLabel lblPassword;
    private JLabel lblStatus;
    
    private AuthService authService;
    private NavigationController navigationController;

    public LoginPanel() {
        authService = AuthService.getInstance();
        navigationController = NavigationController.getInstance();
        initComponents();
    }

    private void initComponents() {
        setLayout(null);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        
        // Título
        lblTitle = new JLabel("Framework Educativo Elorrieta");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185));
        lblTitle.setBounds(100, 50, 400, 40);
        add(lblTitle);
        
        // Label email
        lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblEmail.setBounds(150, 130, 80, 25);
        add(lblEmail);
        
        // Campo email
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setBounds(150, 160, 300, 35);
        add(txtEmail);
        txtEmail.setColumns(10);
        
        // Label contraseña
        lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPassword.setBounds(150, 210, 100, 25);
        add(lblPassword);
        
        // Campo contraseña
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBounds(150, 240, 300, 35);
        add(txtPassword);
        
        // Botón login
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBounds(200, 300, 200, 40);
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        add(btnLogin);
        
        // Label de estado
        lblStatus = new JLabel("");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(Color.RED);
        lblStatus.setBounds(100, 350, 400, 25);
        add(lblStatus);
        
        // Enter en password ejecuta login
        txtPassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }
    
    /**
     * Maneja el proceso de login
     */
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validar campos
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, complete todos los campos");
            return;
        }
        
        // Deshabilitar botón durante el proceso
        btnLogin.setEnabled(false);
        lblStatus.setText("Conectando...");
        lblStatus.setForeground(new Color(41, 128, 185));
        
        // Ejecutar login en hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                Usuario usuario = authService.login(email, password);
                
                // Verificar que es profesor
                if (!usuario.esProfesor()) {
                    showError("Acceso denegado. Solo profesores pueden usar esta aplicación.");
                    authService.logout();
                    return;
                }
                
                // Login exitoso, navegar al menú
                javax.swing.SwingUtilities.invokeLater(() -> {
                    navigationController.navigateTo(new MenuPanel());
                });
                
            } catch (Exception ex) {
                showError("Error de autenticación: " + ex.getMessage());
            } finally {
                // Rehabilitar botón
                javax.swing.SwingUtilities.invokeLater(() -> {
                    btnLogin.setEnabled(true);
                    lblStatus.setText("");
                });
            }
        }).start();
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showError(String message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            lblStatus.setText(message);
            lblStatus.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
