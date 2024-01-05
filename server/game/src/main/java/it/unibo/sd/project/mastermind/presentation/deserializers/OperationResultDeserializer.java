package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.OperationResult;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class OperationResultDeserializer extends AbstractJsonDeserializer<OperationResult> {
    @Override
    protected OperationResult deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            short statusCode = result.get("statusCode").getAsShort();
            String message = result.get("resultMessage").getAsString();

            // in case jsonElement is a UserOpResult
            if (result.has("relatedUser") && result.get("relatedUser").isJsonObject()) {
                JsonObject jsonPlayer = (JsonObject) result.get("relatedUser");
                String accessToken = result.get("accessToken").getAsString();
                try {
                    Player player = Presentation.deserializeAs(jsonPlayer.toString(), Player.class);
                    return new UserOperationResult(statusCode, message, player, accessToken);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot deserialize " + jsonPlayer + " as Player " + e.getMessage());
                }
            }

            // in case jsonElement is a MatchOpResult
            if (result.has("matchAccessCode") && result.get("matchAccessCode").isJsonPrimitive()) {
                String matchAccessCode = result.get("matchAccessCode").getAsString();
                return new MatchOperationResult(statusCode, message, matchAccessCode);
            }

            // otherwise, return a basic OpResult
            return new OperationResult(statusCode, message);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as OperationResult");
        }
    }
}
