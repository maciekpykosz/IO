package controller;

import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.tk.TKClipboard;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.stage.Stage;
import model.dependency.DependencyObj;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;

//TODO: Change in every test to load another project than IO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(ApplicationExtension.class)
class ControllerTest {
    private Controller controllerObj;
    private static final String TEST_FOLDER_PATH = "testProjectIO";

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
        this.controllerObj = loader.getController();
        controllerObj.setStage(primaryStage);
        primaryStage.setTitle("Graph Display");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test
    @Order(1)
    void exportToXML_noGraphLoaded_showPopupWithError(FxRobot robot) {
        robot.clickOn("#exportMenu");
        robot.clickOn("#exportToXML");
        Button errorOkButton = robot.targetWindow("Error").lookup(".button").queryButton();
        Assertions.assertEquals("OK", errorOkButton.getText());
        robot.type(KeyCode.ENTER);
    }

    @Test
    void showAppInfo_showDialogWithAppInfo_null(FxRobot robot) {
        robot.clickOn("#helpMenu");
        robot.clickOn("#showAppInfo");

        Button errorOkButton = robot.targetWindow("Info").lookup(".button").queryButton();
        Assertions.assertEquals("OK", errorOkButton.getText());
        robot.type(KeyCode.ENTER);
    }

    @Test
    void showFileDep_shouldLoadImageToController_null(FxRobot robot) {
        controllerObj.setImage(null);
        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#fileDepMenu");

        ControllerTest.pasteTextByClipboard(robot, ControllerTest.TEST_FOLDER_PATH);

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.ENTER);

        //wait to load image
        while(controllerObj.getImage() == null) {
            robot.sleep(1000);
        }
    }

    @Test
    void showMethodDep_shouldLoadImageToController_null(FxRobot robot) {
        controllerObj.setImage(null);

        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#methodDepMenu");
        robot.clickOn("#methodDep");
        ControllerTest.pasteTextByClipboard(robot, ControllerTest.TEST_FOLDER_PATH);

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.ENTER);

        //wait to load image
        while(controllerObj.getImage() == null) {
            robot.sleep(1000);
        }

        robot.type(KeyCode.ENTER);
    }

    @Test
    void showPackageDep_shouldLoadImageToController_null(FxRobot robot) {
        controllerObj.setImage(null);

        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#packageDepMenu");
        ControllerTest.pasteTextByClipboard(robot, ControllerTest.TEST_FOLDER_PATH);

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.ENTER);

        //wait to load image
        while(controllerObj.getImage() == null) {
            robot.sleep(1000);
        }
        robot.type(KeyCode.ENTER);
    }

    @Test
    void showMethodsWithClass_shouldLoadImageToController_null(FxRobot robot) {
        controllerObj.setImage(null);

        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#methodDefMenu");
        ControllerTest.pasteTextByClipboard(robot, ControllerTest.TEST_FOLDER_PATH);

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.ENTER);

        //wait to load image
        while(controllerObj.getImage() == null) {
            robot.sleep(1000);
        }

        robot.type(KeyCode.ENTER);
    }

    @Test
    void showMixedDep_shouldLoadImageToController_null(FxRobot robot) {
        controllerObj.setImage(null);

        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#chooseGraphCombination");
        //wait to show window
        robot.sleep(1000);
        robot.clickOn("#combinationFileDep");
        robot.clickOn("#combinationMethodDep");
        robot.clickOn("#combinationPackageDep");
        robot.clickOn("#combinationOkButton");

        ControllerTest.pasteTextByClipboard(robot, ControllerTest.TEST_FOLDER_PATH);

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.ENTER);

        //wait to load image
        while(controllerObj.getImage() == null) {
            robot.sleep(1000);
        }

        robot.type(KeyCode.ENTER);
    }

    @Test
    void showMixedDep_closeSubWindow_returnToMainWindow(FxRobot robot) {
        controllerObj.setImage(null);

        robot.clickOn("#dependenciesMenu");
        robot.clickOn("#chooseGraphCombination");
        //wait to show window
        robot.sleep(1000);
        robot.clickOn("#combinationClearButton");

        Assertions.assertDoesNotThrow(() -> robot.clickOn("#dependenciesMenu"));
    }

    @Test
    void exportToXML_savingGraphToXML_makeFileWithXML(FxRobot robot) {
        //run example test to load graph if there isn't image loaded
        if(controllerObj.getImage() == null) {
            showFileDep_shouldLoadImageToController_null(robot);
        }

        robot.clickOn("#exportMenu");
        robot.clickOn("#exportToXML");
        //wait to show window
        robot.sleep(1000);

        ControllerTest.pasteTextByClipboard(robot, "testXml");

        robot.type(KeyCode.ENTER);
        robot.type(KeyCode.LEFT);
        robot.type(KeyCode.ENTER);

        //try to load file
        File defaultDirectory = new File(System.getProperty("user.dir") + "/src/main/resources/testXml.xml");
        Assertions.assertTrue(defaultDirectory.isFile());
        robot.sleep(2000);
    }

    @Test
    void scrollImage_zoomInImage_changedScaleInTransformMatrix(FxRobot robot) {
        //run example test to load graph if there isn't image loaded
        if(controllerObj.getImage() == null) {
            showFileDep_shouldLoadImageToController_null(robot);
        }

        Canvas imageView = robot.targetWindow("Graph Display").lookup("#imageLabel").query();
        robot.moveTo(imageView).scroll(VerticalDirection.UP);
        Assertions.assertTrue(imageView.getGraphicsContext2D().getTransform().getMxx() > 1);
    }

    @Test
    void scrollImage_zoomOutImage_changedScaleInTransformMatrix(FxRobot robot) {
        //run example test to load graph if there isn't image loaded
        if(controllerObj.getImage() == null) {
            showFileDep_shouldLoadImageToController_null(robot);
        }

        Canvas imageView = robot.targetWindow("Graph Display").lookup("#imageLabel").query();
        robot.moveTo(imageView).scroll(VerticalDirection.DOWN);
        Assertions.assertTrue(imageView.getGraphicsContext2D().getTransform().getMxx() < 1);
    }

    @Test
    void moveImage_translateImageInLeftDown_changedTranslateInTransformMatrix(FxRobot robot) {
        //run example test to load graph if there isn't image loaded
        if(controllerObj.getImage() == null) {
            showFileDep_shouldLoadImageToController_null(robot);
        }

        Canvas imageView = robot.targetWindow("Graph Display").lookup("#imageLabel").query();
        robot.moveTo(imageView).press(MouseButton.PRIMARY).moveBy(100,-10).release(MouseButton.PRIMARY);
        Assertions.assertTrue(imageView.getGraphicsContext2D().getTransform().getTx() > 1);
        Assertions.assertTrue(imageView.getGraphicsContext2D().getTransform().getTy() < 0);
    }

    @Test
    void moveImage_translateImageInRightUp_changedTranslateInTransformMatrix(FxRobot robot) {
        //run example test to load graph if there isn't image loaded
        if(controllerObj.getImage() == null) {
            showFileDep_shouldLoadImageToController_null(robot);
        }

        Canvas imageView = robot.targetWindow("Graph Display").lookup("#imageLabel").query();
        robot.moveTo(imageView).press(MouseButton.PRIMARY).moveBy(-100,10).release(MouseButton.PRIMARY);
        Assertions.assertTrue(imageView.getGraphicsContext2D().getTransform().getTx() < 1);
        Assertions.assertTrue(imageView.getGraphicsContext2D().getTransform().getTy() > 0);
    }

    @AfterAll
    static void closeApp() {
        Platform.exit();
    }

    public static void pasteTextByClipboard(FxRobot robot, String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(text);
        clipboard.setContents(stringSelection, stringSelection);
        robot.press(KeyCode.CONTROL).press(KeyCode.V).release(KeyCode.V).release(KeyCode.CONTROL);

    }
}