package it.unibo.sd.project.mastermind.presentation;

import it.unibo.sd.project.mastermind.controllers.utils.HttpStatusCodes;
import it.unibo.sd.project.mastermind.model.result.OperationResult;
import it.unibo.sd.project.mastermind.model.result.UserOperationResult;
import it.unibo.sd.project.mastermind.model.user.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.user.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserOperationResultTests {
    @Test
    void failedOperationResultSerialization() {
        short statusCode = HttpStatusCodes.BAD_REQUEST;
        String resultMessage = "error";
        UserOperationResult operationResult = new UserOperationResult(statusCode, resultMessage);
        String serializerOpResult = Presentation.serializerOf(UserOperationResult.class).serialize(operationResult);
        String expectedResult = getFailedOperationResultAsJson(statusCode, resultMessage);
        assertEquals(expectedResult, serializerOpResult);
    }

    @Test
    void successOperationResultSerialization() {
        short statusCode = HttpStatusCodes.OK;
        String resultMessage = "success";
        String accessToken = "an.access.token";
        String username = "mariello";
        String hashedPassword = "$2a$05$bvIG6Nmid91Mu9RcmmWZfO5HJIMCT8riNW0hEp8f6/FuA2/mHZFpe";
        String email = "mario@me.com";
        String refreshToken = "a.refresh.token";
        byte profilePictureID = 0;
        boolean darkMode = true;
        boolean colorblindMode = true;

        Player player = new Player(username, email, hashedPassword, refreshToken,
            profilePictureID, new AccessibilitySettings(darkMode, colorblindMode), false);
        UserOperationResult operationResult = new UserOperationResult(statusCode, resultMessage, player, accessToken);
        String serializerOpResult = Presentation.serializerOf(UserOperationResult.class).serialize(operationResult);

        String jsonPlayer = getCustomPlayerAsJson(username, email, hashedPassword, refreshToken,
            profilePictureID, darkMode, colorblindMode);
        String expectedResult = getSuccessOperationResultAsJson(statusCode, resultMessage, jsonPlayer, accessToken);
        assertEquals(expectedResult, serializerOpResult);
    }

    @Test
    void failedOperationResultDeserialization() throws Exception {
        short statusCode = HttpStatusCodes.BAD_REQUEST;
        String resultMessage = "error";

        OperationResult deserialized = Presentation.deserializeAs(
            getFailedOperationResultAsJson(statusCode, resultMessage),
            UserOperationResult.class
        );

        assertEquals(statusCode, deserialized.getStatusCode());
        assertEquals(resultMessage, deserialized.getResultMessage());
    }

    @Test
    void successOperationResultDeserialization() throws Exception {
        short statusCode = HttpStatusCodes.OK;
        String resultMessage = "success";
        String accessToken = "an.access.token";
        String username = "mariello";
        String hashedPassword = "$2a$05$bvIG6Nmid91Mu9RcmmWZfO5HJIMCT8riNW0hEp8f6/FuA2/mHZFpe";
        String email = "mario@me.com";
        String refreshToken = "a.refresh.token";
        byte profilePictureID = 0;
        boolean darkMode = true;
        boolean colorblindMode = true;

        String jsonPlayer = getCustomPlayerAsJson(username, email, hashedPassword, refreshToken,
            profilePictureID, darkMode, colorblindMode);
        UserOperationResult deserialized = Presentation.deserializeAs(
            getSuccessOperationResultAsJson(statusCode, resultMessage, jsonPlayer, accessToken),
            UserOperationResult.class
        );
        assertEquals(statusCode, deserialized.getStatusCode());
        assertEquals(resultMessage, deserialized.getResultMessage());
        Player relatedUser = deserialized.getRelatedUser();
        assertEquals(username, relatedUser.getUsername());
        assertEquals(email, relatedUser.getEmail());
        assertEquals(refreshToken, relatedUser.getRefreshToken());
        assertEquals(darkMode, relatedUser.getSettings().isDarkMode());
    }

    private String getFailedOperationResultAsJson(short statusCode, String resultMessage) {
        return "{\"statusCode\":" + statusCode + "," +
            "\"resultMessage\":\"" + resultMessage + "\"}";
    }

    private String getSuccessOperationResultAsJson(short statusCode, String resultMessage, String player, String accessToken) {
        return "{\"statusCode\":" + statusCode + "," +
            "\"resultMessage\":\"" + resultMessage + "\"," +
            "\"relatedUser\":" + player + "," +
            "\"accessToken\":\"" + accessToken + "\"}";
    }

    private String getCustomPlayerAsJson(String username, String email,
                                         String hashedPassword, String refreshToken, byte profilePictureID,
                                         boolean darkMode, boolean colorblindMode) {
        return "{" +
            "\"username\":\"" + username + "\"," +
            "\"email\":\"" + email + "\"," +
            "\"password\":\"" + hashedPassword + "\"," +
            "\"refreshToken\":\"" + (refreshToken == null ? "" : refreshToken) + "\"," +
            "\"profilePictureID\":" + profilePictureID + "," +
            "\"disabled\":false," +
            "\"accessibilitySettings\":{" +
            "\"darkMode\":" + darkMode + "," +
            "\"colorblindMode\":" + colorblindMode + "}}";
    }
}
