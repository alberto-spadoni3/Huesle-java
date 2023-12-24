package it.unibo.sd.project.mastermind.model;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.util.Objects;

public class Player {
    private final String username;
    private final String email;
    private final String password;
    private byte profilePictureID;
    private AccessibilitySettings settings;
    private boolean disabled;
    private String refreshToken;

    public Player(String username, String email, String clearPassword) {
        this.username = username;
        this.email = email;
        this.password = getHashedPassword(clearPassword);
        this.settings = new AccessibilitySettings();
        this.profilePictureID = 0;
        this.disabled = false;
    }

    public Player(String username, String email, String hashedPassword, String refreshToken,
                  byte profilePictureID, AccessibilitySettings accessibilitySettings, boolean disabled) {
        this.username = username;
        this.email = email;
        this.password = hashedPassword;
        this.settings = accessibilitySettings;
        this.profilePictureID = profilePictureID;
        this.disabled = disabled;
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return password;
    }

    public byte getProfilePictureID() {
        return profilePictureID;
    }

    public AccessibilitySettings getSettings() {
        return settings;
    }

    private String getHashedPassword(String clearPassword) {
        return BCrypt.withDefaults().hashToString(14, clearPassword.toCharArray());
    }

    public void setProfilePictureID(byte profilePictureID) {
        this.profilePictureID = profilePictureID;
    }

    public void setAccessibilitySettings(AccessibilitySettings settings) {
        this.settings = settings;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean verifyPassword(String clearPassword) {
        return BCrypt.verifyer()
                .verify(clearPassword.toCharArray(), this.password)
                .verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(username, player.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }
}
