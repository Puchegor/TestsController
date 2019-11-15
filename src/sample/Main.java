package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Classes.Config;
import sample.Classes.DB;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        DB.setConnection(Config.configRead());
        Parent root = FXMLLoader.load(getClass().getResource("FXML/mainWindow.fxml"));
        primaryStage.setTitle("DIZ Test controller");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
