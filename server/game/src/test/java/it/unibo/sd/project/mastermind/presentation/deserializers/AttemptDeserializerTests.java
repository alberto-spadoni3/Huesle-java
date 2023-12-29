package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class AttemptDeserializerTests {
    String start = "{\"colorSequence\":[],\"hints\":{\"rightPositions\":1,\"rightColours\":1},\"attemptMadeBy\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\",\"password\":\"$2a$14$mkvVKtNQd8KTAViim6Etuuue7u8SzkMhrErxzsMvnGT11TjLCK6oq\",\"refreshToken\":\"\",\"profilePictureID\":4,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":false,\"colorblindMode\":true}}}";
    @Test
    void attemptDeserializerTest(){
        Attempt a = Presentation.deserializerOf(Attempt.class).deserialize(start);
        System.out.println(a.getColorSequence());
        System.out.println(a.getHints().getRightColours());
        System.out.println(a.getPlayer().getEmail());
    }
}
