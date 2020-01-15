package controller;

import com.jamesmurty.utils.XMLBuilder2;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Affine;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.dependency.DependencyFinder;
import model.dependency.DependencyObj;
import model.GraphDraw;
import model.GraphDraw.EdgeSettings;
import model.export.XMLCreator;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private Point2D imageTranslateBeginning;
    private Point2D screenCenter;
    private DependencyFinder dependencyFinder;
    private DirectoryChooser directoryChooser;
    private final static String fileNameForFirstGraph = "fileDependencies";
    private final static String fileNameForSecondGraph = "methodDependencies";
    private final static String fileNameForThirdGraph = "moduleDependencies";
    private final static String fileNameForMixedGraph = "mixedDependencies";
    private final static String fileNameForFourthGraph = "methodDefinitions";

    private interface LoadDependency {
        List<DependencyObj> getDependencies(String absolutePath);
    }

    public void scaleImage(ScrollEvent scrollEvent) {
        if(image == null)
            return;

        Affine transformMatrix = imageLabel.getGraphicsContext2D().getTransform();
        imageLabel.getGraphicsContext2D().clearRect(0,0,image.getWidth(), image.getHeight());
        if(scrollEvent.getDeltaY() > 0) {
            transformMatrix.appendScale(1.2, 1.2, screenCenter);
        } else {
            transformMatrix.appendScale(0.8, 0.8, screenCenter);
        }

        imageLabel.getGraphicsContext2D().setTransform(transformMatrix);
        imageLabel.getGraphicsContext2D().fill();
        imageLabel.getGraphicsContext2D().drawImage(image, 0,0);
    }

    public void imageTranslateStart(MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == MouseButton.PRIMARY) {
            this.imageTranslateBeginning = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        }
    }

    public void imageTranslate(MouseEvent mouseEvent) {
        if(image == null || mouseEvent.getButton() != MouseButton.PRIMARY)
            return;
        double scale = imageLabel.getGraphicsContext2D().getTransform().getMxx(); // from transfrom matrix in graphics
        scale = 1/scale;
        Point2D imageDxDy = this.imageTranslateBeginning.subtract(mouseEvent.getX(), mouseEvent.getY()).multiply(-1 * scale);
        screenCenter = screenCenter.subtract(imageDxDy);
        imageLabel.getGraphicsContext2D().clearRect(0,0,image.getWidth(), image.getHeight());
        imageLabel.getGraphicsContext2D().translate(imageDxDy.getX(), imageDxDy.getY());
        imageLabel.getGraphicsContext2D().drawImage(image, 0, 0);
        this.imageTranslateBeginning = new Point2D(mouseEvent.getX(), mouseEvent.getY());
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
            CompletableFuture.supplyAsync(()-> {
                List<DependencyObj> dependencyObjs = loadDependency.getDependencies(selectedDir.getAbsolutePath());

                String version = ControllerFunctions.getHashFromRepository(selectedDir.getAbsolutePath());
                DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g1 = graphDraw.getGraphForDependencies(dependencyObjs);
                ControllerFunctions.saveGraphImage(fileName, g1, version);
                File imageFile1 = new File(System.getProperty("user.dir").toString() + "/src/main/resources/" + fileName + ".png");
                Platform.runLater(() -> viewingInfo.setText(viewingInfoText));

                //reset translations
                Platform.runLater(() -> imageLabel.getGraphicsContext2D().setTransform(1, 0, 0, 1, 0, 0));
                ControllerFunctions.loadingImage(imageFile1, imageLabel, this, gridPane);
                screenCenter = new Point2D(imageLabel.getWidth() / 2., imageLabel.getHeight() / 2);
                return null;
            });
        }
    }

    public void loadFileDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getFilesDependencies(absolutePath), fileNameForFirstGraph, "Showing class dependencies");
    }

    public void loadMethodDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath ->
                        dependencyFinder.getMethodsDependencies(absolutePath)
                , fileNameForSecondGraph, "Showing method dependencies");
    }

    public void loadPackageDep(ActionEvent actionEvent) {
        makeDependencies(absolutePath -> dependencyFinder.getModuleDependencies(absolutePath), fileNameForThirdGraph, "Showing module dependencies");
    }

    public void exportToXML(ActionEvent actionEvent) {
        XMLCreator creator = new XMLCreator();
        List<DependencyObj> lastCreated = dependencyFinder.getLastCreatedDependencies();
        if(lastCreated == null) {
            Alert popup = new Alert(Alert.AlertType.ERROR,"No graph already loaded", ButtonType.OK);
            popup.showAndWait();
            return;
        }

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

        //id's only for testing purpose
        checkBoxFile.setId("combinationFileDep");
        checkBoxMethod.setId("combinationMethodDep");
        checkBoxPackage.setId("combinationPackageDep");
        b.setId("combinationOkButton");
        c.setId("combinationClearButton");

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
                if (checkBoxMethod.isSelected()) {
                    List<DependencyObj> methodsDependencies = dependencyFinder.getMethodsDependencies(absolutePath);
                    mixedDependencies.addAll(methodsDependencies);
                }
                if (checkBoxPackage.isSelected()) {
                    List<DependencyObj> moduleDependencies = dependencyFinder.getModuleDependencies(absolutePath);
                    if(checkBoxMethod.isSelected()) {
                        mixedDependencies.addAll(ControllerFunctions.mergeDependencies(mixedDependencies,moduleDependencies));
                    } else {
                        mixedDependencies.addAll(moduleDependencies);
                    }
                }
                if (checkBoxFile.isSelected()) {
                    List<DependencyObj> filesDependencies = dependencyFinder.getFilesDependencies(absolutePath);
                    mixedDependencies.addAll(filesDependencies);
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
    public void loadMethodDefinitions() {
        makeDependencies(absolutePath -> dependencyFinder.getMethodsDefinitions(absolutePath), fileNameForFourthGraph, "Showing method definitions");
    }
    public List<Set<DependencyObj>> findPartitions(int partitions, List<DependencyObj> dependencies) {
        //finding solo vertex, take all childrens, and gets their parents
        int part = 1;
        List<DependencyObj> aloneVertexes = findAloneVertexes(dependencies);
        List<DependencyObj> othersVertexes = new ArrayList<>(dependencies);
        othersVertexes.removeAll(aloneVertexes);
        if (aloneVertexes.size() > 0 && ++part == partitions) {
            //return status for 2
        }
        List<Set<DependencyObj>> connectedGraphs = findConnectedGraphs(othersVertexes);
        if ((part + connectedGraphs.size() - 1) > partitions) {
            partitions -= aloneVertexes.size() > 0 ? 1 : 0;
            connectedGraphs = mergeGroupsOfVertexes(connectedGraphs, partitions);
        }
        Set<DependencyObj> aloneSet = new HashSet<>(aloneVertexes);
        connectedGraphs.add(aloneSet);
        return connectedGraphs;
    }

    public List<DependencyObj> findAloneVertexes(List<DependencyObj> dependencies) {
        List<DependencyObj> dependenciesWithoutFriends = new ArrayList<>();
        for (DependencyObj dependency : dependencies) {
            if (dependency.getDependencyList().size() == 0 && dependency.getWeight() == 0) {
                dependenciesWithoutFriends.add(dependency);
            }
        }
        for (DependencyObj dependency : dependencies) {
            for (Map.Entry<DependencyObj, Integer> entry : dependency.getDependencyList().entrySet()) {
                dependenciesWithoutFriends.remove(entry.getKey());
            }
        }
        return dependenciesWithoutFriends;
    }

    public List<Set<DependencyObj>> findConnectedGraphs(List<DependencyObj> dependencies) {
        DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> graph = graphDraw.getGraphForDependencies(dependencies);
        ConnectivityInspector<DependencyObj, EdgeSettings> inspector = new ConnectivityInspector<>(graph);
        List<Set<DependencyObj>> connectedVertexes = inspector.connectedSets();
        return connectedVertexes;
    }

    public List<Set<DependencyObj>> mergeGroupsOfVertexes(List<Set<DependencyObj>> vertexesGroups, int partitions) {
        int vertexesSize = vertexesGroups.size();
        for (int i = 0; i < vertexesSize - partitions; i++) {
            //sort
            vertexesGroups.sort(Comparator.comparingInt(Set::size));
            //merge
            vertexesGroups.get(0).addAll(vertexesGroups.get(1));
            vertexesGroups.remove(1);
        }
        return vertexesGroups;
    }

    public void loadMethodPartitionDep(ActionEvent actionEvent) {
        int partitionsCount = Integer.parseInt(((MenuItem) actionEvent.getSource()).getText());
        ControllerFunctions.setDefaultDirector(directoryChooser);
        File selectedDir = directoryChooser.showDialog(onCreatedStage);
        if (selectedDir != null) {
            String version = ControllerFunctions.getHashFromRepository(selectedDir.getAbsolutePath());
            List<DependencyObj> methodsDependencies = dependencyFinder.getMethodsDependencies(selectedDir.getAbsolutePath());
            methodsDependencies = methodsDependencies.stream().distinct().collect(Collectors.toList());
            List<Set<DependencyObj>> partitions = findPartitions(partitionsCount, methodsDependencies);
            // TODO: 2019-12-17 Draw graph with frames for partitions
            List<DependencyObj> partitionedGraphElements = new LinkedList<>();
            for(int i=0; i<partitions.size(); ++i){
                Set<DependencyObj> subPartition = partitions.get(i);
                int finalI = i;
                subPartition.forEach(depObj -> {
                    depObj.setGroupId(finalI);
                    partitionedGraphElements.add(depObj);
                });
            }
            ControllerFunctions.saveGraphImage("partitioned", graphDraw.getGraphForDependencies(partitionedGraphElements), version);
        }
    }

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