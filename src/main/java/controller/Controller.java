package controller;

import com.jamesmurty.utils.XMLBuilder2;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.export.XMLCreator;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.DependencyFinder;
import model.DependencyObj;
import model.EdgeSettings;
import model.GraphDraw;
import model.export.XMLCreator;

public class Controller {
    @FXML
    private GridPane gridPane;
    private GraphDraw graphDraw;
    @FXML
    private Label viewingInfo;
    @FXML
    private ImageView imageLabel;
    private Image image;
    private Stage onCreatedStage;

    private DependencyFinder dependencyFinder;
    private DirectoryChooser directoryChooser;
    private final static String fileNameForFirstGraph = "fileDependencies";
    private final static String fileNameForSecondGraph = "methodDependencies";
    private final static String fileNameForThirdGraph = "moduleDependencies";
    private final static String fileNameForMixedGraph = "mixedDependencies";

    private interface LoadDependency {
        List<DependencyObj> getDependencies(String absolutePath);
    }

    public Controller() {
        super();
        dependencyFinder = new DependencyFinder();
        graphDraw = new GraphDraw();
        directoryChooser = new DirectoryChooser();
    }

    public void setStage(Stage onCreatedStage) {
        this.onCreatedStage = onCreatedStage;
    }

    private void makeDependencies(LoadDependency loadDependency, String fileName, String viewingInfoText) {
        ControllerFunctions.setDefaultDirector(directoryChooser);
        File selectedDir = directoryChooser.showDialog(onCreatedStage);
        if (selectedDir != null) {
            List<DependencyObj> dependencyObjs = loadDependency.getDependencies(selectedDir.getAbsolutePath());
            DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g1 = graphDraw.getGraphForDependencies(dependencyObjs);
            ControllerFunctions.saveGraphImage(fileName, g1);
            File imageFile1 = new File(System.getProperty("user.dir").toString() + "/src/main/resources/" + fileName + ".png");
            viewingInfo.setText(viewingInfoText);

            ControllerFunctions.loadingImage(imageFile1, imageLabel, image, gridPane);
        }
    }

    public void loadFileDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getFilesDependencies(absolutePath), fileNameForFirstGraph, "Showing class dependencies");
    }

    public void loadMethodDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getMethodsDependencies(absolutePath), fileNameForSecondGraph, "Showing method dependencies");
    }

    public void loadPackageDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getModuleDependencies(absolutePath), fileNameForThirdGraph, "Showing module dependencies");
    }

    public void exportToXML(ActionEvent actionEvent) {
        XMLCreator creator = new XMLCreator();
        List<DependencyObj> lastCreated = dependencyFinder.getLastCreatedDependencies();
        creator.addClassesWithDependencies(lastCreated);

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        File defaultDirectory = new File(System.getProperty("user.dir").toString() + "/src/main/resources");
        if (!defaultDirectory.exists()) {
            defaultDirectory.mkdirs();
        }
        fileChooser.setInitialDirectory(defaultDirectory);
        File toSave = fileChooser.showSaveDialog(onCreatedStage);

        if (toSave != null) {
            try {
                XMLBuilder2 builder = creator.getBuilder();
                PrintWriter writer = new PrintWriter(new FileOutputStream(toSave));
                Properties properties = creator.getProperties();
                builder.toWriter(writer, properties);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void chooseCombination() {
        VBox secondaryLayout = new VBox();
        CheckBox checkBoxFile = new CheckBox("File Dependencies");
        CheckBox checkBoxMethod = new CheckBox("Method Dependencies");
        CheckBox checkBoxPackage = new CheckBox("Package Dependencies");
        Label l = new Label("Choose your combination");
        Button b = new Button("OK");
        Button c = new Button("Clear");
        secondaryLayout.getChildren().add(l);
        secondaryLayout.getChildren().add(checkBoxFile);
        secondaryLayout.getChildren().add(checkBoxMethod);
        secondaryLayout.getChildren().add(checkBoxPackage);
        secondaryLayout.getChildren().add(b);
        secondaryLayout.getChildren().add(c);
        Scene secondScene = new Scene(secondaryLayout, 150, 120);

        Stage newWindow = new Stage();
        newWindow.setTitle("Choose graph combination");
        newWindow.setScene(secondScene);
        newWindow.show();
        b.setOnAction(event -> {
            makeDependencies(absolutePath -> {
                List<DependencyObj> mixedDependencies = new ArrayList<>();
                if (checkBoxFile.isSelected()) {
                    List<DependencyObj> filesDependencies = dependencyFinder.getFilesDependencies(absolutePath);
                    mixedDependencies.addAll(filesDependencies);
                }
                if (checkBoxMethod.isSelected()) {
                    List<DependencyObj> methodsDependencies = dependencyFinder.getMethodsDependencies(absolutePath);
                    mixedDependencies.addAll(methodsDependencies);
                }
                if (checkBoxPackage.isSelected()) {
                    List<DependencyObj> moduleDependencies = dependencyFinder.getModuleDependencies(absolutePath);
                    mixedDependencies.addAll(moduleDependencies);
                }
                return mixedDependencies;
            }, fileNameForMixedGraph, "Showing mixed dependencies");
            newWindow.close();
        });
        c.setOnAction(event -> {
            checkBoxFile.setSelected(false);
            checkBoxMethod.setSelected(false);
            checkBoxPackage.setSelected(false);
            newWindow.close();
        });
    }

    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void showAppInfo(ActionEvent actionEvent) {
        ControllerFunctions.infoAlert();
    }
}
