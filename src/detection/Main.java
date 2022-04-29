package detection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start (Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../detection/view/HomeView.fxml"));
        primaryStage.setTitle("Detection des matricules");
     //  Image image=new Image ("../detection/view/icone2.png");
      //primaryStage.getIcons().add(image);
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
