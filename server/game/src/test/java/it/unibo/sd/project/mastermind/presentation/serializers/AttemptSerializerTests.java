package it.unibo.sd.project.mastermind.presentation.serializers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import it.unibo.sd.project.mastermind.model.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttemptSerializerTests {
    private final String email = "mario.rossi@unibo.it";
    private final String username = "marior";
    private final String clearPassword = "Mario123!";
    private final String hashedPassword = getHashedPassword();
    private final byte profilePictureID = 4;
    private final boolean darkMode = false;
    private final boolean colorblindMode = true;
    @Test
    void attemptMadeBySerializationTest(){
        Player player = new Player(username,email,hashedPassword,profilePictureID,new AccessibilitySettings(darkMode, colorblindMode), false );
        List<String> emptyList = new ArrayList<>();
        Byte b = new Byte(String.valueOf(1));
        Hints hints = new Hints(b, b);
        Attempt attempt = new Attempt(emptyList, player, hints);
        assertEquals(getCustomPlayerAsJson(hashedPassword, profilePictureID, darkMode, colorblindMode), Presentation.serializerOf(Attempt.class).serialize(attempt));
    }

    private String getHashedPassword() {
        return BCrypt.withDefaults().hashToString(14, clearPassword.toCharArray());
    }

    private String getCustomPlayerAsJson(String hashedPassword, byte profilePictureID,
                                         boolean darkMode, boolean colorblindMode) {
        return "{\"colorSequence\":[],\"hints\":{\"rightPositions\":1,\"rightColours\":1}," +
                "\"attemptMadeBy\":{\"username\":\"marior\",\"email\":\"mario.rossi@unibo.it\"," +
                "\"password\":\"$2a$14$PGb7NrZ4VoxdXAzM14C7beYqUwmG2vLY9q8TfikhgWukyEbLyUSoO\"," +
                "\"refreshToken\":\"\",\"profilePictureID\":4,\"disabled\":false," +
                "\"accessibilitySettings\":{\"darkMode\":false,\"colorblindMode\":true}}}";
    }
}
