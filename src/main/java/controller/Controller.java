package controller;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.io.File;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.DependencyFinder;
import model.DependencyObj;
import model.EdgeSettings;
import model.GraphDraw;

public class Controller  {
    @FXML
    private GridPane gridPane;
    @FXML
    private MenuItem fileDepMenu;
    @FXML
    private MenuItem methodDepMenu;
    @FXML
    private MenuItem packageDepMenu;
    private GraphDraw graphDraw;
    @FXML
    private Label viewingInfo;
    @FXML
    private ImageView imageLabel;
    private Image image;
    private Stage onCreatedStage;

    private DependencyFinder dependencyFinder;
    private DirectoryChooser directoryChooser;
    private final static String fileNameForFirstGraph = "graf1";
    private final static String fileNameForSecondGraph = "graf2";
    private final static String fileNameForThirdGraph = "graf3";

    public Controller ()
    {
        super();
        dependencyFinder = new DependencyFinder();
        graphDraw = new GraphDraw();
        directoryChooser = new DirectoryChooser();
    }

    public void setStage(Stage onCreatedStage) {
        this.onCreatedStage = onCreatedStage;
    }
    public void loadFileDep(ActionEvent actionEvent) {

        File selectedDir = directoryChooser.showDialog(onCreatedStage);
        if(selectedDir != null)
        {
            List<DependencyObj> dependencyObjs = dependencyFinder.getFilesDependencies(selectedDir.getAbsolutePath());
            DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g1 = graphDraw.getGraphForDependencies(dependencyObjs);
            ControllerFunctions.saveGraphImage(fileNameForFirstGraph, g1);
            File imageFile1 = new File(System.getProperty("user.dir").toString()+ "/src/main/resources/" + fileNameForFirstGraph + ".png");
            viewingInfo.setText("Showing classs dependencies");

            ControllerFunctions.loadingImage(imageFile1, imageLabel, image, gridPane);
        }
    }

    public void loadMethodDep(ActionEvent actionEvent) {
        File selectedDir = directoryChooser.showDialog(onCreatedStage);
        if(selectedDir != null)
        {
            List<DependencyObj> dependencyObjs = dependencyFinder.getMethodsDependencies(selectedDir.getAbsolutePath());
            DependencyObj.calculateWeightsForMethods(dependencyObjs);
            DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g2 = graphDraw.getGraphForDependencies(dependencyObjs);
            ControllerFunctions.saveGraphImage(fileNameForSecondGraph, g2);
            File imageFile2 = new File(System.getProperty("user.dir").toString() + "/src/main/resources/" + fileNameForSecondGraph + ".png");
            viewingInfo.setText("Showing method dependencies");

            ControllerFunctions.loadingImage(imageFile2, imageLabel, image, gridPane);
        }
    }

    public void loadPackageDep(ActionEvent actionEvent) {
        File selectedDir = directoryChooser.showDialog(onCreatedStage);
        if(selectedDir != null)
        {
            List<DependencyObj> dependencyObjs = dependencyFinder.getModuleDependencies(selectedDir.getAbsolutePath());
            DependencyObj.calculateWeightsForMethods(dependencyObjs);
            DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g3 = graphDraw.getGraphForDependencies(dependencyObjs);
            ControllerFunctions.saveGraphImage(fileNameForThirdGraph, g3);
            File imageFile3 = new File(System.getProperty("user.dir").toString() + "/src/main/resources/" + fileNameForThirdGraph + ".png");
            viewingInfo.setText("Showing module dependencies");

            ControllerFunctions.loadingImage(imageFile3, imageLabel, image, gridPane);
        }
    }

    public void exportToXML(ActionEvent actionEvent){
        //create XML and save it to file
    }

    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void showAppInfo(ActionEvent actionEvent) {
        ControllerFunctions.infoAlert();
    }
}
