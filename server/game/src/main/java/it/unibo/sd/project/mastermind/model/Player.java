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

    public Player(String username, String email, String password, boolean clearPassword) {
        this.username = username;
        this.email = email;
        this.password = clearPassword ?
                getHashedPassword(password) :
                password;
        this.settings = new AccessibilitySettings();
        this.profilePictureID = 0;
        this.disabled = false;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    private String getHashedPassword(String clearPassword) {
        return BCrypt.withDefaults().hashToString(14, clearPassword.toCharArray());
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

    public boolean isDisabled() {
        return this.disabled;
    }
}
