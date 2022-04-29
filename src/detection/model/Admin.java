package detection.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Admin {
    private String username;
    private String password;

    public Admin() {
    }

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public static ObservableList<Admin> getUserData() throws SQLException {
        ObservableList<Admin> list= FXCollections.observableArrayList();
        DbConnection db=new DbConnection();
        Connection con=db.getConnection();
        PreparedStatement statement=con.prepareStatement("SELECT *FROM user");
        ResultSet rs=statement.executeQuery();
        while(rs.next()){
            Admin admin=new Admin(rs.getString(1),rs.getString(2));
            list.add(admin);
        }
        return list ;
    }

    public static boolean existance(String username,String password) throws SQLException {
        DbConnection db=new DbConnection();
        Connection con=db.getConnection();
        PreparedStatement statement=con.prepareStatement("SELECT * FROM `user` WHERE username=?" +
                " AND password=?");
        statement.setString(1,username);
        statement.setString(2,password);
        ResultSet rs=statement.executeQuery();
        return rs.next();
    }

}
