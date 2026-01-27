package com.elorrieta.elores.service;

import java.util.ArrayList;
import java.util.List;

import com.elorrieta.elores.model.Alumno;
import com.elorrieta.elores.network.SocketClient;
import com.elorrieta.elores.protocol.CommandType;
import com.elorrieta.elores.protocol.Request;
import com.elorrieta.elores.protocol.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servicio para gestionar operaciones relacionadas con alumnos
 */
public class AlumnoService {
    private static AlumnoService instance;
    private SocketClient socketClient;
    private Gson gson;
    
    private AlumnoService() {
        socketClient = SocketClient.getInstance();
        gson = new Gson();
    }
    
    public static synchronized AlumnoService getInstance() {
        if (instance == null) {
            instance = new AlumnoService();
        }
        return instance;
    }
    
    /**
     * Obtiene la lista de alumnos del profesor actual
     * @param profesorId ID del profesor
     * @return Lista de alumnos
     * @throws Exception Si hay error en la comunicación
     */
    public List<Alumno> getAlumnos(int profesorId) throws Exception {
        if (!socketClient.isConnected()) {
            throw new Exception("No hay conexión con el servidor");
        }
        
        // Crear payload JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("profesorId", profesorId);
        
        // Crear petición
        Request request = new Request(CommandType.GET_ALUMNOS, payload);
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonArray alumnosArray = JsonParser.parseString(dataStr).getAsJsonArray();
                    List<Alumno> alumnos = new ArrayList<>();
                    
                    for (int i = 0; i < alumnosArray.size(); i++) {
                        JsonObject alumnoJson = alumnosArray.get(i).getAsJsonObject();
                        Alumno alumno = parseAlumno(alumnoJson);
                        alumnos.add(alumno);
                    }
                    
                    return alumnos;
                } catch (Exception e) {
                    System.err.println("Error parseando datos de alumnos: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Error procesando datos de alumnos");
                }
            } else {
                return new ArrayList<>(); // Lista vacía si no hay datos
            }
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error al obtener alumnos");
        }
    }
    
    /**
     * Obtiene un alumno específico por su ID
     * @param alumnoId ID del alumno
     * @return Alumno con todos sus datos
     * @throws Exception Si hay error o el alumno no existe
     */
    public Alumno getAlumno(int alumnoId) throws Exception {
        if (!socketClient.isConnected()) {
            throw new Exception("No hay conexión con el servidor");
        }
        
        // Crear payload JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("alumnoId", alumnoId);
        
        // Crear petición
        Request request = new Request(CommandType.GET_ALUMNOS, payload);
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        if (response.isSuccess()) {
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonObject alumnoJson = JsonParser.parseString(dataStr).getAsJsonObject();
                    return parseAlumno(alumnoJson);
                } catch (Exception e) {
                    System.err.println("Error parseando datos del alumno: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Error procesando datos del alumno");
                }
            } else {
                throw new Exception("Alumno no encontrado");
            }
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error al obtener alumno");
        }
    }
    
    /**
     * Parsea un JsonObject a Alumno
     */
    private Alumno parseAlumno(JsonObject json) {
        Alumno alumno = new Alumno();
        if (json.has("id")) alumno.setId(json.get("id").getAsInt());
        if (json.has("nombre")) alumno.setNombre(json.get("nombre").getAsString());
        if (json.has("apellidos")) alumno.setApellidos(json.get("apellidos").getAsString());
        if (json.has("email")) alumno.setEmail(json.get("email").getAsString());
        if (json.has("dni")) alumno.setDni(json.get("dni").getAsString());
        if (json.has("telefono1")) alumno.setTelefono1(json.get("telefono1").getAsString());
        if (json.has("telefono2")) alumno.setTelefono2(json.get("telefono2").getAsString());
        if (json.has("direccion")) alumno.setDireccion(json.get("direccion").getAsString());
        if (json.has("argazkiaUrl")) alumno.setArgazkiaUrl(json.get("argazkiaUrl").getAsString());
        if (json.has("ciclo")) alumno.setCiclo(json.get("ciclo").getAsString());
        if (json.has("curso")) alumno.setCurso(json.get("curso").getAsString());
        return alumno;
    }
}
