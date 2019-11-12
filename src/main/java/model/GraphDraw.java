package model;
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
                graph.addEdge(entry.getKey(), dependencyObj, new EdgeSettings(entry.getValue().toString()));
            }
        }
        return graph;
    }
}
