package com.elorrieta.elores.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Clase para encapsular respuestas del servidor
 * Compatible con el protocolo de Reto2ElorServ
 */
public class Response {
    private StatusCode status;
    private String message;
    private Object data;
    
    public Response() {}
    
    public Response(StatusCode status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return status != null && status.getCode() == 200;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    
    public String getDataAsString() {
        if (data != null) {
            return data.toString();
        }
        return null;
    }
    
    /**
     * Clase interna para representar el código de estado
     */
    public static class StatusCode {
        private int code;
        private String description;
        
        public StatusCode() {}
        
        public StatusCode(int code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public int getCode() {
            return code;
        }
        
        public void setCode(int code) {
            this.code = code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return code + " " + description;
        }
    }
}