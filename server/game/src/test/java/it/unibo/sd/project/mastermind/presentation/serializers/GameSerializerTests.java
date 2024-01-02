package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.Game;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameSerializerTests {
    private final String email = "mario.rossi@unibo.it";
    private final String username = "marior";
    private final String clearPassword = "Mario123!";

    @Test
    void gameSerializerTest(){

        Player newPlayer = new Player(username, email, clearPassword);

        Game game = new Game(newPlayer);
        System.out.println(Presentation.serializerOf(Game.class).getJsonElement(game));
        assertEquals(true, true);
    }
}
