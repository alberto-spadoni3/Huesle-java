package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.AccessibilitySettings;

public class AccessibilitySettingsDeserializer extends AbstractJsonDeserializer<AccessibilitySettings> {
    @Override
    protected AccessibilitySettings deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            boolean darkMode = result.get("darkMode").getAsBoolean();
            boolean colorblindMode = result.get("colorblindMode").getAsBoolean();
            return new AccessibilitySettings(darkMode, colorblindMode);
        } else {
        throw new RuntimeException("Cannot deserialize " + jsonElement + " as AccessibilitySettings");
        }
    }
}

