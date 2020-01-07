package model.dependency;

import java.util.Objects;

public class GroupDependency extends DependencyObj {
    public GroupDependency(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return name + weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDependency that = (GroupDependency) o;
        return (name.equals(that.name) && (weight.equals(that.weight)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }
}
