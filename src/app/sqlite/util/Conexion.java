package app.sqlite.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion { 

    // Ruta del archivo productos.db (ajusta la ruta a la ubicación real) 
    private static final String URL = "jdbc:sqlite:C:/Users/rosse/Desktop/database.db";
    
    public static Connection getConnection() throws SQLException { 
        return DriverManager.getConnection(URL); 
    } 
} 