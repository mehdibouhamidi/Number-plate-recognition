package detection.controller;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import detection.model.Admin;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.SQLException;

public class HomeController {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton btnConnexion;
    @FXML
    private JFXTextField txtUsername;
    @FXML
    private JFXPasswordField txtPassword;

    @FXML
    private void buttonAction(ActionEvent event) throws IOException, SQLException {
        String username=txtUsername.getText();
        String password=txtPassword.getText();
        boolean b= Admin.existance(username,password);
       if(username.isEmpty() | password.isEmpty()){
            Alert alert=new Alert(Alert.AlertType.ERROR,"Rempli tout les champs! ");
            alert.showAndWait();
        }
       else if(b){
           AnchorPane pane=FXMLLoader.load(getClass().getResource("../view/ImportView.fxml"));
           rootPane.getChildren().setAll(pane);
       }
        else{
            Alert alert=new Alert(Alert.AlertType.ERROR,"Username/password incorrecte");
            alert.showAndWait();
        }
    }
}
