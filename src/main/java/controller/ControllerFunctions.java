package controller;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxCellRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
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
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ControllerFunctions {
    private static Stage onCreatedStage;
    public static void saveGraphImage(String imageName, DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g){
        File imgFile = new File(System.getProperty("user.dir").toString() + "/src/main/resources/" + imageName + ".png");
        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JGraphXAdapter<DependencyObj, EdgeSettings> graphAdapter = new JGraphXAdapter<>(g);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);

        for(Map.Entry<DependencyObj, mxICell> iter : graphAdapter.getVertexToCellMap().entrySet()) {
            iter.getValue().setStyle(iter.getKey().getStyle());
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
            Image image = convertToFxImage(ImageIO.read(imageFile));
            if(controller.getImage() != null) {
                imageLabel.getGraphicsContext2D().clearRect(0,0,controller.getImage().getWidth(), controller.getImage().getHeight());
            }
            controller.setImage(image);
            imageLabel.getGraphicsContext2D().restore();
            imageLabel.getGraphicsContext2D().setFill(Color.BLACK);
            imageLabel.getGraphicsContext2D().drawImage(image, 0., 0.);
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
}
