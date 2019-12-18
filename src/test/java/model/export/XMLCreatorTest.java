package model.export;

import com.jamesmurty.utils.XMLBuilder2;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.xml.transform.OutputKeys;

import model.dependency.DependencyFinder;
import model.dependency.DependencyObj;


import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

class XMLCreatorTest {

    @Test
    void getBuilder() {
        XMLCreator creator = new XMLCreator();
        XMLBuilder2 builder = XMLBuilder2.create("Project")
                .a("DocumentationType", "html")
                .a("ExporterVersion", "12.2")
                .a("Name", "untitled")
                .a("UmlVersion", "2.x")
                .a("Xml_structure", "simple")
                .e("Models");
        assertNotEquals(builder, creator.getBuilder());
    }

    @Test
    void getProperties() {
        XMLCreator creator = new XMLCreator();
        Properties properties = new Properties();
        properties.put(OutputKeys.METHOD, "xml");
        properties.put(OutputKeys.INDENT, "yes");
        properties.put("{http://xml.apache.org/xslt}indent-amount", "2");
        assertEquals(properties, creator.getProperties());
    }

    @Test
    void addClassesWithDependencies() {
        XMLCreator creator = new XMLCreator();
        XMLCreator emptyCreator = new XMLCreator();

        assertNotNull(creator);

        File selectedDir = new File(System.getProperty("user.dir").toString() + "/src/test/lab09fx");
        String absolutePath = selectedDir.getAbsolutePath();
        DependencyFinder dependencyFinder = new DependencyFinder();
        List<DependencyObj> filesDependencies = dependencyFinder.getMethodsDependencies(absolutePath);
        creator.addClassesWithDependencies(filesDependencies);

        assertNotEquals(creator, emptyCreator);

        XMLBuilder2 builder = creator.getBuilder();
        File expected = new File(System.getProperty("user.dir").toString() + "/src/main/resources/expectedMethodDependencies.xml");
        File actual = new File(System.getProperty("user.dir").toString() + "/src/main/resources/testMethodDependiencies.xml");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(actual));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = creator.getProperties();
        builder.toWriter(writer, properties);

        Scanner input1 = null;
        try {
            input1 = new Scanner(expected);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scanner input2 = null;
        try {
            input2 = new Scanner(actual);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        boolean var = FALSE;
        for (Scanner it = input1; it.hasNext() && !var; ) {
            String s = it.next();
            for (; input2.hasNext(); ) {
                String s2 = input2.next();
                if (s.equals(s2)) {
                    break;
                } else if (!input2.hasNext() &&
                        !s.contains("From") && !s2.contains("From") &&
                        !s.contains("Id") && !s2.contains("Id") &&
                        !s.contains("To") && !s2.contains("To")) {
                    var = TRUE;
                }
            }
        }
        assertFalse(var);
    }
}