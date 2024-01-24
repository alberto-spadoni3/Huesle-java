package it.unibo.sd.project.mastermind.presentation.serializers;

import it.unibo.sd.project.mastermind.model.user.AccessibilitySettings;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessibilitySettingsSerializerTest {
    String result = "{\"darkMode\":false,\"colorblindMode\":true}";
    @Test
    void accessibilitySettingsTest(){
        AccessibilitySettings as = new AccessibilitySettings(false, true);
        assertEquals(result, Presentation.serializerOf(AccessibilitySettings.class).serialize(as));
    }
}
