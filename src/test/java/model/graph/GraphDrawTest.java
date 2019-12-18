package model.graph;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import model.GraphDraw;
import model.dependency.DependencyFinder;
import model.dependency.DependencyObj;

import static org.junit.jupiter.api.Assertions.*;

public class GraphDrawTest {
    private GraphDraw graphDraw = new GraphDraw();
    private static final String TEST_FOLDER_PATH = System.getProperty("user.dir");
    private DependencyFinder dependencyFinder = new DependencyFinder();
    private List<DependencyObj> methodDependencies = dependencyFinder.getMethodsDependencies(TEST_FOLDER_PATH);
    private List<DependencyObj> fileDependencies = dependencyFinder.getFilesDependencies(TEST_FOLDER_PATH);
    private List<DependencyObj> moduleDependencies = dependencyFinder.getModuleDependencies(TEST_FOLDER_PATH);

    @Test
    void edgeSettings_shouldReturnNullLabel(){
        GraphDraw.EdgeSettings edge = new GraphDraw.EdgeSettings();
        assertEquals(edge.toString(), null);
    }

    @Test
    void edgeSettings_shouldReturnNotNullLabel(){
        GraphDraw.EdgeSettings edge = new GraphDraw.EdgeSettings("");
        assertNotEquals(edge.toString(), null);
    }

    @Test
    void getGraphForDependencies_shouldReturnGraph(){
        DefaultDirectedWeightedGraph<DependencyObj, GraphDraw.EdgeSettings> graphForMethodDependencies = graphDraw.getGraphForDependencies(methodDependencies);
        DefaultDirectedWeightedGraph<DependencyObj, GraphDraw.EdgeSettings> graphForFileDependencies = graphDraw.getGraphForDependencies(fileDependencies);
        DefaultDirectedWeightedGraph<DependencyObj, GraphDraw.EdgeSettings> graphForModuleDependencies = graphDraw.getGraphForDependencies(moduleDependencies);

        assertTrue(graphForFileDependencies.vertexSet().size() <= fileDependencies.size());
        assertTrue(graphForMethodDependencies.vertexSet().size() <= methodDependencies.size());
        assertTrue(graphForModuleDependencies.vertexSet().size() <= moduleDependencies.size());
    }
}
