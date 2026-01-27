package com.elorrieta.elores.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.net.URL;
import javax.imageio.ImageIO;

import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.model.Alumno;
import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.service.AlumnoService;
import com.elorrieta.elores.service.AuthService;

/**
 * Panel para consultar y gestionar alumnos (CU04-Consultar Alumnos)
 * Permite al profesor ver sus alumnos, filtrarlos y consultar sus perfiles
 */
public class AlumnosPanel extends JPanel {
    private static final int PANEL_WIDTH = 600;
    private static final int PANEL_HEIGHT = 500;
    
    private AlumnoService alumnoService;
    private AuthService authService;
    private NavigationController navigationController;
    
    private List<Alumno> todosLosAlumnos;
    private List<Alumno> alumnosFiltrados;
    
    // Componentes UI
    private JLabel lblTitulo;
    private JComboBox<String> cboCiclo;
    private JComboBox<String> cboCurso;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltros;
    private JList<Alumno> listAlumnos;
    private DefaultListModel<Alumno> listModel;
    private JScrollPane scrollAlumnos;
    
    // Panel de detalles del alumno
    private JPanel panelDetalles;
    private JLabel lblFoto;
    private JLabel lblNombreDetalle;
    private JLabel lblApellidosDetalle;
    private JLabel lblEmailDetalle;
    private JLabel lblCicloDetalle;
    private JLabel lblCursoDetalle;
    
    private JButton btnVolver;

    public AlumnosPanel() {
        alumnoService = AlumnoService.getInstance();
        authService = AuthService.getInstance();
        navigationController = NavigationController.getInstance();
        
        todosLosAlumnos = new ArrayList<>();
        alumnosFiltrados = new ArrayList<>();
        
        initComponents();
        cargarAlumnos();
    }

    private void initComponents() {
        setLayout(null);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 245, 245));
        
        // Título
        lblTitulo = new JLabel("Gestión de Alumnos");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(41, 128, 185));
        lblTitulo.setBounds(20, 10, 560, 30);
        add(lblTitulo);
        
        // Panel de filtros
        JPanel panelFiltros = new JPanel();
        panelFiltros.setLayout(null);
        panelFiltros.setBorder(new TitledBorder("Filtros"));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBounds(20, 50, 560, 80);
        add(panelFiltros);
        
        // ComboBox Ciclo
        JLabel lblCiclo = new JLabel("Ciclo:");
        lblCiclo.setBounds(10, 20, 60, 25);
        panelFiltros.add(lblCiclo);
        
        cboCiclo = new JComboBox<>();
        cboCiclo.addItem("Todos");
        cboCiclo.addItem("DAM");
        cboCiclo.addItem("DAW");
        cboCiclo.addItem("ASIR");
        cboCiclo.setBounds(70, 20, 120, 25);
        panelFiltros.add(cboCiclo);
        
        // ComboBox Curso
        JLabel lblCurso = new JLabel("Curso:");
        lblCurso.setBounds(210, 20, 60, 25);
        panelFiltros.add(lblCurso);
        
        cboCurso = new JComboBox<>();
        cboCurso.addItem("Todos");
        cboCurso.addItem("1º");
        cboCurso.addItem("2º");
        cboCurso.setBounds(270, 20, 100, 25);
        panelFiltros.add(cboCurso);
        
        // Botón Filtrar
        btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setBounds(390, 20, 80, 25);
        btnFiltrar.setBackground(new Color(41, 128, 185));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        panelFiltros.add(btnFiltrar);
        
        // Botón Limpiar Filtros
        btnLimpiarFiltros = new JButton("Limpiar");
        btnLimpiarFiltros.setBounds(475, 20, 75, 25);
        btnLimpiarFiltros.setBackground(new Color(149, 165, 166));
        btnLimpiarFiltros.setForeground(Color.WHITE);
        btnLimpiarFiltros.setFocusPainted(false);
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());
        panelFiltros.add(btnLimpiarFiltros);
        
        // Lista de alumnos
        listModel = new DefaultListModel<>();
        listAlumnos = new JList<>(listModel);
        listAlumnos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAlumnos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listAlumnos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetallesAlumno();
            }
        });
        
        scrollAlumnos = new JScrollPane(listAlumnos);
        scrollAlumnos.setBounds(20, 140, 250, 280);
        scrollAlumnos.setBorder(new TitledBorder("Alumnos"));
        add(scrollAlumnos);
        
        // Panel de detalles
        panelDetalles = new JPanel();
        panelDetalles.setLayout(null);
        panelDetalles.setBorder(new TitledBorder("Detalles del Alumno"));
        panelDetalles.setBackground(Color.WHITE);
        panelDetalles.setBounds(280, 140, 300, 280);
        add(panelDetalles);
        
        // Foto del alumno
        lblFoto = new JLabel();
        lblFoto.setBounds(75, 20, 150, 150);
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        lblFoto.setBackground(new Color(230, 230, 230));
        lblFoto.setOpaque(true);
        lblFoto.setText("Sin foto");
        panelDetalles.add(lblFoto);
        
        // Nombre
        lblNombreDetalle = new JLabel("Nombre: -");
        lblNombreDetalle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNombreDetalle.setBounds(10, 180, 280, 20);
        panelDetalles.add(lblNombreDetalle);
        
        // Apellidos
        lblApellidosDetalle = new JLabel("Apellidos: -");
        lblApellidosDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblApellidosDetalle.setBounds(10, 205, 280, 20);
        panelDetalles.add(lblApellidosDetalle);
        
        // Email
        lblEmailDetalle = new JLabel("Email: -");
        lblEmailDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEmailDetalle.setBounds(10, 230, 280, 20);
        panelDetalles.add(lblEmailDetalle);
        
        // Ciclo
        lblCicloDetalle = new JLabel("Ciclo: -");
        lblCicloDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCicloDetalle.setBounds(10, 250, 140, 20);
        panelDetalles.add(lblCicloDetalle);
        
        // Curso
        lblCursoDetalle = new JLabel("Curso: -");
        lblCursoDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCursoDetalle.setBounds(150, 250, 140, 20);
        panelDetalles.add(lblCursoDetalle);
        
        // Botón Volver
        btnVolver = new JButton("← Volver al Menú");
        btnVolver.setBounds(20, 430, 150, 35);
        btnVolver.setBackground(new Color(149, 165, 166));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> volverAlMenu());
        add(btnVolver);
    }

    /**
     * Carga la lista de alumnos desde el servidor
     */
    private void cargarAlumnos() {
        // Mostrar mensaje de carga
        listModel.clear();
        listModel.addElement(null); // Placeholder
        
        SwingWorker<List<Alumno>, Void> worker = new SwingWorker<List<Alumno>, Void>() {
            @Override
            protected List<Alumno> doInBackground() throws Exception {
                Usuario profesor = authService.getUsuarioActual();
                if (profesor == null) {
                    throw new Exception("No hay usuario autenticado");
                }
                return alumnoService.getAlumnos(profesor.getId());
            }

            @Override
            protected void done() {
                try {
                    todosLosAlumnos = get();
                    alumnosFiltrados = new ArrayList<>(todosLosAlumnos);
                    actualizarListaAlumnos();
                    
                    if (todosLosAlumnos.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            AlumnosPanel.this,
                            "No se encontraron alumnos asociados a su cuenta",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    listModel.clear();
                    JOptionPane.showMessageDialog(
                        AlumnosPanel.this,
                        "Error al cargar alumnos: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }

    /**
     * Aplica los filtros seleccionados
     */
    private void aplicarFiltros() {
        String cicloSeleccionado = (String) cboCiclo.getSelectedItem();
        String cursoSeleccionado = (String) cboCurso.getSelectedItem();
        
        alumnosFiltrados = todosLosAlumnos.stream()
            .filter(alumno -> {
                boolean pasaCiclo = "Todos".equals(cicloSeleccionado) || 
                                   cicloSeleccionado.equalsIgnoreCase(alumno.getCiclo());
                boolean pasaCurso = "Todos".equals(cursoSeleccionado) || 
                                   cursoSeleccionado.equalsIgnoreCase(alumno.getCurso());
                return pasaCiclo && pasaCurso;
            })
            .collect(Collectors.toList());
        
        actualizarListaAlumnos();
        
        if (alumnosFiltrados.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No se encontraron alumnos con los filtros aplicados",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Limpia los filtros y muestra todos los alumnos
     */
    private void limpiarFiltros() {
        cboCiclo.setSelectedIndex(0);
        cboCurso.setSelectedIndex(0);
        alumnosFiltrados = new ArrayList<>(todosLosAlumnos);
        actualizarListaAlumnos();
    }

    /**
     * Actualiza la lista visual de alumnos
     */
    private void actualizarListaAlumnos() {
        listModel.clear();
        for (Alumno alumno : alumnosFiltrados) {
            listModel.addElement(alumno);
        }
        limpiarDetalles();
    }

    /**
     * Muestra los detalles del alumno seleccionado
     */
    private void mostrarDetallesAlumno() {
        Alumno alumnoSeleccionado = listAlumnos.getSelectedValue();
        
        if (alumnoSeleccionado == null) {
            limpiarDetalles();
            return;
        }
        
        // Validar que el alumno existe
        if (alumnoSeleccionado.getId() <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Alumno no válido o no existe",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            limpiarDetalles();
            return;
        }
        
        // Actualizar etiquetas con la información
        lblNombreDetalle.setText("Nombre: " + (alumnoSeleccionado.getNombre() != null ? alumnoSeleccionado.getNombre() : "-"));
        lblApellidosDetalle.setText("Apellidos: " + (alumnoSeleccionado.getApellidos() != null ? alumnoSeleccionado.getApellidos() : "-"));
        lblEmailDetalle.setText("Email: " + (alumnoSeleccionado.getEmail() != null ? alumnoSeleccionado.getEmail() : "-"));
        lblCicloDetalle.setText("Ciclo: " + (alumnoSeleccionado.getCiclo() != null ? alumnoSeleccionado.getCiclo() : "-"));
        lblCursoDetalle.setText("Curso: " + (alumnoSeleccionado.getCurso() != null ? alumnoSeleccionado.getCurso() : "-"));
        
        // Cargar foto si existe
        cargarFotoAlumno(alumnoSeleccionado);
    }

    /**
     * Carga la foto del alumno si está disponible
     */
    private void cargarFotoAlumno(Alumno alumno) {
        String fotoUrl = alumno.getArgazkiaUrl();
        
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    try {
                        URL url = new URL(fotoUrl);
                        Image img = ImageIO.read(url);
                        Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    } catch (Exception e) {
                        System.err.println("Error cargando foto: " + e.getMessage());
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            lblFoto.setIcon(icon);
                            lblFoto.setText("");
                        } else {
                            lblFoto.setIcon(null);
                            lblFoto.setText("Sin foto");
                        }
                    } catch (Exception e) {
                        lblFoto.setIcon(null);
                        lblFoto.setText("Error al cargar");
                    }
                }
            };
            
            worker.execute();
        } else {
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
        }
    }

    /**
     * Limpia el panel de detalles
     */
    private void limpiarDetalles() {
        lblNombreDetalle.setText("Nombre: -");
        lblApellidosDetalle.setText("Apellidos: -");
        lblEmailDetalle.setText("Email: -");
        lblCicloDetalle.setText("Ciclo: -");
        lblCursoDetalle.setText("Curso: -");
        lblFoto.setIcon(null);
        lblFoto.setText("Sin foto");
    }

    /**
     * Vuelve al menú principal
     */
    private void volverAlMenu() {
        navigationController.navigateTo(new MenuPanel());
    }
}
