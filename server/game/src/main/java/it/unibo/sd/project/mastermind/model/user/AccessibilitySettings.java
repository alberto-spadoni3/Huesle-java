package it.unibo.sd.project.mastermind.model.user;

import java.util.Objects;

public class AccessibilitySettings {
    private final boolean darkMode;
    private final boolean colorblindMode;

    public AccessibilitySettings() {
        this.darkMode = true;
        this.colorblindMode = false;
    }

    public AccessibilitySettings(boolean darkMode, boolean colorblindMode) {
        this.darkMode = darkMode;
        this.colorblindMode = colorblindMode;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public boolean isColorblindMode() {
        return colorblindMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessibilitySettings that = (AccessibilitySettings) o;
        return darkMode == that.darkMode && colorblindMode == that.colorblindMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(darkMode, colorblindMode);
    }
}
