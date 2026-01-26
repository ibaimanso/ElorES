package com.elorrieta.elores.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.elorrieta.elores.protocol.Request;
import com.elorrieta.elores.protocol.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Cliente TCP para comunicación con el servidor ElorServ
 * Implementa patrón Singleton para gestionar una única conexión
 */
public class SocketClient {
    private static SocketClient instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;
    
    private String host = "localhost";
    private int port = 9000; // Puerto actualizado a 9000
    
    private SocketClient() {
        gson = new GsonBuilder().create();
    }
    
    /**
     * Obtiene la instancia única del cliente
     */
    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }
    
    /**
     * Configura el host y puerto del servidor
     */
    public void configure(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * Conecta al servidor
     */
    public boolean connect() throws IOException {
        if (isConnected()) {
            return true;
        }
        
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException e) {
            disconnect();
            throw new IOException("No se pudo conectar al servidor en " + host + ":" + port, e);
        }
    }
    
    /**
     * Lee el mensaje de bienvenida del servidor
     */
    public void readWelcomeMessage() throws IOException {
        if (!isConnected()) {
            throw new IOException("No hay conexión con el servidor");
        }
        
        try {
            String welcomeJson = in.readLine();
            if (welcomeJson != null) {
                Response welcome = gson.fromJson(welcomeJson, Response.class);
                System.out.println("Servidor: " + welcome.getMessage());
            }
        } catch (IOException e) {
            throw new IOException("Error leyendo mensaje de bienvenida", e);
        }
    }
    
    /**
     * Verifica si hay conexión activa
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    /**
     * Envía una petición y espera respuesta
     */
    public Response sendRequest(Request request) throws IOException {
        if (!isConnected()) {
            throw new IOException("No hay conexión con el servidor");
        }
        
        try {
            // Serializar y enviar request
            String jsonRequest = gson.toJson(request);
            System.out.println("Enviando: " + jsonRequest);
            out.println(jsonRequest);
            
            // Leer respuesta
            String jsonResponse = in.readLine();
            if (jsonResponse == null) {
                throw new IOException("El servidor cerró la conexión");
            }
            
            System.out.println("Recibido: " + jsonResponse);
            
            // Deserializar respuesta
            Response response = gson.fromJson(jsonResponse, Response.class);
            return response;
            
        } catch (IOException e) {
            disconnect();
            throw new IOException("Error en la comunicación con el servidor", e);
        }
    }
    
    /**
     * Cierra la conexión con el servidor
     */
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        } finally {
            socket = null;
            in = null;
            out = null;
        }
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
}