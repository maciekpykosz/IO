package model.dependency;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModuleDependencyTest {
    @Test
    void shouldSetYellowColor(){
        ModuleDependency moduleDependency = new ModuleDependency("");
        assertEquals(moduleDependency.getStyle(), "fillColor=yellow");
    }

}
