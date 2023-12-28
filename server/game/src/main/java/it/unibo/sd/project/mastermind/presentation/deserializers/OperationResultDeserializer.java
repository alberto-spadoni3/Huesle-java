package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.user.OperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class OperationResultDeserializer extends AbstractJsonDeserializer<OperationResult> {
    @Override
    protected OperationResult deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            short statusCode = result.get("statusCode").getAsShort();
            String message = result.get("resultMessage").getAsString();
            if (result.has("relatedUser") && result.get("relatedUser").isJsonObject()) {
                JsonObject jsonPlayer = (JsonObject) result.get("relatedUser");
                String accessToken = result.get("accessToken").getAsString();
                try {
                    Player player = Presentation.deserializeAs(jsonPlayer.toString(), Player.class);
                    return new OperationResult(statusCode, message, player, accessToken);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot deserialize " + jsonPlayer + " as Player " + e.getMessage());
                }
            } else
                return new OperationResult(statusCode, message);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as OperationResult");
        }
    }
}
