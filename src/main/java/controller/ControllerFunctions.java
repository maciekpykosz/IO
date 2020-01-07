package controller;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxCellRenderer;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.dependency.DependencyObj;
import model.GraphDraw.EdgeSettings;
import model.dependency.GroupDependency;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerFunctions {
    public static void saveGraphImage(String imageName, DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g){
        File imgFile = new File(System.getProperty("user.dir").toString() + "/src/main/resources/" + imageName + ".png");
        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //code to define groups in separating graphs
        //defining group vertices
        List<GroupDependency> groups = new LinkedList<>();
        int maxGroupNum = g.vertexSet().stream().map(DependencyObj::getGroupId).max(Integer::compareTo).get();
        if(maxGroupNum != -1) {
            for(int i=0; i<=maxGroupNum; ++i) {
                GroupDependency group = new GroupDependency("Group");
                group.setWeight(i);
                groups.add(group);
                g.addVertex(group);
            }
        }

        JGraphXAdapter<DependencyObj, EdgeSettings> graphAdapter = new JGraphXAdapter<>(g);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        for(Map.Entry<DependencyObj, mxICell> iter : graphAdapter.getVertexToCellMap().entrySet()) {
            iter.getValue().setStyle(iter.getKey().getStyle());
        }

        //adding vertex to groups
        if(maxGroupNum != -1) {
            Map<Integer, List<mxICell>> vertexPerGroupId = new HashMap<>();

            //grouping vertex per groupId
            for(Map.Entry<DependencyObj, mxICell> iter : graphAdapter.getVertexToCellMap().entrySet()) {
                if(iter.getKey().getGroupId() == -1) continue;
                Integer vertexGroupId = iter.getKey().getGroupId();

                if(vertexPerGroupId.containsKey(vertexGroupId)) {
                    vertexPerGroupId.get(vertexGroupId).add(iter.getValue());
                } else {
                    List<mxICell> listOfCells = new LinkedList<>();
                    listOfCells.add(iter.getValue());
                    vertexPerGroupId.put(vertexGroupId, listOfCells);
                }
            }

            //creating groups in graph
            for(GroupDependency group : groups) {
                mxICell groupCell = graphAdapter.getVertexToCellMap().get(group);
                Object[] groupVertex = vertexPerGroupId.get(group.getWeight()).toArray();
                graphAdapter.groupCells(groupCell, 0, groupVertex);
            }
        }

        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, java.awt.Color.WHITE , true, null);
        try {
            ImageIO.write(image, "PNG", imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //converting bufferedImage to image in javaFX in Java8
    private static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }

        return new ImageView(wr).getImage();
    }

    public static void loadingImage(File imageFile, Canvas imageLabel, Controller controller, GridPane gridPane){
        try {
            final Image image = convertToFxImage(ImageIO.read(imageFile));
            Platform.runLater(()-> {
                Image toRender = image;
                boolean showWarningAboutResize = false;
                //scale image if it is too big
                if(toRender.getWidth() > 15000) {       //probably max supported image size
                    try {
                        toRender = new Image(imageFile.toURI().toURL().toString(), 15000, image.getHeight(), true, false);
                        showWarningAboutResize = true;
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                if (controller.getImage() != null) {
                    imageLabel.getGraphicsContext2D().clearRect(0, 0, controller.getImage().getWidth(), controller.getImage().getHeight());
                }
                controller.setImage(toRender);
                imageLabel.getGraphicsContext2D().restore();
                imageLabel.getGraphicsContext2D().setFill(Color.BLACK);
                imageLabel.getGraphicsContext2D().drawImage(toRender, 0., 0.);

                //showing alert
                if(showWarningAboutResize) {
                    Alert popupWindow = new Alert(Alert.AlertType.WARNING,
                            "Application do not support loading large images(up to 15k pixels wide/tall. " +
                                    "If you want see proper quality image, open it with dedicated image viewer!",
                            ButtonType.OK);
                    popupWindow.showAndWait();
                }
            });
        }catch (IOException e) {
            System.err.println("Blad odczytu obrazka");
            e.printStackTrace();
        }
    }

    public static void infoAlert() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Info");
        info.setHeaderText("Information about this program");
        info.setContentText("Dependency analyser made by \"Gumisie\"");
        info.showAndWait();
    }
    public static void setDefaultDirector(DirectoryChooser directoryChooser){
        File defaultDirectory = new File(System.getProperty("user.dir") + "/..");
        if (! defaultDirectory.exists()) {
            defaultDirectory.mkdirs();
        }
        directoryChooser.setInitialDirectory(defaultDirectory);
    }

    public static List<DependencyObj> mergeDependencies(List<DependencyObj> methodDep, List<DependencyObj> packetDep) {
        List<DependencyObj> mergedDep = new LinkedList<>();
        //store all methods in packetDep
        Map<String, DependencyObj> packetMethods = new HashMap<>();

        packetDep.stream()
                .filter((depObj -> depObj.getName().split("\\n").length > 1))
                .forEach(dependencyObj -> {
                    String methodName = dependencyObj.getName().split("\\n")[0];
                    packetMethods.putIfAbsent(methodName, dependencyObj);
                });

        for(DependencyObj method : methodDep) {
            //firstly check connections in methods
            //stacks are to modify map after foreach loop
            Stack<DependencyObj> linkToRemove = new Stack<>();
            Stack<DependencyObj> linkToAdd = new Stack<>();

            for(Map.Entry<DependencyObj, Integer> methodLink : method.getDependencyList().entrySet()) {
                DependencyObj sameMethodLink = packetMethods.get(methodLink.getKey().getName());
                if(sameMethodLink != null) {
                    linkToRemove.push(methodLink.getKey());
                    linkToAdd.push(sameMethodLink);
                }
            }

            while(!linkToRemove.empty()) {
                DependencyObj removedLink = linkToRemove.pop();
                DependencyObj newLink = linkToAdd.pop();

                method.getDependencyList().put(newLink, method.getDependencyList().get(removedLink));
                method.getDependencyList().remove(removedLink);
            }

            //secondly check if method has duplicate in packet
            DependencyObj sameMethodInPacket = packetMethods.get(method.getName());
            if(sameMethodInPacket != null) {
                //merge vertex
                for(Map.Entry<DependencyObj, Integer> methodLink : method.getDependencyList().entrySet()) {
                    sameMethodInPacket.getDependencyList().put(methodLink.getKey(), methodLink.getValue());
                }

                sameMethodInPacket.setStyle("fillColor=#007f7f");
                sameMethodInPacket.setWeight(sameMethodInPacket.getWeight() + method.getWeight());
            } else {
                mergedDep.add(method);
            }
        }

        mergedDep.addAll(packetDep);

        return mergedDep;
    }
}
