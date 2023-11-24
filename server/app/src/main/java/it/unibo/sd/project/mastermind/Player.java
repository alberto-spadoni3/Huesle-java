package it.unibo.sd.project.mastermind;

import java.util.Objects;

public class Player {
    private final String username;
    private final String email;
    private final String password;
    private byte profilePictureID;
    private AccessibilitySettings settings;
    private boolean disables;

    public Player(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.settings = new AccessibilitySettings();
        this.profilePictureID = 0;
        this.disables = false;
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
