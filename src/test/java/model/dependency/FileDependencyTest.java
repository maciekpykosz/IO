package model.dependency;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class FileDependencyTest {

    FileDependency fileDependency = new FileDependency("");

    @Test
    void shouldSetRedColorOfEdge(){assertEquals("fillColor=red",fileDependency.getStyle());}
}
