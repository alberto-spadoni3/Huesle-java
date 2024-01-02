package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class MatchStatusDeserializerTests {


    String start = "{\"matchState\":\"PLAYING\",\"players\":{" +
            "\"0\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\",\"password\":" +
            "\"$2a$14$Zo/Ujuzwm9E.JcqeQJQd3.8dBEF8B4lPSiiFzR2FmA.Um/8vKlAAi\"," +
            "\"refreshToken\":\"\",\"profilePictureID\":4,\"disabled\":false,\"accessibilitySettings\":{" +
            "\"darkMode\":false,\"colorblindMode\":true}}," +
            "\"1\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\",\"password\":" +
            "\"$2a$14$Zo/Ujuzwm9E.JcqeQJQd3.8dBEF8B4lPSiiFzR2FmA.Um/8vKlAAi\"," +
            "\"refreshToken\":\"\",\"profilePictureID\":4,\"disabled\":false,\"accessibilitySettings\":{" +
            "\"darkMode\":false,\"colorblindMode\":true}}}," +
            "\"nextPlayer\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\",\"password\":" +
            "\"$2a$14$Zo/Ujuzwm9E.JcqeQJQd3.8dBEF8B4lPSiiFzR2FmA.Um/8vKlAAi\",\"refreshToken\":\"\",\"profilePictureID\":4,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":false,\"colorblindMode\":true}}}";

    @Test
    void matchStatusDeserializerTest() throws Exception {
        MatchStatus m = Presentation.deserializeAs(start, MatchStatus.class);
        System.out.println(m.getMatchState());
        System.out.println(m.getPlayers());
        System.out.println(m.getNextPlayer());

    }
}
