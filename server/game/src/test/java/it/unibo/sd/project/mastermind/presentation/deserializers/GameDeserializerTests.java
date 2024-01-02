package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.Game;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

public class GameDeserializerTests {
    String start = "{\"player\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\",\"password\":\"$2a$14$dOoze0si06EPZrPQXm7n7eLzTMIH/A46EiuHsivVz/ByD1Aa50eHm\",\"refreshToken\":\"\",\"profilePictureID\":0,\"disabled\":false,\"accessibilitySettings\":{\"darkMode\":true,\"colorblindMode\":false}},\"activeMatches\":{},\"endedMatches\":{}}\n";

    @Test
    public void gameDeserializerTest(){
        Game game = Presentation.deserializerOf(Game.class).deserialize(start);
        System.out.println(game.getPlayer().getEmail());
        System.out.println(game.getActiveMatches().size());
        System.out.println(game.getEndedMatches().size());
    }
}
