package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.match.SecretCode;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecretCodeDeserializerTests {
    private final String start = "{\"colorCode\":[\"crimson\",\"mediumblue\",\"coral\",\"rebeccapurple\"]}";

    @Test
    void SecretCodeDeserializerTest(){
        SecretCode secretCode = Presentation.deserializerOf(SecretCode.class).deserialize(start);
        assertEquals(start, Presentation.serializerOf(SecretCode.class).serialize(secretCode));
    }
}
