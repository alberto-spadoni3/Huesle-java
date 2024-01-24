package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.user.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class PlayerDeserializer extends AbstractJsonDeserializer<Player> {
    @Override
    protected Player deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonPlayer = (JsonObject) jsonElement;
            if (jsonPlayer.has("username") &&
                jsonPlayer.has("email") &&
                jsonPlayer.has("password")) {
                // we have now to distinguish between a new Player
                // and one already created that is taken from the DB
                if (jsonPlayer.has("accessibilitySettings") &&
                    jsonPlayer.has("profilePictureID") &&
                    jsonPlayer.has("disabled")) {
                    // initialize a Player object which was already present in the DB
                    String hashedPassword = jsonPlayer.get("password").getAsString();
                    JsonObject jsonSettings = jsonPlayer.getAsJsonObject("accessibilitySettings");
                    AccessibilitySettings settings;
                    try {
                        settings = Presentation.deserializeAs(jsonSettings.toString(), AccessibilitySettings.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot deserialize as " + AccessibilitySettings.class.getName());
                    }
                    String possibleToken = jsonPlayer.get("refreshToken").getAsString();
                    return new Player(
                            jsonPlayer.get("username").getAsString(),
                            jsonPlayer.get("email").getAsString(),
                            hashedPassword,
                            possibleToken.isBlank() ? null : possibleToken,
                            jsonPlayer.get("profilePictureID").getAsByte(),
                            settings,
                            jsonPlayer.get("disabled").getAsBoolean()
                    );
                } else {
                    // create a new Player
                    String clearPassword = jsonPlayer.get("password").getAsString();
                    return new Player(
                            jsonPlayer.get("username").getAsString(),
                            jsonPlayer.get("email").getAsString(),
                            clearPassword
                    );
                }
            }
            else
                throw new RuntimeException("Cannot deserialize as " + Player.class.getName() + " for missing fields");
        } else
            throw new RuntimeException("Cannot deserialize as " + Player.class.getName());
    }
}
