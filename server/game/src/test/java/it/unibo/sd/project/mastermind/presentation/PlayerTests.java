package it.unibo.sd.project.mastermind.presentation;

import at.favre.lib.crypto.bcrypt.BCrypt;
import it.unibo.sd.project.mastermind.model.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTests {
    private final String email = "mario.rossi@unibo.it";
    private final String username = "marior";
    private final String clearPassword = "Mario123!";
    private final String hashedPassword = getHashedPassword(clearPassword);
    private final byte profilePictureID = 4;
    private final boolean darkMode = false;
    private final boolean colorblindMode = true;

    @Test
    void newPlayerSerialization() {
        Player newPlayer = new Player(username, email, clearPassword);
        String serializedPlayer = Presentation.serializerOf(Player.class).serialize(newPlayer);
        String jsonPlayer = getCustomPlayerAsJson(
                newPlayer.getPassword(),
                (byte) 0,
                true,
                false
        );

        assertEquals(jsonPlayer, serializedPlayer);
    }

    @Test
    void CustomPlayerSerialization() {
        Player customPlayer = new Player(
                username, email, hashedPassword, profilePictureID,
                new AccessibilitySettings(darkMode, colorblindMode),
                false
            );
        String serializedPlayer = Presentation.serializerOf(Player.class).serialize(customPlayer);
        String jsonPlayer = getCustomPlayerAsJson(
                hashedPassword,
                profilePictureID,
                darkMode,
                colorblindMode
        );

        assertEquals(jsonPlayer, serializedPlayer);
    }

    @Test
    void newPlayerDeserialization() throws Exception {
        Player newPlayer = Presentation.deserializeAs(getNewPlayerAsJson(), Player.class);

        assertEquals(username, newPlayer.getUsername());
        assertEquals(email, newPlayer.getEmail());
        assertTrue(newPlayer.verifyPassword(clearPassword));
        assertEquals(0, newPlayer.getProfilePictureID());
        assertTrue(newPlayer.getSettings().isDarkMode());
        assertFalse(newPlayer.getSettings().isColorblindMode());
        assertFalse(newPlayer.isDisabled());
    }

    @Test
    void CustomPlayerDeserialization() throws Exception {
        Player deserializedPlayer = Presentation.deserializeAs(
                getCustomPlayerAsJson(
                        hashedPassword,
                        profilePictureID,
                        darkMode,
                        colorblindMode
                ), Player.class);

        assertEquals(username, deserializedPlayer.getUsername());
        assertEquals(email, deserializedPlayer.getEmail());
        assertEquals(hashedPassword, deserializedPlayer.getPassword());
        assertEquals(profilePictureID, deserializedPlayer.getProfilePictureID());
        assertEquals(darkMode, deserializedPlayer.getSettings().isDarkMode());
        assertEquals(colorblindMode, deserializedPlayer.getSettings().isColorblindMode());
        assertFalse(deserializedPlayer.isDisabled());
    }

    private String getHashedPassword(String clearPassword) {
        return BCrypt.withDefaults().hashToString(14, clearPassword.toCharArray());
    }

    private String getNewPlayerAsJson() {
        return "{\"username\":" + username + "," +
                "\"email\":" + email + "," +
                "\"password\":" + clearPassword + "}";
    }

    private String getCustomPlayerAsJson(String hashedPassword, byte profilePictureID,
                                         boolean darkMode, boolean colorblindMode) {
        return "{" +
                "\"username\":\"" + username + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + hashedPassword + "\"," +
                "\"profilePictureID\":" + profilePictureID + "," +
                "\"disabled\":false," +
                "\"accessibilitySettings\":{" +
                "\"darkMode\":" + darkMode + "," +
                "\"colorblindMode\":" + colorblindMode + "}}";
    }
}
