package com.elorrieta.elores.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para cifrado de contraseñas usando BCrypt
 * Compatible con el cifrado usado en el servidor y la app móvil
 */
public class PasswordEncryptor {
    
    /**
     * Cifra una contraseña usando BCrypt
     * @param plainPassword Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    
    /**
     * Verifica si una contraseña coincide con un hash BCrypt
     * @param plainPassword Contraseña en texto plano
     * @param hashedPassword Hash BCrypt almacenado
     * @return true si la contraseña es correcta
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
