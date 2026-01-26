package com.elorrieta.elores.protocol;

/**
 * Enum con los tipos de acciones soportados por el protocolo TCP
 * Compatible con el servidor Reto2ElorServ
 */
public enum CommandType {
    LOGIN,
    DISCONNECT,
    GET_HORARIO,
    GET_REUNIONES,
    CREATE_REUNION,
    UPDATE_REUNION,
    DELETE_REUNION,
    GET_ALUMNOS,
    GET_PERFIL,
    UPDATE_PERFIL,
    PING,
    OTHER
}