package com.elorrieta.elores.model;

import java.io.Serializable;

/**
 * DTO para representar un horario del profesor
 * Coincide con la estructura de la tabla 'horarios' de la base de datos
 */
public class Horario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String dia; // LUNES, MARTES, MIERCOLES, JUEVES, VIERNES
    private int hora; // 1-6
    private int profeId;
    private int moduloId;
    private String moduloNombre; // Nombre del módulo
    private String aula;
    private String observaciones;
    private Integer cicloId;
    private Integer curso;
    private String cicloNombre; // Nombre del ciclo
    
    public Horario() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getProfeId() {
        return profeId;
    }

    public void setProfeId(int profeId) {
        this.profeId = profeId;
    }

    public int getModuloId() {
        return moduloId;
    }

    public void setModuloId(int moduloId) {
        this.moduloId = moduloId;
    }

    public String getModuloNombre() {
        return moduloNombre;
    }

    public void setModuloNombre(String moduloNombre) {
        this.moduloNombre = moduloNombre;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getCicloId() {
        return cicloId;
    }

    public void setCicloId(Integer cicloId) {
        this.cicloId = cicloId;
    }

    public Integer getCurso() {
        return curso;
    }

    public void setCurso(Integer curso) {
        this.curso = curso;
    }

    public String getCicloNombre() {
        return cicloNombre;
    }

    public void setCicloNombre(String cicloNombre) {
        this.cicloNombre = cicloNombre;
    }
    
    /**
     * Determina el tipo de entrada basándose en el módulo
     * @return "TUTORIA", "GUARDIA" o "CLASE"
     */
    public String getTipo() {
        if (moduloNombre == null) {
            return "CLASE";
        }
        
        String moduloLower = moduloNombre.toLowerCase();
        if (moduloLower.contains("tutoria") || moduloLower.contains("tutoretza")) {
            return "TUTORIA";
        } else if (moduloLower.contains("guardia") || moduloLower.contains("zaintza")) {
            return "GUARDIA";
        } else {
            return "CLASE";
        }
    }
    
    @Override
    public String toString() {
        String tipo = getTipo();
        if ("CLASE".equals(tipo)) {
            StringBuilder sb = new StringBuilder();
            if (moduloNombre != null) {
                sb.append(moduloNombre);
            }
            if (curso != null && cicloNombre != null) {
                sb.append("\n").append(curso).append("º ").append(cicloNombre);
            }
            if (aula != null && !aula.isEmpty()) {
                sb.append("\n").append(aula);
            }
            return sb.toString();
        } else {
            return tipo;
        }
    }
}