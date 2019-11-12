package model;

import org.jgraph.graph.DefaultEdge;

public class EdgeSettings extends DefaultEdge {
    private String label;

    public EdgeSettings(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
