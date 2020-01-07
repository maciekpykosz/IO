package model.dependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DependencyObj {
    protected static Integer ids = 1;
    protected Integer id;
    protected String name;
    protected Integer weight = 0;  // Current object weight
    protected String edgeStyle = "";
    protected HashMap<DependencyObj, Integer> dependencyList = new HashMap<>(); // dependencyObject with edge weight
    protected Integer cyclomaticComplexity = null;

    public DependencyObj(String name) {
        this.name = name;
        id = ids;
        ids++;
    }

    public Integer getId() {
        return id;
    }

    public static Integer getIds() {
        return ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public HashMap<DependencyObj, Integer> getDependencyList() {
        return dependencyList;
    }

    public void setDependencyList(HashMap<DependencyObj, Integer> dependencyList) {
        this.dependencyList = dependencyList;
    }

    public void addDependency(DependencyObj dependencyObj) {
        if (dependencyList.containsKey(dependencyObj)) {
            dependencyList.put(dependencyObj, (dependencyList.get(dependencyObj) + 1));
        } else {
            dependencyList.put(dependencyObj, 1);
        }
    }

    public static void calculateWeightsForMethods(List<DependencyObj> dependencyList) {
        for (DependencyObj dependencyObj : dependencyList) {
            Map<DependencyObj, Integer> objList = dependencyObj.getDependencyList();
            for (Map.Entry<DependencyObj, Integer> entry : objList.entrySet()) {
                dependencyObj.setWeight((dependencyObj.getWeight() + entry.getValue()));
            }
        }
    }

    public void setCyclomaticComplexity(Integer cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    @Override
    public String toString() {
        if (cyclomaticComplexity != null) {
            return this.name + "\n" + this.weight + "\nCC: " + cyclomaticComplexity;
        } else {
            return this.name + "\n" + this.weight;
        }
    }

    @Override //containsKey basing on this method, so i need to compare 2 obj based on name
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyObj that = (DependencyObj) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getStyle() {
        return this.edgeStyle;
    }

    public void setStyle(String style) {
        this.edgeStyle = style;
    }
}
