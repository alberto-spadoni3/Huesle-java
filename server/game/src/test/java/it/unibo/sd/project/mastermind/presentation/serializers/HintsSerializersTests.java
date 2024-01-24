package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.match.Hints;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HintsSerializersTests {
    private final int rightPositions = 1;
    private final int rightColours = 2;
    private final Hints hints = new Hints(new Byte((byte) rightPositions), new Byte((byte) rightColours));

    @Test
    void hintsSerializationTest(){
        System.out.println(Presentation.serializerOf(Hints.class).serialize(hints));
        assertEquals(result, Presentation.serializerOf(Hints.class).serialize(hints));
    }
    String result = "{\"rightPositions\":"+rightPositions+",\"rightColours\":"+rightColours+"}";
}
