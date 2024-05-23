package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.request.MatchRequest;

public class MatchRequestDeserializer extends AbstractJsonDeserializer<MatchRequest> {
    @Override
    protected MatchRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String requesterUsername = jsonObject.get("requesterUsername").getAsString(),
                matchAccessCode = null,
                matchID = null;
            boolean isMatchPrivate = false;
            if (jsonObject.has("isPrivateMatch"))
                isMatchPrivate = jsonObject.get("isPrivateMatch").getAsBoolean();
            if (jsonObject.has("matchAccessCode"))
                matchAccessCode = jsonObject.get("matchAccessCode").getAsString();
            if (jsonObject.has("matchID"))
                matchID = jsonObject.get("matchID").getAsString();

            return new MatchRequest(requesterUsername)
                .setMatchPrivate(isMatchPrivate)
                .setMatchAccessCode(matchAccessCode)
                .setMatchID(matchID);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as SearchRequest");
        }
    }
}
