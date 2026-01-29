package com.elorrieta.elores.service;

import java.util.ArrayList;
import java.util.List;

import com.elorrieta.elores.model.Horario;
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
 * Servicio para gestionar horarios del profesor
 */
public class HorarioService {
    private static HorarioService instance;
    private SocketClient socketClient;
    private AuthService authService;
    private Gson gson;
    
    private HorarioService() {
        socketClient = SocketClient.getInstance();
        authService = AuthService.getInstance();
        gson = new Gson();
    }
    
    public static synchronized HorarioService getInstance() {
        if (instance == null) {
            instance = new HorarioService();
        }
        return instance;
    }
    
    /**
     * Obtiene el horario del profesor actual con sus reuniones
     * @return Lista de horarios del profesor
     * @throws Exception Si hay error de conexión o el horario no existe
     */
    public List<Horario> getHorarioProfesor() throws Exception {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("Usuario no autenticado");
        }
        return getHorarioProfesorPorId(usuario.getId());
    }
    
    /**
     * Obtiene el horario de un profesor específico por ID
     * @param profesorId ID del profesor
     * @return Lista de horarios del profesor
     * @throws Exception Si hay error de conexión o el horario no existe
     */
    public List<Horario> getHorarioProfesorPorId(int profesorId) throws Exception {
        // Crear payload JSON con el ID del profesor
        JsonObject payload = new JsonObject();
        payload.addProperty("profesorId", profesorId);
        
        // Crear petición
        Request request = new Request(CommandType.GET_HORARIO, payload);
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            // Parsear datos del horario
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(dataStr);
                    
                    // Verificar si es un array o un objeto
                    if (jsonElement.isJsonArray()) {
                        JsonArray horarioArray = jsonElement.getAsJsonArray();
                        List<Horario> horarios = new ArrayList<>();
                        
                        for (JsonElement element : horarioArray) {
                            Horario horario = parseHorario(element.getAsJsonObject());
                            horarios.add(horario);
                        }
                        
                        return horarios;
                    } else if (jsonElement.isJsonObject()) {
                        // Si es un objeto único, envolverlo en una lista
                        List<Horario> horarios = new ArrayList<>();
                        horarios.add(parseHorario(jsonElement.getAsJsonObject()));
                        return horarios;
                    }
                } catch (Exception e) {
                    System.err.println("Error parseando horario: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Error procesando datos del horario");
                }
            }
            
            // Si no hay datos, retornar lista vacía
            return new ArrayList<>();
        } else {
            // Error del servidor
            String errorMsg = response.getMessage() != null ? response.getMessage() : "Error desconocido";
            throw new Exception(errorMsg);
        }
    }
    
    /**
     * Obtiene las reuniones del profesor actual
     * @return Lista de reuniones del profesor
     * @throws Exception Si hay error de conexión
     */
    public List<Reunion> getReunionesProfesor() throws Exception {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            throw new Exception("Usuario no autenticado");
        }
        return getReunionesProfesorPorId(usuario.getId());
    }
    
    /**
     * Obtiene las reuniones de un profesor específico por ID
     * @param profesorId ID del profesor
     * @return Lista de reuniones del profesor
     * @throws Exception Si hay error de conexión
     */
    public List<Reunion> getReunionesProfesorPorId(int profesorId) throws Exception {
        // Crear payload JSON con el ID del profesor
        JsonObject payload = new JsonObject();
        payload.addProperty("profesorId", profesorId);
        
        // Crear petición
        Request request = new Request(CommandType.GET_REUNIONES, payload);
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            // Parsear datos de las reuniones
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
            
            // Si no hay datos, retornar lista vacía
            return new ArrayList<>();
        } else {
            // Si hay error, retornar lista vacía (las reuniones son opcionales)
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene la lista de todos los profesores del sistema
     * @return Lista de usuarios con rol de profesor
     * @throws Exception Si hay error de conexión
     */
    public List<Usuario> getListaProfesores() throws Exception {
        // Crear petición para obtener lista de profesores
        Request request = new Request(CommandType.GET_PROFESORES, new JsonObject());
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(dataStr);
                    
                    if (jsonElement.isJsonArray()) {
                        JsonArray profesoresArray = jsonElement.getAsJsonArray();
                        List<Usuario> profesores = new ArrayList<>();
                        
                        for (JsonElement element : profesoresArray) {
                            JsonObject profObj = element.getAsJsonObject();
                            Usuario profesor = new Usuario();
                            
                            if (profObj.has("id")) profesor.setId(profObj.get("id").getAsInt());
                            if (profObj.has("nombre")) profesor.setNombre(profObj.get("nombre").getAsString());
                            if (profObj.has("apellidos")) profesor.setApellidos(profObj.get("apellidos").getAsString());
                            if (profObj.has("apellido")) profesor.setApellidos(profObj.get("apellido").getAsString());
                            if (profObj.has("email")) profesor.setEmail(profObj.get("email").getAsString());
                            
                            profesores.add(profesor);
                        }
                        
                        return profesores;
                    }
                } catch (Exception e) {
                    System.err.println("Error parseando lista de profesores: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return new ArrayList<>();
        } else {
            throw new Exception("Error al obtener lista de profesores: " + response.getMessage());
        }
    }
    
    /**
     * Parsea un objeto JSON a Horario
     * Estructura esperada de la BD:
     * - id, dia, hora, profe_id, modulo_id, aula, observaciones, ciclo_id, curso
     * - Puede incluir: modulo_nombre, ciclo_nombre (desde JOIN)
     */
    private Horario parseHorario(JsonObject jsonObj) {
        Horario horario = new Horario();
        
        if (jsonObj.has("id")) horario.setId(jsonObj.get("id").getAsInt());
        
        // Campo 'dia' en la BD
        if (jsonObj.has("dia")) {
            horario.setDia(jsonObj.get("dia").getAsString());
        } else if (jsonObj.has("diaSemana")) {
            horario.setDia(jsonObj.get("diaSemana").getAsString());
        }
        
        if (jsonObj.has("hora")) horario.setHora(jsonObj.get("hora").getAsInt());
        
        // Campo 'profe_id' en la BD
        if (jsonObj.has("profe_id")) {
            horario.setProfeId(jsonObj.get("profe_id").getAsInt());
        } else if (jsonObj.has("profeId")) {
            horario.setProfeId(jsonObj.get("profeId").getAsInt());
        } else if (jsonObj.has("profesorId")) {
            horario.setProfeId(jsonObj.get("profesorId").getAsInt());
        }
        
        // Campo 'modulo_id' en la BD
        if (jsonObj.has("modulo_id")) {
            horario.setModuloId(jsonObj.get("modulo_id").getAsInt());
        } else if (jsonObj.has("moduloId")) {
            horario.setModuloId(jsonObj.get("moduloId").getAsInt());
        }
        
        // Nombre del módulo (desde JOIN)
        if (jsonObj.has("modulo_nombre")) {
            horario.setModuloNombre(jsonObj.get("modulo_nombre").getAsString());
        } else if (jsonObj.has("moduloNombre")) {
            horario.setModuloNombre(jsonObj.get("moduloNombre").getAsString());
        } else if (jsonObj.has("modulo")) {
            horario.setModuloNombre(jsonObj.get("modulo").getAsString());
        }
        
        if (jsonObj.has("aula") && !jsonObj.get("aula").isJsonNull()) {
            horario.setAula(jsonObj.get("aula").getAsString());
        }
        
        if (jsonObj.has("observaciones") && !jsonObj.get("observaciones").isJsonNull()) {
            horario.setObservaciones(jsonObj.get("observaciones").getAsString());
        }
        
        // Campo 'ciclo_id' en la BD
        if (jsonObj.has("ciclo_id") && !jsonObj.get("ciclo_id").isJsonNull()) {
            horario.setCicloId(jsonObj.get("ciclo_id").getAsInt());
        } else if (jsonObj.has("cicloId") && !jsonObj.get("cicloId").isJsonNull()) {
            horario.setCicloId(jsonObj.get("cicloId").getAsInt());
        }
        
        if (jsonObj.has("curso") && !jsonObj.get("curso").isJsonNull()) {
            horario.setCurso(jsonObj.get("curso").getAsInt());
        }
        
        // Nombre del ciclo (desde JOIN)
        if (jsonObj.has("ciclo_nombre")) {
            horario.setCicloNombre(jsonObj.get("ciclo_nombre").getAsString());
        } else if (jsonObj.has("cicloNombre")) {
            horario.setCicloNombre(jsonObj.get("cicloNombre").getAsString());
        } else if (jsonObj.has("ciclo")) {
            horario.setCicloNombre(jsonObj.get("ciclo").getAsString());
        }
        
        return horario;
    }
    
    /**
     * Parsea un objeto JSON a Reunion
     * Estructura esperada de la BD:
     * - id_reunion, estado, profesor_id, alumno_id, titulo, asunto, aula, fecha
     * - Campos adicionales calculados: dia, hora
     */
    private Reunion parseReunion(JsonObject jsonObj) {
        Reunion reunion = new Reunion();
        
        // Campo 'id_reunion' en la BD
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
        
        // Campo 'profesor_id' en la BD
        if (jsonObj.has("profesor_id") && !jsonObj.get("profesor_id").isJsonNull()) {
            reunion.setProfesorId(jsonObj.get("profesor_id").getAsInt());
        } else if (jsonObj.has("profesorId") && !jsonObj.get("profesorId").isJsonNull()) {
            reunion.setProfesorId(jsonObj.get("profesorId").getAsInt());
        }
        
        // Campo 'alumno_id' en la BD
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
        
        // Campos calculados para mostrar en el horario
        if (jsonObj.has("dia")) {
            reunion.setDia(jsonObj.get("dia").getAsString());
        } else if (jsonObj.has("diaSemana")) {
            reunion.setDia(jsonObj.get("diaSemana").getAsString());
        }
        
        if (jsonObj.has("hora")) {
            reunion.setHora(jsonObj.get("hora").getAsInt());
        }
        
        return reunion;
    }
}