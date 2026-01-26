package com.elorrieta.elores.protocol;

import com.google.gson.JsonObject;

/**
 * Clase para encapsular peticiones al servidor
 * Compatible con el protocolo de Reto2ElorServ
 */
public class Request {
    private CommandType action;  // Cambiado de "command" a "action" para compatibilidad
    private String sessionToken;
    private String payload;      // Cambiado de "data" a "payload"
    
    public Request(CommandType action) {
        this.action = action;
        this.payload = "{}";
    }
    
    public Request(CommandType action, String payload) {
        this.action = action;
        this.payload = payload;
    }
    
    public Request(CommandType action, JsonObject data) {
        this.action = action;
        this.payload = data.toString();
    }

    public CommandType getAction() {
        return action;
    }

    public void setAction(CommandType action) {
        this.action = action;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    public void setPayload(JsonObject data) {
        this.payload = data.toString();
    }
}