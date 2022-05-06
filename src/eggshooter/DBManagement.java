/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eggshooter;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author ASIA
 */
public class DBManagement {
    public static final String URL = "jdbc:mysql://localhost:3306/eggshooter";
    public static final String UID = "root";
    public static final String PWD = "";
    private Connection conn;
    
    public DBManagement() throws SQLException {
        DriverManager.registerDriver(new Driver());
        conn = (Connection)DriverManager.getConnection(URL, UID, PWD);
    }
    
    public Connection getConnection() {
        return this.conn;
    }
    
    public boolean isConnected() {
        return this.conn != null;
    }
    
    public void closeConnection() throws SQLException {
        this.conn.close();
    }
}
