package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.user.Player;

public class PlayerSerializer extends AbstractJsonSerializer<Player> {
    @Override
    protected JsonElement toJsonElement(Player player) {
        JsonObject jsonPlayer = new JsonObject();
        jsonPlayer.addProperty("username", player.getUsername());
        String email = player.getEmail();
        jsonPlayer.addProperty("email", email == null ? "" : email);
        jsonPlayer.addProperty("password", player.getPassword());
        String refreshToken = player.getRefreshToken();
        jsonPlayer.addProperty("refreshToken", refreshToken == null ? "" : refreshToken);
        jsonPlayer.addProperty("profilePictureID", player.getProfilePictureID());
        jsonPlayer.addProperty("disabled", player.isDisabled());
        JsonObject settings = new JsonObject();
        settings.addProperty("darkMode", player.getSettings().isDarkMode());
        settings.addProperty("colorblindMode", player.getSettings().isColorblindMode());
        jsonPlayer.add("accessibilitySettings", settings);
        return jsonPlayer;
    }
}
