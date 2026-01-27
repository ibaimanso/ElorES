package com.elorrieta.elores.service;

import com.elorrieta.elores.model.Usuario;
import com.elorrieta.elores.network.SocketClient;
import com.elorrieta.elores.protocol.CommandType;
import com.elorrieta.elores.protocol.Request;
import com.elorrieta.elores.protocol.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servicio para gestión de perfiles de usuario
 */
public class PerfilService {
    private static PerfilService instance;
    private SocketClient socketClient;
    private Gson gson;
    
    private PerfilService() {
        socketClient = SocketClient.getInstance();
        gson = new Gson();
    }
    
    public static synchronized PerfilService getInstance() {
        if (instance == null) {
            instance = new PerfilService();
        }
        return instance;
    }
    
    /**
     * Obtiene el perfil completo de un usuario
     * @param userId ID del usuario
     * @return Usuario con todos los datos del perfil
     * @throws Exception Si hay error de conexión o no existe el perfil
     */
    public Usuario getPerfil(int userId) throws Exception {
        // Crear payload JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("userId", userId);
        
        // Crear petición de obtener perfil
        Request request = new Request(CommandType.GET_PERFIL, payload);
        
        System.out.println("=== ENVIANDO GET_PERFIL ===");
        System.out.println("User ID: " + userId);
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        System.out.println("=== RESPUESTA RECIBIDA ===");
        System.out.println("Success: " + response.isSuccess());
        System.out.println("Message: " + response.getMessage());
        
        if (response.isSuccess()) {
            // Parsear datos del perfil desde la respuesta
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonObject perfilData = JsonParser.parseString(dataStr).getAsJsonObject();
                    return parseUsuario(perfilData);
                } catch (Exception e) {
                    System.err.println("Error parseando datos de perfil: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Error procesando datos del perfil");
                }
            } else {
                throw new Exception("Respuesta del servidor sin datos de perfil");
            }
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error obteniendo perfil");
        }
    }
    
    /**
     * Parsea un JsonObject a Usuario
     */
    private Usuario parseUsuario(JsonObject json) {
        Usuario usuario = new Usuario();
        if (json.has("id")) usuario.setId(json.get("id").getAsInt());
        if (json.has("email")) usuario.setEmail(json.get("email").getAsString());
        if (json.has("username")) usuario.setUsername(json.get("username").getAsString());
        if (json.has("nombre")) usuario.setNombre(json.get("nombre").getAsString());
        if (json.has("apellidos")) usuario.setApellidos(json.get("apellidos").getAsString());
        if (json.has("dni")) usuario.setDni(json.get("dni").getAsString());
        if (json.has("direccion")) usuario.setDireccion(json.get("direccion").getAsString());
        if (json.has("telefono1")) usuario.setTelefono1(json.get("telefono1").getAsString());
        if (json.has("telefono2")) usuario.setTelefono2(json.get("telefono2").getAsString());
        if (json.has("tipoId")) usuario.setTipoId(json.get("tipoId").getAsInt());
        if (json.has("tipoNombre")) usuario.setTipoNombre(json.get("tipoNombre").getAsString());
        if (json.has("argazkiaUrl")) usuario.setArgazkiaUrl(json.get("argazkiaUrl").getAsString());
        return usuario;
    }
}
