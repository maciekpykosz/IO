package model;
import model.dependency.DependencyObj;
import model.dependency.MethodDefinitionDependency;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphDraw {

    public DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> getGraphForDependencies(List<DependencyObj> dependencyObjList) {
        DefaultDirectedWeightedGraph<DependencyObj, EdgeSettings> graph = new DefaultDirectedWeightedGraph<>(EdgeSettings.class);
        for (DependencyObj dependencyObj : dependencyObjList) {
            graph.addVertex(dependencyObj);
            HashMap<DependencyObj, Integer> dependencies = dependencyObj.getDependencyList();
            for (Map.Entry<DependencyObj, Integer> entry : dependencies.entrySet()) {
                graph.addVertex(entry.getKey());
                EdgeSettings edgeSettings;
                if (entry.getKey() instanceof MethodDefinitionDependency){
                    edgeSettings = new EdgeSettings();
                }else {
                    edgeSettings = new EdgeSettings(entry.getValue().toString());
                }
                graph.addEdge(entry.getKey(), dependencyObj, edgeSettings);
            }
        }
        return graph;
    }
    public static class EdgeSettings extends DefaultEdge {
        private String label;

        public EdgeSettings(){

        }

        public EdgeSettings(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

}
