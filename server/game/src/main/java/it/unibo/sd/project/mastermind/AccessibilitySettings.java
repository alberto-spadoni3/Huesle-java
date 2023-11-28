package it.unibo.sd.project.mastermind;

public class AccessibilitySettings {
    private boolean darkMode;
    private boolean colorblindMode;

    public AccessibilitySettings() {
        this.darkMode = true;
        this.colorblindMode = false;
    }

    public AccessibilitySettings(boolean darkMode, boolean colorblindMode) {
        this.darkMode = darkMode;
        this.colorblindMode = colorblindMode;
    }

    public void toggleDarkMode() {
        this.darkMode = !this.darkMode;
    }

    public void toggleColorblindMode() {
        this.colorblindMode = !this.colorblindMode;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public boolean isColorblindMode() {
        return colorblindMode;
    }
}
