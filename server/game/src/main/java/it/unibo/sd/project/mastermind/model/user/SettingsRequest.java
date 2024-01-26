package it.unibo.sd.project.mastermind.model.user;

import it.unibo.sd.project.mastermind.model.OperationRequest;

public class SettingsRequest extends OperationRequest {
    private AccessibilitySettings accessibilitySettings;
    private byte profilePictureID;
    private String newEmail, oldPassword, newPassword;

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

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public void setPasswords(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public AccessibilitySettings getAccessibilitySettings() {
        return accessibilitySettings;
    }

    public byte getProfilePictureID() {
        return profilePictureID;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
