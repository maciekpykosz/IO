package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DependencyObj {
    private static Integer ids = 1;
    private Integer id;
    private String name;
    private Integer weight = 0;  // Current object weight
    private HashMap<DependencyObj, Integer> dependencyList = new HashMap<>(); // dependencyObject with edge weight

    public DependencyObj() {
        id = ids;
        ids++;
    }

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

    @Override
    public String toString() {
        return this.name + "n" + this.weight;
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
}
