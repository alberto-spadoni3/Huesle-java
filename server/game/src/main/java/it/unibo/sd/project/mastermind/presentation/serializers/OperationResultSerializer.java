package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.OperationResult;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class OperationResultSerializer extends AbstractJsonSerializer<OperationResult> {
    @Override
    protected JsonElement toJsonElement(OperationResult object) {
        JsonObject jsonOpResult = new JsonObject();
        jsonOpResult.addProperty("statusCode", object.getStatusCode());
        jsonOpResult.addProperty("resultMessage", object.getResultMessage());
        if (object instanceof UserOperationResult userOperationResult) {
            Player possibleUser = userOperationResult.getRelatedUser();
            String accessToken = userOperationResult.getAccessToken();
            if (possibleUser != null && accessToken != null) {
                JsonElement jsonPlayer = Presentation.serializerOf(Player.class).getJsonElement(possibleUser);
                if (jsonPlayer.isJsonObject())
                    jsonOpResult.add("relatedUser", jsonPlayer);
                jsonOpResult.addProperty("accessToken", accessToken);
            }
        } else if (object instanceof MatchOperationResult matchOperationResult) {
            String matchAccessCode = matchOperationResult.getMatchAccessCode();
            if (matchAccessCode != null)
                jsonOpResult.addProperty("matchAccessCode", matchAccessCode);
        }
        return jsonOpResult;
    }
}
