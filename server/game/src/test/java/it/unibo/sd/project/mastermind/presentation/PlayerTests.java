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
    private final String hashedPassword = getHashedPassword();
    private final byte profilePictureID = 4;
    private final boolean darkMode = false;
    private final boolean colorblindMode = true;

    @Test
    void newPlayerSerialization() {
        Player newPlayer = new Player(username, email, clearPassword);
        String serializedPlayer = Presentation.serializerOf(Player.class).serialize(newPlayer);
        String jsonPlayer = getCustomPlayerAsJson(
                newPlayer.getPassword(),
                null,
                (byte) 0,
                true,
                false
        );

        assertEquals(jsonPlayer, serializedPlayer);
    }

    @Test
    void CustomPlayerSerialization() {
        // Player without a refresh token
        Player customPlayer = new Player(
                username, email, hashedPassword, null, profilePictureID,
                new AccessibilitySettings(darkMode, colorblindMode),
                false
            );
        String serializedPlayer = Presentation.serializerOf(Player.class).serialize(customPlayer);
        String jsonPlayer = getCustomPlayerAsJson(
                hashedPassword,
                null,
                profilePictureID,
                darkMode,
                colorblindMode
        );
        assertEquals(jsonPlayer, serializedPlayer);

        // Player with a refresh token
        String refreshToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTYXJhIiwiaWF0IjoxNzAzMjY2ODE1LCJleHAiOjE3MDMyNjY5MzV9.Kjkf_Njfd_K6iKkXybwzehcCUUgEgxxWIsd4srCKhgA";
        Player customPlayerWithToken = new Player(
                username, email, hashedPassword, refreshToken, profilePictureID,
                new AccessibilitySettings(darkMode, colorblindMode),
                false
        );
        String serializedPlayerWithToken = Presentation.serializerOf(Player.class).serialize(customPlayerWithToken);
        String jsonPlayerWithToken = getCustomPlayerAsJson(
                hashedPassword,
                refreshToken,
                profilePictureID,
                darkMode,
                colorblindMode
        );
        assertEquals(jsonPlayerWithToken, serializedPlayerWithToken);
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
        assertNull(newPlayer.getRefreshToken());
    }

    @Test
    void CustomPlayerDeserialization() throws Exception {
        Player deserializedPlayer = Presentation.deserializeAs(
                getCustomPlayerAsJson(
                        hashedPassword,
                        null,
                        profilePictureID,
                        darkMode,
                        colorblindMode
                ), Player.class);

        String refreshToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTYXJhIiwiaWF0IjoxNzAzMjY2ODE1LCJleHAiOjE3MDMyNjY5MzV9.Kjkf_Njfd_K6iKkXybwzehcCUUgEgxxWIsd4srCKhgA";
        Player deserializedPlayerWithToken = Presentation.deserializeAs(
                getCustomPlayerAsJson(
                        hashedPassword,
                        refreshToken,
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
        assertNull(deserializedPlayer.getRefreshToken());
        assertEquals(refreshToken, deserializedPlayerWithToken.getRefreshToken());
    }

    private String getHashedPassword() {
        return BCrypt.withDefaults().hashToString(14, clearPassword.toCharArray());
    }

    private String getNewPlayerAsJson() {
        return "{\"username\":" + username + "," +
                "\"email\":" + email + "," +
                "\"password\":" + clearPassword + "}";
    }

    private String getCustomPlayerAsJson(String hashedPassword, String refreshToken, byte profilePictureID,
                                         boolean darkMode, boolean colorblindMode) {
        return "{" +
                "\"username\":\"" + username + "\"," +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + hashedPassword + "\"," +
                "\"refreshToken\":\"" + (refreshToken == null ? "" : refreshToken) + "\"," +
                "\"profilePictureID\":" + profilePictureID + "," +
                "\"disabled\":false," +
                "\"accessibilitySettings\":{" +
                "\"darkMode\":" + darkMode + "," +
                "\"colorblindMode\":" + colorblindMode + "}}";
    }
}
