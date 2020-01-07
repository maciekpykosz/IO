package model.dependency;

public class MethodDefinitionDependency extends DependencyObj{
    public MethodDefinitionDependency(String name) {
        super(name);
    }

    @Override
    public String toString() {
        if (cyclomaticComplexity != null) {
            return this.name + "\nCC: " + cyclomaticComplexity;
        } else {
            return this.name;
        }
    }
}
