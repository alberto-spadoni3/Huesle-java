package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchStatusSerializerTests {
    Player p1 = new Player("prova","pro@pro.va","asdf");
    Player p2 = new Player("p2", "p2@pro.va","asdf");
    String p1p = p1.getPassword();

    String p2p = p2.getPassword();
    ArrayList<Player> players = new ArrayList<Player>();

    /*
    String result = "{\"matchState\":\"PLAYING\",\"players\":{" +
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
    */
    @Test
    void matchStatusSerializerTest(){
        players.add(p1);
        players.add(p2);
        MatchStatus ms = new MatchStatus(players);

        System.out.println(Presentation.serializerOf(MatchStatus.class).serialize(ms));
    }
}
