package com.elorrieta.elores.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestión de configuración de la aplicación
 */
public class AppConfig {
    private static AppConfig instance;
    private Properties properties;
    
    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }
    
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                // Configuración por defecto si no existe el archivo
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Error cargando configuración, usando valores por defecto: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "9999");
    }
    
    public String getServerHost() {
        return properties.getProperty("server.host", "localhost");
    }
    
    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "9999"));
    }
}
