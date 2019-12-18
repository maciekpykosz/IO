package model.dependency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodDependencyTest {
    MethodDependency methodDependency = new MethodDependency("");

    @Test
    void shouldSetGreenColorOfEdge() {
        assertEquals("fillColor=green", methodDependency.getStyle());
    }
}
