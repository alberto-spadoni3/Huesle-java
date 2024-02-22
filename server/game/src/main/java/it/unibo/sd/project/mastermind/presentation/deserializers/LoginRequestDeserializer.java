package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.request.LoginRequest;

public class LoginRequestDeserializer extends AbstractJsonDeserializer<LoginRequest> {
    @Override
    protected LoginRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            if (jsonObject.has("username") && jsonObject.has("password")) {
                String username = jsonObject.get("username").getAsString();
                String clearPassword = jsonObject.get("password").getAsString();
                return new LoginRequest(username, clearPassword);
            } else
                throw new RuntimeException("Cannot deserialize as " + LoginRequest.class.getName());
        } else
            throw new RuntimeException("Cannot deserialize as " + LoginRequest.class.getName());
    }
}
