package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.PendingMatchRequest;

public class PendingMatchRequestSerializer extends AbstractJsonSerializer<PendingMatchRequest> {
    @Override
    protected JsonElement toJsonElement(PendingMatchRequest object) {
        JsonObject result = new JsonObject();
        result.addProperty("requesterUsername", object.getRequesterUsername());
        result.addProperty("matchAccessCode", object.getMatchAccessCode());
        return result;
    }
}
