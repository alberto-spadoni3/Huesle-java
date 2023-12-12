package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    @Test
    void passwordVerification() {
        String email = "mario.rossi@unibo.it";
        String username = "mariorossi";
        String password = "Mario123!";
        Player player = new Player(username, email, password, true);

        assertTrue(player.verifyPassword("Mario123!"), "The given password doesn't match with the user password");
    }
}
