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
 * Servicio de autenticación
 * Gestiona login, logout y sesión del usuario
 */
public class AuthService {
    private static AuthService instance;
    private Usuario usuarioActual;
    private SocketClient socketClient;
    private Gson gson;
    
    private AuthService() {
        socketClient = SocketClient.getInstance();
        gson = new Gson();
    }
    
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Realiza el login del usuario
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return Usuario autenticado o null si falla
     * @throws Exception Si hay error de conexión o credenciales inválidas
     */
    public Usuario login(String email, String password) throws Exception {
        // Conectar al servidor si no está conectado
        if (!socketClient.isConnected()) {
            socketClient.connect();
            // Leer mensaje de bienvenida del servidor UNA SOLA VEZ
            try {
                socketClient.readWelcomeMessage();
            } catch (Exception e) {
                System.err.println("Error leyendo bienvenida: " + e.getMessage());
            }
        }
        
        // Crear payload JSON
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        
        // Crear petición de login
        Request request = new Request(CommandType.LOGIN, payload);
        
        System.out.println("=== ENVIANDO LOGIN ===");
        
        // Enviar petición
        Response response = socketClient.sendRequest(request);
        
        System.out.println("=== RESPUESTA RECIBIDA ===");
        System.out.println("Success: " + response.isSuccess());
        System.out.println("Message: " + response.getMessage());
        System.out.println("Data: " + response.getDataAsString());
        
        if (response.isSuccess()) {
            // Parsear datos del usuario desde la respuesta
            String dataStr = response.getDataAsString();
            if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("null")) {
                try {
                    JsonObject userData = JsonParser.parseString(dataStr).getAsJsonObject();
                    usuarioActual = parseUsuario(userData);
                    return usuarioActual;
                } catch (Exception e) {
                    System.err.println("Error parseando datos de usuario: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Error procesando datos del usuario");
                }
            } else {
                throw new Exception("Respuesta del servidor sin datos de usuario");
            }
        } else {
            throw new Exception(response.getMessage() != null ? response.getMessage() : "Error desconocido en autenticación");
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
        if (json.has("tipoId")) usuario.setTipoId(json.get("tipoId").getAsInt());
        if (json.has("tipoNombre")) usuario.setTipoNombre(json.get("tipoNombre").getAsString());
        return usuario;
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() throws Exception {
        if (usuarioActual != null && socketClient.isConnected()) {
            try {
                Request request = new Request(CommandType.DISCONNECT);
                JsonObject payload = new JsonObject();
                payload.addProperty("userId", usuarioActual.getId());
                request.setPayload(payload);
                socketClient.sendRequest(request);
            } finally {
                usuarioActual = null;
                socketClient.disconnect();
            }
        }
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return usuarioActual != null;
    }
}