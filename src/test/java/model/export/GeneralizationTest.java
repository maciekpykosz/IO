package model.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneralizationTest {

    @Test
    void getFrom() {
        Integer from = 1;
        Generalization generalization = new Generalization(from, 20);
        assertEquals(from, generalization.getFrom());
    }

    @Test
    void getTo() {
        Integer to = 20;
        Generalization generalization = new Generalization(1, to);
        assertEquals(to, generalization.getTo());
    }
}