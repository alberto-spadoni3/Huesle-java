package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.match.SecretCode;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecretCodeSerializerTests {
    private final List<String> sequence = new ArrayList<>();
    private final String result = "{\"colorCode\":[\"crimson\",\"mediumblue\",\"coral\",\"rebeccapurple\"]}";
    @Test
    void secretCodeSerializerTest(){
        this.sequence.add("crimson");
        this.sequence.add("mediumblue");
        this.sequence.add("coral");
        this.sequence.add("rebeccapurple");
        SecretCode secretCode = new SecretCode((ArrayList<String>) sequence);
        assertEquals(result, Presentation.serializerOf(SecretCode.class).serialize(secretCode));
    }

}
