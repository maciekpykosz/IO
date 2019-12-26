package model.dependency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodDefinitionDependencyTest {
    MethodDefinitionDependency methodDefinitionDependency = new MethodDefinitionDependency("test");


    @Test
    void toString1() {
        assertEquals("test", methodDefinitionDependency.toString());
    }
}