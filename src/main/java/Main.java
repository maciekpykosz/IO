import java.net.URL;

import controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(new URL("file:///"+ System.getProperty("user.dir").toString() + "/src/main/java/view/sample.fxml"));
        Parent root = loader.load();
        ((Controller)loader.getController()).setStage(primaryStage);
        primaryStage.setTitle("Graph Display");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
