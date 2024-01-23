package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.OperationRequest;

public class SettingsRequest extends OperationRequest {
    private AccessibilitySettings accessibilitySettings;
    private byte profilePictureID;

    public SettingsRequest(String requesterUsername) {
        super(requesterUsername);
        accessibilitySettings = null;
        profilePictureID = 0;
    }

    public SettingsRequest setAccessibilitySettings(AccessibilitySettings accessibilitySettings) {
        this.accessibilitySettings = accessibilitySettings;
        return this;
    }

    public SettingsRequest setProfilePictureID(byte profilePictureID) {
        this.profilePictureID = profilePictureID;
        return this;
    }

    public AccessibilitySettings getAccessibilitySettings() {
        return accessibilitySettings;
    }

    public byte getProfilePictureID() {
        return profilePictureID;
    }
}
