package controller;

import com.sun.javafx.robot.FXRobot;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@ExtendWith(ApplicationExtension.class)
class ControllerTest {

    @Start
    private void start(Stage primaryStage) {
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(new URL("file:///"+ System.getProperty("user.dir").toString() + "/src/main/java/view/sample.fxml"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ((Controller)loader.getController()).setStage(primaryStage);
        primaryStage.setTitle("Graph Display");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test
    void showAppInfo_click_null(FxRobot robot) {
        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#fileDepMenu");
        robot.type(KeyCode.I);
        robot.type(KeyCode.O);

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.ENTER);

        robot.sleep(1000);
    }
}