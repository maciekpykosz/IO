package model.export;

import com.jamesmurty.utils.XMLBuilder2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import model.DependencyObj;

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
}
