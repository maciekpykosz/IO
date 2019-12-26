package model.dependency;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import model.GraphDraw;

import static org.junit.jupiter.api.Assertions.*;

class DependencyFinderTest {
    private static final String TEST_FOLDER_PATH = System.getProperty("user.dir");
    private DependencyFinder dependencyFinder = new DependencyFinder();
    private List<DependencyObj> methodDependencies = dependencyFinder.getMethodsDependencies(TEST_FOLDER_PATH);
    private List<DependencyObj> fileDependencies = dependencyFinder.getFilesDependencies(TEST_FOLDER_PATH);
    private List<DependencyObj> moduleDependencies = dependencyFinder.getModuleDependencies(TEST_FOLDER_PATH);
    private List<DependencyObj> methodeDefinitions = dependencyFinder.getMethodsDefinitions(TEST_FOLDER_PATH);
    private List<DependencyObj> methodeDefinitions2 = dependencyFinder.getMethodsDefinitions("");

    @Test
    void getLastCreatedDependencies_shouldReturnNullObject(){
        DependencyFinder dependencyFinder = new DependencyFinder();
        assertEquals(dependencyFinder.getLastCreatedDependencies(), null);
    }

    @Test
    void getLastCreatedDependencies_shouldReturnList(){
        List<DependencyObj> lastCreatedDependencies = dependencyFinder.getLastCreatedDependencies();
        assertTrue(lastCreatedDependencies.size() > 0);
    }
    @Test
    void getMethodsDependencies_shouldReturnNotEmptyObject() {
        assertFalse(methodDependencies.isEmpty());
    }

    @Test
    void getMethodsDependencies_shouldReturnALinkedList() {
        assertTrue(LinkedList.class.isAssignableFrom(methodDependencies.getClass()));
    }

    @Test
    void getMethodsDependencies_shouldSayThatDependencyObjIsInstanceOfMethodDependency() {
        for(DependencyObj depObj : methodDependencies)
            assertTrue(depObj instanceof MethodDependency );
    }

    @Test
    void getFileDependencies_shouldReturnNotEmptyObject(){assertFalse(fileDependencies.isEmpty());}

    @Test
    void  getFileDependencies_shouldReturnALinkedList()
    {
        assertTrue(LinkedList.class.isAssignableFrom(fileDependencies.getClass()));
    }

    @Test
    void getFileDependencies_shouldSayThatDependencyObjIsInstanceOfFileDependency()
    {
        for(DependencyObj depObj : fileDependencies)
            assertTrue(depObj instanceof FileDependency );
    }

    @Test
    void getModuleDependencies_shouldReturnNotEmptyObject(){
        assertFalse(moduleDependencies.isEmpty());
    }

    @Test
    void getModuleDependencies_shouldReturnALinkedList(){
        assertTrue(LinkedList.class.isAssignableFrom(moduleDependencies.getClass()));
    }

    @Test
    void getModuleDependencies_shouldSayThatDependencyObjIsInstanceOfModuleDependency(){
        for (DependencyObj moduleDependency : moduleDependencies) {
            assertTrue(moduleDependency instanceof ModuleDependency);
        }
    }

    @Test
    void getMethodsDefinitions_shouldNotReturnEmptyList() {
        assertFalse(methodeDefinitions.isEmpty());
    }
    //podanie sciezki (folder zawierajacy pliki java*) metoda ma zwracac liste nie pusta!

    @Test
    void getMethodsDefinitions_shouldReturnEmptyList() {
        assertTrue(methodeDefinitions2.isEmpty());
    }
    //podanie pustej sciezki (folder nie zawierajacy plikow java*) metoda ma zwracac pusta liste
}