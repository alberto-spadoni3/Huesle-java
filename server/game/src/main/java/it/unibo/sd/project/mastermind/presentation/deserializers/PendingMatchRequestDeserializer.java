package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.PendingMatchRequest;
import it.unibo.sd.project.mastermind.model.match.SearchRequest;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class PendingMatchRequestDeserializer extends AbstractJsonDeserializer<PendingMatchRequest> {
    @Override
    protected PendingMatchRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            if (jsonObject.has("requesterUsername")) {
                String requesterUsername = jsonObject.get("requesterUsername").getAsString();
                PendingMatchRequest matchRequest = new PendingMatchRequest(requesterUsername);
                if (jsonObject.has("matchAccessCode"))
                    matchRequest.setMatchAccessCode(jsonObject.get("matchAccessCode").getAsString());
                return matchRequest;
            }
        } else
            throw new RuntimeException("Cannot deserialize element " + jsonElement);
        return null;
    }
}
