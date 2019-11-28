package model.export;

import com.jamesmurty.utils.XMLBuilder2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import model.dependency.DependencyObj;

public class XMLCreator {

    private XMLBuilder2 builder;
    private Properties properties;
    private static Integer elementsId;

    public XMLCreator() {
        builder = XMLBuilder2.create("Project")
                .a("DocumentationType", "html")
                .a("ExporterVersion", "12.2")
                .a("Name", "untitled")
                .a("UmlVersion", "2.x")
                .a("Xml_structure", "simple")
                .e("Models");
        properties = new Properties();
        properties.put(OutputKeys.METHOD, "xml");
        properties.put(OutputKeys.INDENT, "yes");
        properties.put("{http://xml.apache.org/xslt}indent-amount", "2");
        elementsId = DependencyObj.getIds();
    }

    public XMLBuilder2 getBuilder() {
        return builder;
    }

    public Properties getProperties() {
        return properties;
    }

    private List<Generalization> getGeneralizations(List<DependencyObj> dependencies) {
        List<Generalization> generalizations = new ArrayList<>();
        for (DependencyObj fromDependency : dependencies) {
            for (Map.Entry<DependencyObj, Integer> entry : fromDependency.getDependencyList().entrySet()) {
                DependencyObj toDependency = entry.getKey();
                Generalization generalization = new Generalization(fromDependency.getId(), toDependency.getId());
                generalizations.add(generalization);
            }
        }
        return generalizations;
    }

    public void addClassesWithDependencies(List<DependencyObj> dependencies) {
        for (DependencyObj dependencyObj : dependencies) {
            builder.e("Class")
                    .a("Id", dependencyObj.getId().toString())
                    .a("Name", dependencyObj.getName())
                    .up();
        }

        List<Generalization> generalizations = getGeneralizations(dependencies);
        for (Generalization generalization : generalizations) {
            builder.e("Usage")
                    .a("Id", elementsId.toString())
                    .a("From", generalization.getFrom().toString())
                    .a("To", generalization.getTo().toString())
                    .up();
            elementsId++;
        }
    }
}
