package model.dependency;

public class MethodDefinitionDependency extends DependencyObj{
    public MethodDefinitionDependency(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
