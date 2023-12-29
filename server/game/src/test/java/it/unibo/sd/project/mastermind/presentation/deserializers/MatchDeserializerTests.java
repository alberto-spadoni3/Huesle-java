package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class MatchDeserializerTests {
    String start = "{\"UUID\":\"00000000-0000-0001-0000-000000000002\",\"matchStatus\":{\"matchState\":\"PLAYING\",\"players\":{\"0\":{\"username\":\"prova\",\"email\":\"pro@pro.va\",\"password\":\"$2a$14$qpWSHARxIK7a.bQ6WieNbOQ1kniHX4GkmewKH5RY.WAsS/vPCBS8.\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}},\"1\":{\"username\":\"p2\",\"email\":\"p2@pro.va\",\"password\":\"$2a$14$XdQUqHcD/h7guDY2EkXb8.uFXwtrTka9WKZLf726vc7Al8OcRosyK\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}}},\"nextPlayer\":{\"username\":\"prova\",\"email\":\"pro@pro.va\",\"password\":\"$2a$14$qpWSHARxIK7a.bQ6WieNbOQ1kniHX4GkmewKH5RY.WAsS/vPCBS8.\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}}},\"attempts\":{},\"secretCode\":{\"colorCode\":[]}}";

    @Test
    void matchDeserializerTest(){
        Match m = Presentation.deserializerOf(Match.class).deserialize(start);
        System.out.println(m.getMatchID());
        System.out.println(m.getMadeAttempts().size());
        System.out.println(m.getMatchStatus().getPlayers().size());
        System.out.println(m.getSecretCode().getCode());
    }
}
