package it.unibo.sd.project.mastermind.presentation;

import it.unibo.sd.project.mastermind.model.request.LoginRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginRequestTests {
    @Test
    void loginRequestDeserialization() throws Exception {
        String username = "marior";
        String clearPassword = "Mario123!";
        LoginRequest deserializedRequest = Presentation.deserializeAs(
                getLoginRequestAsJson(username, clearPassword),
                LoginRequest.class
        );

        assertEquals(username, deserializedRequest.getRequesterUsername());
        assertEquals(clearPassword, deserializedRequest.getClearPassword());
    }

    private String getLoginRequestAsJson(String username, String clearPassword) {
        return "{\"username\":" + username + "," +
                "\"password\":" + clearPassword + "}";
    }
}
