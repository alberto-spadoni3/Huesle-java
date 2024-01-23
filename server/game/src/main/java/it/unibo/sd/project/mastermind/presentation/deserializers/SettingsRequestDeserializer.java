package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.SettingsRequest;
import it.unibo.sd.project.mastermind.model.AccessibilitySettings;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class SettingsRequestDeserializer extends AbstractJsonDeserializer<SettingsRequest> {
    @Override
    protected SettingsRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String requesterUsername = jsonObject.get("requesterUsername").getAsString();
            SettingsRequest settingsRequest = new SettingsRequest(requesterUsername);
            try {
                if (((JsonObject) jsonElement).has("accessibilitySettings")) {
                    JsonObject jsonSettings = jsonObject.get("accessibilitySettings").getAsJsonObject();
                    AccessibilitySettings settings = Presentation.deserializeAs(jsonSettings.toString(), AccessibilitySettings.class);
                    settingsRequest.setAccessibilitySettings(settings);
                } else settingsRequest.setProfilePictureID(jsonObject.get("profilePictureID").getAsByte());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return settingsRequest;
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as SettingsRequest");
        }
    }
}
