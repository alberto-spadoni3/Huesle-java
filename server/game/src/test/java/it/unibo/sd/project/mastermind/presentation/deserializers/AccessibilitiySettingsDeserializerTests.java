package it.unibo.sd.project.mastermind.presentation.deserializers;

import it.unibo.sd.project.mastermind.model.AccessibilitySettings;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessibilitiySettingsDeserializerTests {
    String start = "{\"darkMode\":false,\"colorblindMode\":true}";

    @Test
    void AccessibilitySettingsDeserializerTest() throws Exception {
        AccessibilitySettings as;
        try {
            as = Presentation.deserializeAs(start, AccessibilitySettings.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(as.isDarkMode());
        System.out.println(as.isColorblindMode());
        assertEquals(true, true);
    }
}
