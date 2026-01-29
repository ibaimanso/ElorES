package com.elorrieta.elores.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elorrieta.elores.controller.NavigationController;
import com.elorrieta.elores.model.Horario;
import com.elorrieta.elores.model.Reunion;
import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.service.AuthService;
import com.elorrieta.elores.service.HorarioService;

/**
 * Panel para consultar el horario del profesor (CU05)
 * Muestra horario semanal con clases, tutorías, guardias y reuniones
 * Los profesores pueden consultar horarios de otros profesores
 */
public class HorarioPanel extends JPanel {
    private static final String[] DIAS = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"};
    private static final int HORAS = 6;
    
    // Colores según estado de reunión
    private static final Color COLOR_CONFLICTO = new Color(169, 169, 169); // Gris
    private static final Color COLOR_ACEPTADO = new Color(144, 238, 144); // Verde
    private static final Color COLOR_CANCELADO = new Color(255, 99, 71); // Rojo
    private static final Color COLOR_PENDIENTE = new Color(255, 215, 0); // Amarillo
    private static final Color COLOR_CLASE = new Color(173, 216, 230); // Azul claro
    private static final Color COLOR_TUTORIA = new Color(221, 160, 221); // Púrpura claro
    private static final Color COLOR_GUARDIA = new Color(255, 182, 193); // Rosa claro
    private static final Color COLOR_VACIO = Color.WHITE;
    
    private HorarioService horarioService;
    private AuthService authService;
    private NavigationController navigationController;
    private JPanel panelHorario;
    private JButton btnVolver;
    private JLabel lblTitulo;
    private JComboBox<ProfesorItem> cmbProfesores;
    private List<Usuario> listaProfesores;
    
    // Almacenar las celdas del horario para poder actualizarlas
    private Map<String, JPanel> celdasHorario;

    public HorarioPanel() {
        horarioService = HorarioService.getInstance();
        authService = AuthService.getInstance();
        navigationController = NavigationController.getInstance();
        celdasHorario = new HashMap<>();
        initComponents();
        cargarListaProfesores();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel superior con título, selector de profesor y botón volver
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(245, 245, 245));
        
        lblTitulo = new JLabel("📅 Horario Semanal");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(41, 128, 185));
        panelSuperior.add(lblTitulo, BorderLayout.CENTER);
        
        btnVolver = new JButton("← Volver");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnVolver.setBackground(new Color(52, 152, 219));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            MenuPanel menuPanel = new MenuPanel();
            navigationController.navigateTo(menuPanel);
        });
        panelSuperior.add(btnVolver, BorderLayout.WEST);
        
        // Panel selector de profesor
        JPanel panelSelector = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panelSelector.setBackground(new Color(245, 245, 245));
        
        JLabel lblProfesor = new JLabel("Profesor:");
        lblProfesor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelSelector.add(lblProfesor);
        
        cmbProfesores = new JComboBox<>();
        cmbProfesores.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbProfesores.setPreferredSize(new Dimension(250, 30));
        cmbProfesores.addActionListener(e -> {
            ProfesorItem selectedItem = (ProfesorItem) cmbProfesores.getSelectedItem();
            if (selectedItem != null) {
                cargarHorarioProfesor(selectedItem.getId());
            }
        });
        panelSelector.add(cmbProfesores);
        
        panelSuperior.add(panelSelector, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central con el horario en formato tabla
        panelHorario = new JPanel(new GridBagLayout());
        panelHorario.setBackground(Color.WHITE);
        panelHorario.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        
        JScrollPane scrollPane = new JScrollPane(panelHorario);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior con leyenda de colores
        JPanel panelLeyenda = crearPanelLeyenda();
        add(panelLeyenda, BorderLayout.SOUTH);
        
        // Crear estructura del horario vacío
        crearEstructuraHorario();
    }
    
    /**
     * Carga la lista de profesores desde el servidor
     */
    private void cargarListaProfesores() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String errorMessage;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    listaProfesores = horarioService.getListaProfesores();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    System.err.println("Error cargando lista de profesores: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(HorarioPanel.this,
                        "Error al cargar la lista de profesores:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (listaProfesores != null && !listaProfesores.isEmpty()) {
                    // Poblar el ComboBox con los profesores
                    for (Usuario profesor : listaProfesores) {
                        String nombre = profesor.getNombre() + " " + 
                                      (profesor.getApellidos() != null ? profesor.getApellidos() : "");
                        cmbProfesores.addItem(new ProfesorItem(profesor.getId(), nombre.trim()));
                    }
                    
                    // Seleccionar el profesor actual por defecto
                    Usuario usuarioActual = authService.getUsuarioActual();
                    if (usuarioActual != null) {
                        for (int i = 0; i < cmbProfesores.getItemCount(); i++) {
                            ProfesorItem item = cmbProfesores.getItemAt(i);
                            if (item.getId() == usuarioActual.getId()) {
                                cmbProfesores.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                    
                    // Si no se disparó el evento de selección, cargar el primer horario
                    if (cmbProfesores.getSelectedItem() != null) {
                        ProfesorItem selectedItem = (ProfesorItem) cmbProfesores.getSelectedItem();
                        cargarHorarioProfesor(selectedItem.getId());
                    }
                } else {
                    JOptionPane.showMessageDialog(HorarioPanel.this,
                        "No se encontraron profesores en el sistema.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Crea la estructura vacía del horario
     */
    private void crearEstructuraHorario() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(1, 1, 1, 1);
        
        // Fila de encabezado vacía para las horas
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblVacio = new JLabel("");
        lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
        lblVacio.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        lblVacio.setOpaque(true);
        lblVacio.setBackground(new Color(240, 240, 240));
        panelHorario.add(lblVacio, gbc);
        
        // Encabezados de los días
        for (int dia = 0; dia < DIAS.length; dia++) {
            gbc.gridx = dia + 1;
            gbc.gridy = 0;
            JLabel lblDia = new JLabel(DIAS[dia]);
            lblDia.setHorizontalAlignment(SwingConstants.CENTER);
            lblDia.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblDia.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            lblDia.setOpaque(true);
            lblDia.setBackground(new Color(41, 128, 185));
            lblDia.setForeground(Color.WHITE);
            panelHorario.add(lblDia, gbc);
        }
        
        // Crear celdas para cada hora y día
        for (int hora = 1; hora <= HORAS; hora++) {
            // Etiqueta de la hora
            gbc.gridx = 0;
            gbc.gridy = hora;
            JLabel lblHora = new JLabel("Hora " + hora);
            lblHora.setHorizontalAlignment(SwingConstants.CENTER);
            lblHora.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblHora.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            lblHora.setOpaque(true);
            lblHora.setBackground(new Color(240, 240, 240));
            panelHorario.add(lblHora, gbc);
            
            // Celdas para cada día
            for (int dia = 0; dia < DIAS.length; dia++) {
                gbc.gridx = dia + 1;
                gbc.gridy = hora;
                
                JPanel celda = crearCeldaVacia();
                String clave = DIAS[dia] + "_" + hora;
                celdasHorario.put(clave, celda);
                panelHorario.add(celda, gbc);
            }
        }
    }
    
    /**
     * Crea una celda vacía del horario
     */
    private JPanel crearCeldaVacia() {
        JPanel celda = new JPanel(new BorderLayout());
        celda.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        celda.setBackground(COLOR_VACIO);
        celda.setPreferredSize(new Dimension(100, 80));
        
        JLabel lblVacio = new JLabel("");
        lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
        celda.add(lblVacio, BorderLayout.CENTER);
        
        return celda;
    }
    
    /**
     * Carga el horario de un profesor específico
     */
    private void cargarHorarioProfesor(int profesorId) {
        // Limpiar horario actual
        limpiarHorario();
        
        // Mostrar indicador de carga
        SwingUtilities.invokeLater(() -> {
            lblTitulo.setText("📅 Cargando horario...");
        });
        
        // Cargar en segundo plano
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Horario> horarios;
            private List<Reunion> reuniones;
            private String errorMessage;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Obtener horario y reuniones del profesor seleccionado
                    horarios = horarioService.getHorarioProfesorPorId(profesorId);
                    reuniones = horarioService.getReunionesProfesorPorId(profesorId);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    System.err.println("Error cargando horario: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                lblTitulo.setText("📅 Horario Semanal");
                
                if (errorMessage != null) {
                    // Mostrar error
                    JOptionPane.showMessageDialog(HorarioPanel.this,
                        "Error al cargar el horario:\n" + errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (horarios == null || horarios.isEmpty()) {
                    // No hay horario disponible
                    JOptionPane.showMessageDialog(HorarioPanel.this,
                        "No hay horario disponible para este profesor.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                // Mostrar horarios
                for (Horario horario : horarios) {
                    mostrarHorarioEnCelda(horario);
                }
                
                // Mostrar reuniones
                if (reuniones != null) {
                    for (Reunion reunion : reuniones) {
                        mostrarReunionEnCelda(reunion);
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Limpia el horario actual
     */
    private void limpiarHorario() {
        for (JPanel celda : celdasHorario.values()) {
            celda.removeAll();
            celda.setBackground(COLOR_VACIO);
            JLabel lblVacio = new JLabel("");
            lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
            celda.add(lblVacio, BorderLayout.CENTER);
            celda.revalidate();
            celda.repaint();
        }
    }
    
    /**
     * Carga el horario desde el servidor (método deprecated, usar cargarHorarioProfesor)
     */
    @Deprecated
    private void cargarHorario() {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario != null) {
            cargarHorarioProfesor(usuario.getId());
        }
    }
    
    /**
     * Muestra un horario en su celda correspondiente
     */
    private void mostrarHorarioEnCelda(Horario horario) {
        String clave = horario.getDia() + "_" + horario.getHora();
        JPanel celda = celdasHorario.get(clave);
        
        if (celda != null) {
            celda.removeAll();
            
            // Determinar color según tipo
            Color colorFondo;
            switch (horario.getTipo().toUpperCase()) {
                case "CLASE":
                    colorFondo = COLOR_CLASE;
                    break;
                case "TUTORIA":
                    colorFondo = COLOR_TUTORIA;
                    break;
                case "GUARDIA":
                    colorFondo = COLOR_GUARDIA;
                    break;
                default:
                    colorFondo = COLOR_VACIO;
            }
            celda.setBackground(colorFondo);
            
            // Crear contenido según tipo
            if ("CLASE".equals(horario.getTipo())) {
                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(false);
                textArea.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                
                // Usar el método toString() del modelo que ya construye el texto correctamente
                textArea.setText(horario.toString());
                textArea.setMargin(new Insets(5, 5, 5, 5));
                celda.add(textArea, BorderLayout.CENTER);
            } else {
                JLabel lblTipo = new JLabel(horario.getTipo());
                lblTipo.setHorizontalAlignment(SwingConstants.CENTER);
                lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 11));
                celda.add(lblTipo, BorderLayout.CENTER);
            }
            
            celda.revalidate();
            celda.repaint();
        }
    }
    
    /**
     * Muestra una reunión en su celda correspondiente
     * Si ya hay contenido (horario), lo superpone o modifica el color
     */
    private void mostrarReunionEnCelda(Reunion reunion) {
        String clave = reunion.getDiaSemana() + "_" + reunion.getHora();
        JPanel celda = celdasHorario.get(clave);
        
        if (celda != null) {
            // Determinar color según estado
            Color colorFondo;
            String estadoUpper = reunion.getEstado().toUpperCase();
            
            if (estadoUpper.equals("CONFLICTO") || estadoUpper.equals("GATAZKA")) {
                colorFondo = COLOR_CONFLICTO;
            } else if (estadoUpper.equals("ACEPTADA") || estadoUpper.equals("ACEPTADO") || estadoUpper.equals("ONARTUTA")) {
                colorFondo = COLOR_ACEPTADO;
            } else if (estadoUpper.equals("DENEGADA") || estadoUpper.equals("CANCELADO") || estadoUpper.equals("EZEZTATUTA")) {
                colorFondo = COLOR_CANCELADO;
            } else if (estadoUpper.equals("PENDIENTE") || estadoUpper.equals("ONARTZEKE")) {
                colorFondo = COLOR_PENDIENTE;
            } else {
                colorFondo = COLOR_PENDIENTE; // Por defecto
            }
            
            // Si la celda ya tiene contenido, crear un panel compuesto
            if (celda.getComponentCount() > 0 && celda.getComponent(0) instanceof JTextArea) {
                // Hay una clase, agregar la reunión debajo
                JPanel panelCompuesto = new JPanel(new BorderLayout());
                panelCompuesto.setBackground(colorFondo);
                
                // Mantener el contenido anterior en la parte superior
                Component contenidoAnterior = celda.getComponent(0);
                JPanel panelSuperior = new JPanel(new BorderLayout());
                panelSuperior.setOpaque(false);
                panelSuperior.add(contenidoAnterior, BorderLayout.CENTER);
                panelCompuesto.add(panelSuperior, BorderLayout.CENTER);
                
                // Agregar la reunión en la parte inferior
                String tituloReunion = reunion.getTitulo() != null ? reunion.getTitulo() : "Reunión";
                JLabel lblReunion = new JLabel("Reunión: " + tituloReunion);
                lblReunion.setHorizontalAlignment(SwingConstants.CENTER);
                lblReunion.setFont(new Font("Segoe UI", Font.BOLD, 9));
                lblReunion.setBorder(new EmptyBorder(2, 2, 2, 2));
                panelCompuesto.add(lblReunion, BorderLayout.SOUTH);
                
                celda.removeAll();
                celda.add(panelCompuesto, BorderLayout.CENTER);
            } else {
                // La celda está vacía o tiene solo texto simple
                celda.removeAll();
                celda.setBackground(colorFondo);
                
                JTextArea textArea = new JTextArea();
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(false);
                textArea.setFont(new Font("Segoe UI", Font.BOLD, 10));
                
                String tituloReunion = reunion.getTitulo() != null ? reunion.getTitulo() : "Sin título";
                textArea.setText("Reunión:\n" + tituloReunion);
                textArea.setMargin(new Insets(5, 5, 5, 5));
                celda.add(textArea, BorderLayout.CENTER);
            }
            
            celda.revalidate();
            celda.repaint();
        }
    }
    
    /**
     * Crea el panel de leyenda con los códigos de colores
     */
    private JPanel crearPanelLeyenda() {
        JPanel panelLeyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelLeyenda.setBackground(new Color(245, 245, 245));
        panelLeyenda.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Código de Colores",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)
        ));
        
        // Leyenda para reuniones
        panelLeyenda.add(crearItemLeyenda("Conflicto", COLOR_CONFLICTO));
        panelLeyenda.add(crearItemLeyenda("Aceptado", COLOR_ACEPTADO));
        panelLeyenda.add(crearItemLeyenda("Cancelado", COLOR_CANCELADO));
        panelLeyenda.add(crearItemLeyenda("Pendiente", COLOR_PENDIENTE));
        
        return panelLeyenda;
    }
    
    /**
     * Crea un item de leyenda con color y etiqueta
     */
    private JPanel crearItemLeyenda(String texto, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(new Color(245, 245, 245));
        
        JPanel cuadroColor = new JPanel();
        cuadroColor.setBackground(color);
        cuadroColor.setPreferredSize(new Dimension(20, 20));
        cuadroColor.setBorder(new LineBorder(Color.BLACK, 1));
        
        JLabel lblTexto = new JLabel(texto);
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        panel.add(cuadroColor);
        panel.add(lblTexto);
        
        return panel;
    }
    
    /**
     * Clase interna para representar un item del ComboBox de profesores
     */
    private static class ProfesorItem {
        private final int id;
        private final String nombre;
        
        public ProfesorItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
        
        public int getId() {
            return id;
        }
        
        @Override
        public String toString() {
            return nombre;
        }
    }
}
