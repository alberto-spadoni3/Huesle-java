package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class MatchStatusDeserializerTests {
    String p1p = " ";
    String p2p = " ";

    String start = "{\"matchState\":\"PLAYING\",\"players\":{" +
            "\"0\":{\"username\":\"prova\",\"email\":\"pro@pro.va\"," +
            "\"password\":\""+p1p+"\"," +
            "\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{" +
            "\"darkMode\":true,\"colorblindMode\":false}}," +
            "\"1\":{\"username\":\"p2\",\"email\":\"p2@pro.va\"," +
            "\"password\":\""+p2p+"\"," +
            "\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{" +
            "\"darkMode\":true,\"colorblindMode\":false}}}," +
            "\"nextPlayer\":{\"username\":\"prova\",\"email\":\"pro@pro.va\"," +
            "\"password\":\"$2a$14$mxlqjRjx6qO2GTlixfieFusI0PwaOTO5iLip5cr2cTrG4O8b71PAi\"," +
            "\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{" +
            "\"darkMode\":true,\"colorblindMode\":false}}}";

    @Test
    void matchStatusDeserializerTest() throws Exception {
        MatchStatus m = Presentation.deserializeAs(start, MatchStatus.class);
        System.out.println(m.getMatchState());
        System.out.println(m.getPlayers());
        System.out.println(m.getNextPlayer());

    }
}
