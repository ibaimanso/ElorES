package com.elorrieta.elores.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.model.Alumno;
import com.elorrieta.elores.model.Reunion;
import com.elorrieta.elores.service.AlumnoService;
import com.elorrieta.elores.service.AuthService;
import com.elorrieta.elores.service.ReunionService;

/**
 * Panel para gestionar reuniones (CU07)
 * Permite crear, consultar, modificar y eliminar reuniones
 * Las reuniones se notifican por correo y aparecen en el horario
 */
public class ReunionesPanel extends JPanel {
    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private static final Color COLOR_DANGER = new Color(231, 76, 60);
    private static final Color COLOR_WARNING = new Color(243, 156, 18);
    private static final Color COLOR_BG = new Color(245, 245, 245);
    
    private ReunionService reunionService;
    private AlumnoService alumnoService;
    private AuthService authService;
    private NavigationController navigationController;
    
    // Componentes de la interfaz
    private JButton btnVolver;
    private JButton btnNuevaReunion;
    private JButton btnActualizar;
    private JTable tableReuniones;
    private DefaultTableModel tableModel;
    private JPanel panelDetalles;
    
    // Componentes de detalles
    private JLabel lblDetalleId;
    private JLabel lblDetalleTitulo;
    private JLabel lblDetalleAlumno;
    private JLabel lblDetalleFecha;
    private JLabel lblDetalleAula;
    private JTextArea txtDetalleAsunto;
    private JComboBox<String> cmbEstado;
    private JButton btnGuardarEstado;
    private JButton btnEliminar;
    private JButton btnVerMapa;
    
    private Reunion reunionSeleccionada;
    private List<Alumno> listaAlumnos;

    public ReunionesPanel() {
        reunionService = ReunionService.getInstance();
        alumnoService = AlumnoService.getInstance();
        authService = AuthService.getInstance();
        navigationController = NavigationController.getInstance();
        
        initComponents();
        cargarReuniones();
        cargarAlumnos();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel superior con título y botones
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central con split: tabla y detalles
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);
        
        // Panel izquierdo: Tabla de reuniones
        JPanel panelTabla = crearPanelTabla();
        splitPane.setLeftComponent(panelTabla);
        
        // Panel derecho: Detalles de reunión
        panelDetalles = crearPanelDetalles();
        splitPane.setRightComponent(panelDetalles);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        
        // Título
        JLabel lblTitulo = new JLabel("📅 Gestión de Reuniones");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(COLOR_PRIMARY);
        panel.add(lblTitulo, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.setBackground(COLOR_BG);
        
        btnVolver = crearBoton("← Volver", COLOR_PRIMARY);
        btnVolver.addActionListener(e -> {
            MenuPanel menuPanel = new MenuPanel();
            navigationController.navigateTo(menuPanel);
        });
        panelBotones.add(btnVolver);
        
        panel.add(panelBotones, BorderLayout.WEST);
        
        // Botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelAcciones.setBackground(COLOR_BG);
        
        btnNuevaReunion = crearBoton("➕ Nueva Reunión", COLOR_SUCCESS);
        btnNuevaReunion.addActionListener(e -> mostrarDialogoNuevaReunion());
        panelAcciones.add(btnNuevaReunion);
        
        btnActualizar = crearBoton("🔄 Actualizar", COLOR_PRIMARY);
        btnActualizar.addActionListener(e -> cargarReuniones());
        panelAcciones.add(btnActualizar);
        
        panel.add(panelAcciones, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Reuniones Programadas",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        // Crear tabla
        String[] columnas = {"ID", "Título", "Alumno", "Fecha", "Hora", "Aula", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableReuniones = new JTable(tableModel);
        tableReuniones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableReuniones.getTableHeader().setReorderingAllowed(false);
        tableReuniones.setRowHeight(25);
        tableReuniones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetallesReunionSeleccionada();
            }
        });
        
        // Ocultar columna ID
        tableReuniones.getColumnModel().getColumn(0).setMinWidth(0);
        tableReuniones.getColumnModel().getColumn(0).setMaxWidth(0);
        tableReuniones.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(tableReuniones);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelDetalles() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Detalles de la Reunión",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        // Mensaje cuando no hay selección
        JLabel lblMensaje = new JLabel("Selecciona una reunión para ver sus detalles");
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMensaje.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblMensaje.setForeground(Color.GRAY);
        panel.add(Box.createVerticalStrut(50));
        panel.add(lblMensaje);
        
        // Crear componentes de detalles (inicialmente ocultos)
        lblDetalleId = new JLabel();
        lblDetalleTitulo = crearLabelDetalle("Título:");
        lblDetalleAlumno = crearLabelDetalle("Alumno:");
        lblDetalleFecha = crearLabelDetalle("Fecha:");
        lblDetalleAula = crearLabelDetalle("Aula:");
        
        JPanel panelAsunto = new JPanel(new BorderLayout(5, 5));
        panelAsunto.setBackground(Color.WHITE);
        panelAsunto.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblAsuntoLabel = new JLabel("Asunto:");
        lblAsuntoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtDetalleAsunto = new JTextArea(4, 20);
        txtDetalleAsunto.setEditable(false);
        txtDetalleAsunto.setLineWrap(true);
        txtDetalleAsunto.setWrapStyleWord(true);
        txtDetalleAsunto.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollAsunto = new JScrollPane(txtDetalleAsunto);
        panelAsunto.add(lblAsuntoLabel, BorderLayout.NORTH);
        panelAsunto.add(scrollAsunto, BorderLayout.CENTER);
        
        // Panel de estado
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstado.setBackground(Color.WHITE);
        panelEstado.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbEstado = new JComboBox<>(new String[]{"pendiente", "aceptada", "denegada", "cancelada"});
        btnGuardarEstado = crearBoton("Guardar Estado", COLOR_SUCCESS);
        btnGuardarEstado.addActionListener(e -> actualizarEstadoReunion());
        panelEstado.add(lblEstado);
        panelEstado.add(cmbEstado);
        panelEstado.add(btnGuardarEstado);
        
        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setBackground(Color.WHITE);
        
        btnVerMapa = crearBoton("🗺️ Ver en Mapa", COLOR_PRIMARY);
        btnVerMapa.addActionListener(e -> mostrarMapaAula());
        panelBotones.add(btnVerMapa);
        
        btnEliminar = crearBoton("🗑️ Eliminar", COLOR_DANGER);
        btnEliminar.addActionListener(e -> eliminarReunion());
        panelBotones.add(btnEliminar);
        
        return panel;
    }
    
    private JLabel crearLabelDetalle(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setBorder(new EmptyBorder(5, 10, 5, 10));
        return label;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton button = new JButton(texto);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        return button;
    }
    
    private void cargarReuniones() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Reunion> reuniones;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    reuniones = reunionService.getReunionesProfesor();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    System.err.println("Error cargando reuniones: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "Error al cargar reuniones:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Limpiar tabla
                tableModel.setRowCount(0);
                
                if (reuniones != null && !reuniones.isEmpty()) {
                    for (Reunion reunion : reuniones) {
                        String alumnoNombre = obtenerNombreAlumno(reunion.getAlumnoId());
                        String fechaStr = reunion.getFecha() != null 
                            ? reunion.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : reunion.getDia();
                        String horaStr = reunion.getHora() > 0 
                            ? String.valueOf(reunion.getHora())
                            : reunion.getFecha() != null 
                                ? reunion.getFecha().format(DateTimeFormatter.ofPattern("HH:mm"))
                                : "-";
                        
                        tableModel.addRow(new Object[]{
                            reunion.getIdReunion(),
                            reunion.getTitulo() != null ? reunion.getTitulo() : "Sin título",
                            alumnoNombre,
                            fechaStr,
                            horaStr,
                            reunion.getAula() != null ? reunion.getAula() : "-",
                            reunion.getEstado()
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "No hay reuniones programadas.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void cargarAlumnos() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    listaAlumnos = alumnoService.getAlumnos(authService.getUsuarioActual().getId());
                } catch (Exception e) {
                    System.err.println("Error cargando alumnos: " + e.getMessage());
                }
                return null;
            }
        };
        
        worker.execute();
    }
    
    private String obtenerNombreAlumno(Integer alumnoId) {
        if (alumnoId == null || listaAlumnos == null) {
            return "Alumno desconocido";
        }
        
        for (Alumno alumno : listaAlumnos) {
            if (alumno.getId() == alumnoId) {
                return alumno.getNombre() + " " + (alumno.getApellidos() != null ? alumno.getApellidos() : "");
            }
        }
        
        return "Alumno ID: " + alumnoId;
    }
    
    private void mostrarDetallesReunionSeleccionada() {
        int selectedRow = tableReuniones.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        int reunionId = (int) tableModel.getValueAt(selectedRow, 0);
        
        // Buscar la reunión en la lista
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Reunion> reuniones;
            
            @Override
            protected Void doInBackground() throws Exception {
                reuniones = reunionService.getReunionesProfesor();
                return null;
            }
            
            @Override
            protected void done() {
                if (reuniones != null) {
                    for (Reunion reunion : reuniones) {
                        if (reunion.getIdReunion() == reunionId) {
                            reunionSeleccionada = reunion;
                            actualizarPanelDetalles();
                            break;
                        }
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    private void actualizarPanelDetalles() {
        if (reunionSeleccionada == null) {
            return;
        }
        
        // Reconstruir panel de detalles con información
        panelDetalles.removeAll();
        panelDetalles.setLayout(new BoxLayout(panelDetalles, BoxLayout.Y_AXIS));
        
        // Agregar información
        panelDetalles.add(Box.createVerticalStrut(10));
        
        JLabel lblTitulo = new JLabel("<html><b>Título:</b> " + 
            (reunionSeleccionada.getTitulo() != null ? reunionSeleccionada.getTitulo() : "Sin título") + "</html>");
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(5, 10, 5, 10));
        panelDetalles.add(lblTitulo);
        
        JLabel lblAlumno = new JLabel("<html><b>Alumno:</b> " + 
            obtenerNombreAlumno(reunionSeleccionada.getAlumnoId()) + "</html>");
        lblAlumno.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAlumno.setBorder(new EmptyBorder(5, 10, 5, 10));
        panelDetalles.add(lblAlumno);
        
        String fechaStr = reunionSeleccionada.getFecha() != null 
            ? reunionSeleccionada.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            : reunionSeleccionada.getDia() + " - Hora " + reunionSeleccionada.getHora();
        JLabel lblFecha = new JLabel("<html><b>Fecha:</b> " + fechaStr + "</html>");
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblFecha.setBorder(new EmptyBorder(5, 10, 5, 10));
        panelDetalles.add(lblFecha);
        
        JLabel lblAula = new JLabel("<html><b>Aula:</b> " + 
            (reunionSeleccionada.getAula() != null ? reunionSeleccionada.getAula() : "No especificada") + "</html>");
        lblAula.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAula.setBorder(new EmptyBorder(5, 10, 5, 10));
        panelDetalles.add(lblAula);
        
        // Tema/Asunto
        JPanel panelAsunto = new JPanel(new BorderLayout(5, 5));
        panelAsunto.setBackground(Color.WHITE);
        panelAsunto.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelAsunto.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblAsuntoLabel = new JLabel("Tema:");
        lblAsuntoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextArea txtAsunto = new JTextArea(4, 20);
        txtAsunto.setText(reunionSeleccionada.getAsunto() != null ? reunionSeleccionada.getAsunto() : "Sin tema especificado");
        txtAsunto.setEditable(false);
        txtAsunto.setLineWrap(true);
        txtAsunto.setWrapStyleWord(true);
        txtAsunto.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollAsunto = new JScrollPane(txtAsunto);
        panelAsunto.add(lblAsuntoLabel, BorderLayout.NORTH);
        panelAsunto.add(scrollAsunto, BorderLayout.CENTER);
        panelDetalles.add(panelAsunto);
        
        // Estado con descripción
        JPanel panelEstado = new JPanel(new GridBagLayout());
        panelEstado.setBackground(Color.WHITE);
        panelEstado.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 10);
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelEstado.add(lblEstado, gbc);
        
        gbc.gridx = 1;
        cmbEstado = new JComboBox<>(new String[]{"pendiente", "aceptada", "denegada", "cancelada"});
        cmbEstado.setSelectedItem(reunionSeleccionada.getEstado());
        panelEstado.add(cmbEstado, gbc);
        
        gbc.gridx = 2;
        btnGuardarEstado = crearBoton("Actualizar", COLOR_SUCCESS);
        btnGuardarEstado.setPreferredSize(new Dimension(120, 30));
        btnGuardarEstado.addActionListener(e -> actualizarEstadoReunion());
        panelEstado.add(btnGuardarEstado, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        JLabel lblEstadoInfo = new JLabel("<html><i>Al cambiar el estado se enviará notificación por correo</i></html>");
        lblEstadoInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblEstadoInfo.setForeground(Color.GRAY);
        panelEstado.add(lblEstadoInfo, gbc);
        
        panelDetalles.add(panelEstado);
        
        panelDetalles.add(Box.createVerticalStrut(10));
        
        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnVerMapa = crearBoton("🗺️ Ver Ubicación", COLOR_PRIMARY);
        btnVerMapa.addActionListener(e -> mostrarMapaAula());
        panelBotones.add(btnVerMapa);
        
        btnEliminar = crearBoton("🗑️ Eliminar", COLOR_DANGER);
        btnEliminar.addActionListener(e -> eliminarReunion());
        panelBotones.add(btnEliminar);
        
        panelDetalles.add(panelBotones);
        panelDetalles.add(Box.createVerticalGlue());
        
        panelDetalles.revalidate();
        panelDetalles.repaint();
    }
    
    private void mostrarDialogoNuevaReunion() {
        if (listaAlumnos == null || listaAlumnos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay alumnos disponibles para convocar reunión.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Reunión", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Alumno
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Alumno:*"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cmbAlumno = new JComboBox<>();
        for (Alumno alumno : listaAlumnos) {
            cmbAlumno.addItem(alumno.getNombre() + " " + alumno.getApellidos());
        }
        panelFormulario.add(cmbAlumno, gbc);
        
        // Título
        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Título:*"), gbc);
        gbc.gridx = 1;
        JTextField txtTitulo = new JTextField(20);
        panelFormulario.add(txtTitulo, gbc);
        
        // Día
        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Día:*"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cmbDia = new JComboBox<>(new String[]{
            "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"
        });
        panelFormulario.add(cmbDia, gbc);
        
        // Hora (solo horas lectivas 1-6)
        gbc.gridx = 0; gbc.gridy = 3;
        panelFormulario.add(new JLabel("Hora:*"), gbc);
        gbc.gridx = 1;
        JSpinner spinnerHora = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        JPanel panelHora = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelHora.add(spinnerHora);
        JLabel lblHoraInfo = new JLabel("(Horas lectivas: 1-6)");
        lblHoraInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblHoraInfo.setForeground(Color.GRAY);
        panelHora.add(lblHoraInfo);
        panelFormulario.add(panelHora, gbc);
        
        // Aula
        gbc.gridx = 0; gbc.gridy = 4;
        panelFormulario.add(new JLabel("Aula:*"), gbc);
        gbc.gridx = 1;
        JTextField txtAula = new JTextField(20);
        txtAula.setText("5.005"); // Valor predeterminado
        panelFormulario.add(txtAula, gbc);
        
        // Ubicación (Centro educativo)
        gbc.gridx = 0; gbc.gridy = 5;
        panelFormulario.add(new JLabel("Ubicación:"), gbc);
        gbc.gridx = 1;
        JTextField txtUbicacion = new JTextField(20);
        txtUbicacion.setText("CIFP Elorrieta-Errekamari LHII"); // Valor predeterminado
        txtUbicacion.setEditable(false);
        panelFormulario.add(txtUbicacion, gbc);
        
        // Tema/Asunto
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelFormulario.add(new JLabel("Tema:*"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea txtAsunto = new JTextArea(5, 20);
        txtAsunto.setLineWrap(true);
        txtAsunto.setWrapStyleWord(true);
        txtAsunto.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollAsunto = new JScrollPane(txtAsunto);
        panelFormulario.add(scrollAsunto, gbc);
        
        // Nota informativa
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblInfo = new JLabel("<html><i>* Campos obligatorios<br>" +
                                    "Se enviará notificación por correo al alumno.<br>" +
                                    "Si coincide con otra actividad se marcará como Conflicto.</i></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblInfo.setForeground(Color.GRAY);
        panelFormulario.add(lblInfo, gbc);
        
        dialog.add(panelFormulario, BorderLayout.CENTER);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = crearBoton("Crear Reunión", COLOR_SUCCESS);
        btnCrear.addActionListener(e -> {
            // Validaciones
            if (txtTitulo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "El título es obligatorio",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtAula.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "El aula es obligatoria",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtAsunto.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "El tema/asunto es obligatorio",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int alumnoSeleccionado = cmbAlumno.getSelectedIndex();
            if (alumnoSeleccionado < 0) {
                JOptionPane.showMessageDialog(dialog,
                    "Debe seleccionar un alumno",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Alumno alumno = listaAlumnos.get(alumnoSeleccionado);
            String titulo = txtTitulo.getText().trim();
            String aula = txtAula.getText().trim();
            String asunto = txtAsunto.getText().trim();
            int hora = (Integer) spinnerHora.getValue();
            
            // Validar que la hora esté en el rango correcto
            if (hora < 1 || hora > 6) {
                JOptionPane.showMessageDialog(dialog,
                    "La hora debe estar entre 1 y 6 (horas lectivas)",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDateTime fecha = calcularFechaReunion((String) cmbDia.getSelectedItem(), hora);
            
            crearReunion(alumno.getId(), titulo, asunto, aula, fecha);
            dialog.dispose();
        });
        
        JButton btnCancelar = crearBoton("Cancelar", COLOR_DANGER);
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        panelBotones.add(btnCrear);
        panelBotones.add(btnCancelar);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private LocalDateTime calcularFechaReunion(String dia, int hora) {
        LocalDateTime ahora = LocalDateTime.now();
        int diaActual = ahora.getDayOfWeek().getValue();
        
        int diaObjetivo = 0;
        switch (dia) {
            case "LUNES": diaObjetivo = 1; break;
            case "MARTES": diaObjetivo = 2; break;
            case "MIERCOLES": diaObjetivo = 3; break;
            case "JUEVES": diaObjetivo = 4; break;
            case "VIERNES": diaObjetivo = 5; break;
        }
        
        int diasAgregar = diaObjetivo - diaActual;
        if (diasAgregar <= 0) {
            diasAgregar += 7;
        }

        int horaReal = hora;
        
        return ahora.plusDays(diasAgregar).withHour(horaReal).withMinute(0).withSecond(0);
    }
    
    private void crearReunion(int alumnoId, String titulo, String asunto, String aula, LocalDateTime fecha) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean exito = false;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    exito = reunionService.crearReunion(alumnoId, titulo, asunto, aula, fecha);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    System.err.println("Error creando reunión: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "Error al crear reunión:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } else if (exito) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "<html><b>Reunión creada exitosamente</b><br><br>" +
                        "Se ha enviado notificación por correo al alumno.<br>" +
                        "La reunión aparecerá en el horario de ambos participantes.</html>",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarReuniones();
                }
            }
        };
        
        worker.execute();
    }
    
    private void actualizarEstadoReunion() {
        if (reunionSeleccionada == null) {
            return;
        }
        
        String nuevoEstado = (String) cmbEstado.getSelectedItem();
        
        // Confirmar cambio de estado
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "<html>¿Está seguro de cambiar el estado a <b>" + nuevoEstado + "</b>?<br>" +
            "Se enviará notificación por correo a todas las partes.</html>",
            "Confirmar cambio de estado",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean exito = false;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    exito = reunionService.actualizarEstadoReunion(reunionSeleccionada.getIdReunion(), nuevoEstado);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    System.err.println("Error actualizando estado: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "Error al actualizar estado:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } else if (exito) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "<html><b>Estado actualizado exitosamente</b><br><br>" +
                        "Se ha enviado notificación por correo a todas las partes.</html>",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarReuniones();
                }
            }
        };
        
        worker.execute();
    }
    
    private void eliminarReunion() {
        if (reunionSeleccionada == null) {
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar esta reunión?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private boolean exito = false;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    exito = reunionService.eliminarReunion(reunionSeleccionada.getIdReunion());
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    System.err.println("Error eliminando reunión: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "Error al eliminar reunión:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } else if (exito) {
                    JOptionPane.showMessageDialog(ReunionesPanel.this,
                        "Reunión eliminada exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    reunionSeleccionada = null;
                    panelDetalles.removeAll();
                    panelDetalles.revalidate();
                    panelDetalles.repaint();
                    cargarReuniones();
                }
            }
        };
        
        worker.execute();
    }
    
    private void mostrarMapaAula() {
        if (reunionSeleccionada == null || reunionSeleccionada.getAula() == null) {
            JOptionPane.showMessageDialog(this,
                "No se puede mostrar el mapa: aula no especificada",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Mostrar diálogo con información de ubicación
        JDialog dialogMapa = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                         "Ubicación de la Reunión", true);
        dialogMapa.setSize(600, 500);
        dialogMapa.setLocationRelativeTo(this);
        
        JPanel panelMapa = new JPanel(new BorderLayout());
        panelMapa.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Información del aula
        JLabel lblInfo = new JLabel("<html><h2>Aula: " + reunionSeleccionada.getAula() + "</h2>" +
                                    "<p><b>Centro:</b> CIFP Elorrieta-Errekamari LHII</p>" +
                                    "<p><b>Dirección:</b> Otxarkoaga Kalea, 35, 48004 Bilbao, Vizcaya</p>" +
                                    "<p><b>Coordenadas:</b> 43.2627, -2.9253</p></html>");
        lblInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelMapa.add(lblInfo, BorderLayout.NORTH);
        
        // Área de mapa simulado
        JTextArea txtMapaSimulado = new JTextArea();
        txtMapaSimulado.setEditable(false);
        txtMapaSimulado.setText("\n\n" +
                               "          🏫 CIFP ELORRIETA-ERREKAMARI LHII\n\n" +
                               "          📍 Ubicación en Google Maps:\n" +
                               "          https://goo.gl/maps/elorrieta\n\n" +
                               "          Aula: " + reunionSeleccionada.getAula() + "\n" +
                               "          Planta: " + (reunionSeleccionada.getAula().startsWith("5.") ? "5ª" : "N/A") + "\n\n" +
                               "          Cómo llegar:\n" +
                               "          - Metro: Basarrate (L2)\n" +
                               "          - Bus: A3306, A3307, A3308\n\n" +
                               "          [En una implementación completa aquí se mostraría\n" +
                               "           un mapa interactivo con la ubicación del centro\n" +
                               "           usando datos del archivo EuskadiLatLon.json]");
        txtMapaSimulado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtMapaSimulado.setBackground(new Color(230, 230, 230));
        JScrollPane scrollMapa = new JScrollPane(txtMapaSimulado);
        panelMapa.add(scrollMapa, BorderLayout.CENTER);
        
        JButton btnCerrar = crearBoton("Cerrar", COLOR_PRIMARY);
        btnCerrar.addActionListener(e -> dialogMapa.dispose());
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnCerrar);
        panelMapa.add(panelBoton, BorderLayout.SOUTH);
        
        dialogMapa.add(panelMapa);
        dialogMapa.setVisible(true);
    }
}
