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
import javafx.stage.DirectoryChooser;
import model.dependency.DependencyObj;
import model.GraphDraw.EdgeSettings;
import model.dependency.Difference;
import model.dependency.GroupDependency;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class ControllerFunctions {
    public static void saveGraphImage(String imageName, DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> g, String hash){
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

        mergeFiles(imgFile, hash);
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
                imageLabel.getGraphicsContext2D().setFill(javafx.scene.paint.Color.BLACK);
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

    private static final String hashPath = System.getProperty("user.dir").toString() + "/src/main/resources/hash.png";

    private static void mergeFiles(File pngFile, String hash) {
        try {
            BufferedImage graphFile = ImageIO.read(pngFile);
            BufferedImage hashFile = createHashFile(hash);
            int width = Math.max(graphFile.getWidth(), hashFile.getWidth());
            int height = Math.max(graphFile.getHeight(), hashFile.getHeight());
            BufferedImage combinedFile = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics graphics = combinedFile.getGraphics();
            int hashPositionX = width - hashFile.getWidth();
            int hashPositionY = height - hashFile.getHeight();
            graphics.drawImage(graphFile, 0, 0, null);
            graphics.drawImage(hashFile, hashPositionX, hashPositionY, null);
            ImageIO.write(combinedFile, "PNG", pngFile);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static BufferedImage createHashFile(String hash) {
        final int width = 300;
        final int height = 15;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bi.createGraphics();
        Font font = new Font("TimesRoman", Font.BOLD, 12);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(hash);
        int stringHeight = fontMetrics.getAscent();
        graphics.setPaint(Color.BLACK);
        graphics.drawString(hash, (width - stringWidth) / 2, height / 2 + stringHeight / 4);
        return bi;
    }

    public static String getHashFromRepository(String repoPath){
        File gitDirectory = new File(repoPath + "/.git");
        String version = "unknown";
        if (gitDirectory.exists()) {
            try {
                Repository repo = new FileRepositoryBuilder()
                        .setGitDir(gitDirectory)
                        .build();
                ObjectId head = repo.resolve("HEAD");
                version = head.getName();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return version;
    }

    public static List<Difference> getDiffFromCommits(String repoPath) {
        File gitRepo = new File(repoPath + "/.git");
        try {
            Repository repo = new FileRepositoryBuilder()
                    .setGitDir(gitRepo)
                    .build();
            ObjectId oldHead = repo.resolve("HEAD~1^{tree}");
            ObjectId head = repo.resolve("HEAD^{tree}");

            //System.out.println("Printing diff between tree: " + oldHead + " and " + head);

            // prepare the two iterators to compute the diff between
            ObjectReader reader = repo.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);

            // finally get the list of changed files

            Git git = new Git(repo);
            List<DiffEntry> diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();
            return getDifferenceList(diffs);
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Difference> getDifferenceList(List<DiffEntry> diffEntries) {
        List<Difference> differences = new ArrayList<>();
        for (DiffEntry diffEntry : diffEntries) {
            String modifier = diffEntry.getChangeType().toString();
            String[] filePath = diffEntry.getNewPath().split("/");
            String[] fileName = filePath[filePath.length - 1].split("\\.");
            if (fileName[1].equals("java")) {
                Difference difference = new Difference(modifier, fileName[0]);
                differences.add(difference);
            }
        }
        return differences;
    }

    public static List<DependencyObj> addModifiers(List<DependencyObj> dependencies, List<Difference> differences){
        for (DependencyObj dependency : dependencies) {
            for (Difference difference : differences) {
                if (dependency.getName().equals(difference.getFileName())){
                    dependency.addModifierToName(difference.getModifier());
                }
            }
        }
        return dependencies;
    }
}