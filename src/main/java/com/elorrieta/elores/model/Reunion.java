package com.elorrieta.elores.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO para representar una reunión
 * Coincide con la estructura de la tabla 'reuniones' de la base de datos
 */
public class Reunion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int idReunion;
    private String estado; // pendiente, aceptada, denegada, conflicto
    private String estadoEus;
    private Integer profesorId;
    private Integer alumnoId;
    private String idCentro;
    private String titulo;
    private String asunto;
    private String aula;
    private LocalDateTime fecha;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campos adicionales para mostrar en el horario
    private String dia; // Día de la semana calculado desde fecha
    private int hora; // Hora calculada desde fecha
    
    public Reunion() {}

    public int getIdReunion() {
        return idReunion;
    }

    public void setIdReunion(int idReunion) {
        this.idReunion = idReunion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoEus() {
        return estadoEus;
    }

    public void setEstadoEus(String estadoEus) {
        this.estadoEus = estadoEus;
    }

    public Integer getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(Integer profesorId) {
        this.profesorId = profesorId;
    }

    public Integer getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Integer alumnoId) {
        this.alumnoId = alumnoId;
    }

    public String getIdCentro() {
        return idCentro;
    }

    public void setIdCentro(String idCentro) {
        this.idCentro = idCentro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }
    
    // Alias para compatibilidad con el panel
    public String getDiaSemana() {
        return dia;
    }
    
    public void setDiaSemana(String dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }
}