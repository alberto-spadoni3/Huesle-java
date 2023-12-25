package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class HintsDeserializerTests {
    private final int rightPositions = 1;
    private final int rightColours = 2;
    String start = "{\"rightPositions\":"+rightPositions+",\"rightColours\":"+rightColours+"}";

    @Test
    void hintsDeserializerTest() throws Exception {
        Hints h = Presentation.deserializeAs(start, Hints.class);
        System.out.println(h.getRightPositions());
        System.out.println(h.getRightColours());
    }
}
