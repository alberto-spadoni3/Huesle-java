package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttemptDeserializerTests {
    String start =
            "{\"colorSequence\":[\"red\",\"green\",\"yellow\",\"green\"]" +
            ",\"hints\":{\"rightPositions\":1,\"rightColours\":2}," +
            "\"attemptMadeBy\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\",\"password\":\"$2a$14$mkvVKtNQd8KTAViim6Etuuue7u8SzkMhrErxzsMvnGT11TjLCK6oq\",\"refreshToken\":\"\",\"profilePictureID\":4,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":false,\"colorblindMode\":true}}}";

    @Test
    void attemptDeserializerTest(){
        ArrayList<String> sequence = new ArrayList<>(List.of("red", "green", "yellow", "green"));
        Attempt a = Presentation.deserializerOf(Attempt.class).deserialize(start);

        assertEquals(sequence, a.getColorSequence());
        assertEquals(2, a.getHints().getRightColours());
        assertEquals(1, a.getHints().getRightPositions());
        assertEquals("mario.rossi@unibo.it", a.getPlayer().getEmail());
    }
}
