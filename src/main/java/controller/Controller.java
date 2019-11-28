package controller;

import com.jamesmurty.utils.XMLBuilder2;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.dependency.DependencyFinder;
import model.dependency.DependencyObj;
import model.GraphDraw;
import model.GraphDraw.EdgeSettings;
import model.export.XMLCreator;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Controller {
    @FXML
    private GridPane gridPane;
    private GraphDraw graphDraw;
    @FXML
    private Label viewingInfo;
    @FXML
    private Canvas imageLabel;
    private Image image;
    private Stage onCreatedStage;
    private Point2D imageTranslateBegining;

    private DependencyFinder dependencyFinder;
    private DirectoryChooser directoryChooser;
    private final static String fileNameForFirstGraph = "fileDependencies";
    private final static String fileNameForSecondGraph = "methodDependencies";
    private final static String fileNameForThirdGraph = "moduleDependencies";
    private final static String fileNameForMixedGraph = "mixedDependencies";

    public void scaleImage(ScrollEvent scrollEvent) {
        System.out.println(scrollEvent);
        imageLabel.getGraphicsContext2D().clearRect(0,0,image.getWidth(), image.getHeight());
        if(scrollEvent.getDeltaY() > 0) {
            imageLabel.getGraphicsContext2D().scale(1.2, 1.2);
        } else {
            imageLabel.getGraphicsContext2D().scale(0.8,0.8);
        }

        imageLabel.getGraphicsContext2D().fill();
        imageLabel.getGraphicsContext2D().drawImage(image, 0,0);
    }

    public void imageTranslateStart(MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseButton.PRIMARY) {
            this.imageTranslateBegining = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        }
        System.out.println("Clicked");
    }

    public void imageTranslateEnd(MouseEvent mouseEvent) {
        //in big scale dx is greater than in lower scale
        double scale = imageLabel.getGraphicsContext2D().getTransform().getMxx(); // from transfrom matrix in graphics
        scale = 1/scale;
        Point2D imageDxDy = this.imageTranslateBegining.subtract(mouseEvent.getX(), mouseEvent.getY()).multiply(-1 * scale);
        imageLabel.getGraphicsContext2D().clearRect(0,0,image.getWidth(), image.getHeight());
        imageLabel.getGraphicsContext2D().translate(imageDxDy.getX(), imageDxDy.getY());
        imageLabel.getGraphicsContext2D().drawImage(image, 0, 0);

        System.out.println(imageDxDy);
    }

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

            ControllerFunctions.loadingImage(imageFile1, imageLabel, this, gridPane);
        }
    }

    public void loadFileDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getFilesDependencies(absolutePath, "fillColor=red"), fileNameForFirstGraph, "Showing class dependencies");
    }

    public void loadMethodDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getMethodsDependencies(absolutePath, "fillColor=blue"), fileNameForSecondGraph, "Showing method dependencies");
    }

    public void loadPackageDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getModuleDependencies(absolutePath, "fillColor=green"), fileNameForThirdGraph, "Showing module dependencies");
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

    public void setImage(Image image) {
        this.image = image;
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
                    List<DependencyObj> filesDependencies = dependencyFinder.getFilesDependencies(absolutePath, "fillColor=red");
                    mixedDependencies.addAll(filesDependencies);
                }
                if (checkBoxMethod.isSelected()) {
                    List<DependencyObj> methodsDependencies = dependencyFinder.getMethodsDependencies(absolutePath, "fillColor=green");
                    mixedDependencies.addAll(methodsDependencies);
                }
                if (checkBoxPackage.isSelected()) {
                    List<DependencyObj> moduleDependencies = dependencyFinder.getModuleDependencies(absolutePath, "fillColor=blue");
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
    public void loadMethodDefinitions()
    {}


    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void showAppInfo(ActionEvent actionEvent) {
        ControllerFunctions.infoAlert();
    }

    public Image getImage() {
        return this.image;
    }
}
