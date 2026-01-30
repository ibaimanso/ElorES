package com.elorrieta.elores.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.elorrieta.elores.model.Reunion;
import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.network.SocketClient;
import com.elorrieta.elores.protocol.CommandType;
import com.elorrieta.elores.protocol.Request;
import com.elorrieta.elores.protocol.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servicio para gestionar reuniones del profesor
 */
public class ReunionService {
    private static ReunionService instance;
    private SocketClient socketClient;
    private AuthService authService;
    private Gson gson;
    
    private ReunionService() {
        socketClient = SocketClient.getInstance();
        authService = AuthService.getInstance();
        gson = new Gson();
    }
    
    public static synchronized ReunionService getInstance() {
        if (instance == null) {
            instance = new ReunionService();
        }
        return instance;
    }
    
    /**
     * Obtiene las reuniones del profesor actual
     * @return Lista de reuniones
     * @throws Exception Si hay error de conexión
     */
    public List<Reunion> getReunionesProfesor() throws Exception {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("Usuario no autenticado");
        }
        
        JsonObject payload = new JsonObject();
        payload.addProperty("profesorId", usuario.getId());
        
        Request request = new Request(CommandType.GET_REUNIONES, payload);
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(dataStr);
                    
                    if (jsonElement.isJsonArray()) {
                        JsonArray reunionesArray = jsonElement.getAsJsonArray();
                        List<Reunion> reuniones = new ArrayList<>();
                        
                        for (JsonElement element : reunionesArray) {
                            Reunion reunion = parseReunion(element.getAsJsonObject());
                            reuniones.add(reunion);
                        }
                        
                        return reuniones;
                    }
                } catch (Exception e) {
                    System.err.println("Error parseando reuniones: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return new ArrayList<>();
        } else {
            return new ArrayList<>();
        }
    }
    
    /**
     * Crea una nueva reunión
     * @param alumnoId ID del alumno convocado
     * @param titulo Título de la reunión
     * @param asunto Asunto de la reunión
     * @param aula Aula donde se realizará
     * @param fecha Fecha y hora de la reunión
     * @return true si se creó correctamente
     * @throws Exception Si hay error
     */
    public boolean crearReunion(int alumnoId, String titulo, String asunto, String aula, LocalDateTime fecha) throws Exception {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("Usuario no autenticado");
        }
        
        JsonObject payload = new JsonObject();
        payload.addProperty("profesorId", usuario.getId());
        payload.addProperty("alumnoId", alumnoId);
        payload.addProperty("titulo", titulo);
        payload.addProperty("asunto", asunto);
        payload.addProperty("aula", aula);
        payload.addProperty("fecha", fecha.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        Request request = new Request(CommandType.CREATE_REUNION, payload);
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            return true;
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error al crear reunión");
        }
    }
    
    /**
     * Actualiza el estado de una reunión
     * @param reunionId ID de la reunión
     * @param nuevoEstado Nuevo estado (aceptada, denegada, cancelada)
     * @return true si se actualizó correctamente
     * @throws Exception Si hay error
     */
    public boolean actualizarEstadoReunion(int reunionId, String nuevoEstado) throws Exception {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("Usuario no autenticado");
        }
        
        JsonObject payload = new JsonObject();
        payload.addProperty("reunionId", reunionId);
        payload.addProperty("estado", nuevoEstado);
        payload.addProperty("profesorId", usuario.getId());
        
        Request request = new Request(CommandType.UPDATE_REUNION, payload);
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            return true;
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error al actualizar reunión");
        }
    }
    
    /**
     * Elimina una reunión
     * @param reunionId ID de la reunión a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception Si hay error
     */
    public boolean eliminarReunion(int reunionId) throws Exception {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("Usuario no autenticado");
        }
        
        JsonObject payload = new JsonObject();
        payload.addProperty("reunionId", reunionId);
        payload.addProperty("profesorId", usuario.getId());
        
        Request request = new Request(CommandType.DELETE_REUNION, payload);
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            return true;
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error al eliminar reunión");
        }
    }
    
    /**
     * Parsea un objeto JSON a Reunion
     */
    private Reunion parseReunion(JsonObject jsonObj) {
        Reunion reunion = new Reunion();
        
        if (jsonObj.has("id_reunion")) {
            reunion.setIdReunion(jsonObj.get("id_reunion").getAsInt());
        } else if (jsonObj.has("idReunion")) {
            reunion.setIdReunion(jsonObj.get("idReunion").getAsInt());
        } else if (jsonObj.has("id")) {
            reunion.setIdReunion(jsonObj.get("id").getAsInt());
        }
        
        if (jsonObj.has("estado")) {
            reunion.setEstado(jsonObj.get("estado").getAsString());
        }
        
        if (jsonObj.has("profesor_id") && !jsonObj.get("profesor_id").isJsonNull()) {
            reunion.setProfesorId(jsonObj.get("profesor_id").getAsInt());
        } else if (jsonObj.has("profesorId") && !jsonObj.get("profesorId").isJsonNull()) {
            reunion.setProfesorId(jsonObj.get("profesorId").getAsInt());
        }
        
        if (jsonObj.has("alumno_id") && !jsonObj.get("alumno_id").isJsonNull()) {
            reunion.setAlumnoId(jsonObj.get("alumno_id").getAsInt());
        } else if (jsonObj.has("alumnoId") && !jsonObj.get("alumnoId").isJsonNull()) {
            reunion.setAlumnoId(jsonObj.get("alumnoId").getAsInt());
        }
        
        if (jsonObj.has("titulo") && !jsonObj.get("titulo").isJsonNull()) {
            reunion.setTitulo(jsonObj.get("titulo").getAsString());
        }
        
        if (jsonObj.has("asunto") && !jsonObj.get("asunto").isJsonNull()) {
            reunion.setAsunto(jsonObj.get("asunto").getAsString());
        }
        
        if (jsonObj.has("aula") && !jsonObj.get("aula").isJsonNull()) {
            reunion.setAula(jsonObj.get("aula").getAsString());
        }
        
        if (jsonObj.has("dia")) {
            reunion.setDia(jsonObj.get("dia").getAsString());
        } else if (jsonObj.has("diaSemana")) {
            reunion.setDia(jsonObj.get("diaSemana").getAsString());
        }
        
        if (jsonObj.has("hora")) {
            reunion.setHora(jsonObj.get("hora").getAsInt());
        }
        
        if (jsonObj.has("fecha") && !jsonObj.get("fecha").isJsonNull()) {
            try {
                String fechaStr = jsonObj.get("fecha").getAsString();
                reunion.setFecha(LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                System.err.println("Error parseando fecha: " + e.getMessage());
            }
        }
        
        return reunion;
    }
}
