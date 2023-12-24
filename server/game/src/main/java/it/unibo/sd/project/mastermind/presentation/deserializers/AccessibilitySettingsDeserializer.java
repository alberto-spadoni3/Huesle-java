package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.AccessibilitySettings;

public class AccessibilitySettingsDeserializer extends AbstractJsonDeserializer<AccessibilitySettings> {
    @Override
    protected AccessibilitySettings deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            boolean darkMode = Boolean.parseBoolean(result.get("darkMode").getAsString());
            boolean colorblindMode = Boolean.parseBoolean(result.get("resultMessage").getAsString());
            return new AccessibilitySettings(darkMode, colorblindMode);
        } else {
        throw new RuntimeException("Cannot deserialize " + jsonElement + " as AccessibilitySettings");
        }
    }
}

