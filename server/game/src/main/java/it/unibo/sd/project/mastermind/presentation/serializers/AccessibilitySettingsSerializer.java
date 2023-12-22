package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.AccessibilitySettings;

public class AccessibilitySettingsSerializer extends AbstractJsonSerializer<AccessibilitySettings> {
    @Override
    protected JsonElement toJsonElement(AccessibilitySettings accessibilitySettings) {
        JsonObject jsonAccessibilitySettings = new JsonObject();
        jsonAccessibilitySettings.addProperty("darkMode", accessibilitySettings.isDarkMode());
        jsonAccessibilitySettings.addProperty("colorblindMode", accessibilitySettings.isColorblindMode());
        return jsonAccessibilitySettings;
    }
}
