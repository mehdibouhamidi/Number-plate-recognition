package detection.model;

import java.sql.*;

public class DbConnection {
    private String url="jdbc:mysql://localhost:3306/detection";
    private String username="root";
    private String password="";
    private Connection connect;

    public DbConnection() throws SQLException {
        this.connect= DriverManager.getConnection(url,username,password);
    }
    public Connection getConnection(){
        return connect;
    }
}