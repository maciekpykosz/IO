package model.dependency;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyFinderTest {
    private static final String TEST_FOLDER_PATH = System.getProperty("user.dir");
    private DependencyFinder dependencyFinder = new DependencyFinder();
    private List<DependencyObj> methodDependencies = dependencyFinder.getMethodsDependencies(TEST_FOLDER_PATH);
    private List<DependencyObj> fileDependencies = dependencyFinder.getFilesDependencies(TEST_FOLDER_PATH);

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


}